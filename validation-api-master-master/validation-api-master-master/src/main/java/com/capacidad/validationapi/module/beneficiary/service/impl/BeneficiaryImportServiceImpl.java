package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryImportDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryImportService;
import com.capacidad.validationapi.module.importprocessor.misc.ImportUtils;
import com.capacidad.validationapi.module.importprocessor.model.ImportOperation;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import com.capacidad.validationapi.module.importprocessor.model.OperationResolverProperties;
import com.capacidad.validationapi.module.importprocessor.service.ImportErrorHandler;
import com.capacidad.validationapi.module.importprocessor.service.impl.BaseImportService;
import com.capacidad.validationapi.module.render.service.impl.CSVReaderWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;

@Log4j2
@Service
public class BeneficiaryImportServiceImpl extends BaseImportService<BeneficiaryImportDTO> implements BeneficiaryImportService {

    private final BeneficiaryImportBuilderTemplate importBuilderTemplate;
    private final BeneficiaryImportOperationResolver importOperationResolver;
    private final BeneficiaryImportPropertiesInitializer importPropertiesInitializer;
    private final ImportErrorHandler importErrorHandler;
    private final ObjectMapper objectMapper;

    @Autowired
    public BeneficiaryImportServiceImpl(BeneficiaryImportBuilderTemplate importBuilderTemplate,
                                        BeneficiaryImportOperationResolver importOperationResolver,
                                        ImportErrorHandler importErrorHandler,
                                        BeneficiaryImportPropertiesInitializer importPropertiesInitializer,
                                        ObjectMapper objectMapper) {
        this.importBuilderTemplate = importBuilderTemplate;
        this.importOperationResolver = importOperationResolver;
        this.importErrorHandler = importErrorHandler;
        this.importPropertiesInitializer = importPropertiesInitializer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void importMultipartFile(ImportProperties importProperties) throws ObjectNotValidException {
        super.importMultipartFile(importProperties);
        MultipartFile file = importProperties.getMultipartFile();
        this.validateFile(file);
        Charset readerCharset = StandardCharsets.ISO_8859_1;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), readerCharset));
             OutputStreamWriter writer = new OutputStreamWriter(importProperties.getOutputStream(), StandardCharsets.UTF_8)) {
            TenantContext.setTenant(importProperties.getTenantId());
            SecurityContextHolder.getContext().setAuthentication(importProperties.getAuthentication());
            importProperties.setReader(reader);
            CSVReaderWrapper<BeneficiaryImportDTO> csvReader = this.buildCsvReader(importProperties);
            Iterator<BeneficiaryImportDTO> iterator = csvReader.iterator(BeneficiaryImportDTO.class, false);
            OperationResolverProperties<Beneficiary> operationProperties = new OperationResolverProperties<>(Collections.emptyList(),
                    importProperties,
                    writer,
                    csvReader.getRowCount(readerCharset),
                    100);
            ImportReport importResult = processImport(iterator, operationProperties);
            writer.write(objectMapper.writeValueAsString(importResult));
            writer.flush();
            TenantContext.clearTenant();
            SecurityContextHolder.clearContext();
        } catch (IOException e) {
            log.error("{} - importMultipartFile({}): {}", this.getClass(), importProperties.getClass(), e.getMessage());
        }
    }

    private ImportReport processImport(Iterator<BeneficiaryImportDTO> iterator, OperationResolverProperties<Beneficiary> operationProperties) {
        Instant start = Instant.now();
        log.info("({}) - processImport - Starting Beneficiaries import operation: {}, total elements: {}", this.getClass(),
                operationProperties.getImportProperties().getOperation(),
                operationProperties.getTotalElements());
        ImportReport result = processBeneficiaries(iterator, operationProperties);
        Instant end = Instant.now();
        result.setDuration(Duration.between(start, end).toSeconds());
        log.info("({}) - processImport - Database operations executed in {} seconds", this.getClass(), result.getDuration());
        return result;
    }

    private ImportReport processBeneficiaries(Iterator<BeneficiaryImportDTO> iterator, OperationResolverProperties<Beneficiary> operationProperties) {
        ImportReport finalReport = new ImportReport();
        final int flushSize = 3500;
        Map<String, Object> persistedProperties = importPropertiesInitializer.initializeProperties();
        List<Beneficiary> beneficiaryTempList = new ArrayList<>();
        operationProperties.setObjects(beneficiaryTempList);
        final AtomicInteger tempCount = new AtomicInteger(0);
        final AtomicInteger finalCount = new AtomicInteger(0);
        iterator.forEachRemaining(throwingConsumer(i -> {
            buildObject(i, finalReport, persistedProperties, operationProperties.getImportProperties().getOperation())
                    .ifPresent(beneficiaryTempList::add);
            tempCount.incrementAndGet();
            finalCount.incrementAndGet();
            if (tempCount.get() == flushSize || finalCount.get() == operationProperties.getTotalElements()) {
                beneficiaryTempList.forEach(b -> importBuilderTemplate.setAuditInfo(b, operationProperties.getImportProperties()));
                ImportReport result = importOperationResolver.executeImportOperation(operationProperties);
                finalReport.addOperations(result.getOperations());
                finalReport.addAllErrors(result.getErrorList());
                tempCount.set(0);
                beneficiaryTempList.clear();
                ImportUtils.writePercentage(finalCount.get(), operationProperties.getTotalElements(), operationProperties.getWriter());
            }
        }));
        return finalReport;
    }

    private Optional<Beneficiary> buildObject(BeneficiaryImportDTO importDTO, ImportReport importReport, Map<String, Object> persistedProperties, ImportOperation operation) {
        if (operation.equals(ImportOperation.CREATE_UPDATE))
            return buildCreateUpdateBeneficiary(importDTO, importReport, persistedProperties);
        return buildDisableBeneficiary(importDTO);
    }

    private Optional<Beneficiary> buildCreateUpdateBeneficiary(BeneficiaryImportDTO importDTO, ImportReport importReport, Map<String, Object> persistedProperties) {
        try {
            this.validateImportObject(importDTO);
            return Optional.of(importBuilderTemplate.buildBeneficiary(importDTO, persistedProperties));
        } catch (Exception e) {
            resolveException(e, importReport);
        }
        return Optional.empty();
    }

    private void resolveException(Exception e, ImportReport importReport) {
        String errorMessage = importErrorHandler.handleImportError(e);
        importReport.incrementErrors();
        importReport.addError(errorMessage);
    }

    private Optional<Beneficiary> buildDisableBeneficiary(BeneficiaryImportDTO importDTO) {
        try {
            String beneficiaryCode = importBuilderTemplate.resolveBeneficiaryCode(importDTO);
            if (beneficiaryCode.isEmpty())
                return Optional.empty();
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setBeneficiaryCode(beneficiaryCode);
            return Optional.of(beneficiary);
        } catch (ObjectNotValidException e) {
            return Optional.empty();
        }
    }

}
