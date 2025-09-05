package com.capacidad.validationapi.module.batch.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.batch.dto.BatchItemDTO;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.projection.BatchItemProjection;
import com.capacidad.validationapi.module.batch.repository.BatchItemRepository;
import com.capacidad.validationapi.module.batch.service.BatchItemService;
import com.capacidad.validationapi.module.batch.service.BatchItemSupportService;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationValidator;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.WHITESPACE;
import static com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference.REJECTION;

@Log4j2
@Service
public class BatchItemServiceImpl extends BaseServiceImpl<BatchItem, BatchItemDTO, Long> implements BatchItemService {

    private final BatchItemRepository batchItemRepository;
    private final MedicalAuthorizationItemService medicalAuthorizationItemService;
    private final PractitionerService practitionerService;
    private final MedicalAuthorizationValidator medicalAuthorizationValidator;
    private final RestrictionTypeValidator restrictionTypeValidator;
    private final BatchItemSupportService batchItemSupportService;

    @Autowired
    public BatchItemServiceImpl(BatchItemRepository repository,
                                MedicalAuthorizationItemService medicalAuthorizationItemService,
                                PractitionerService practitionerService,
                                MedicalAuthorizationValidator medicalAuthorizationValidator,
                                RestrictionTypeValidator restrictionTypeValidator,
                                BatchItemSupportService batchItemSupportService) {
        super(repository);
        this.batchItemRepository = repository;
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
        this.practitionerService = practitionerService;
        this.medicalAuthorizationValidator = medicalAuthorizationValidator;
        this.restrictionTypeValidator = restrictionTypeValidator;
        this.batchItemSupportService = batchItemSupportService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchItemProjection create(BatchItemDTO input, Batch batch) throws ObjectNotValidException, ObjectNotFoundException {
        BatchItem object = this.mapDtoToInput(input);
        object.setBatch(batch);
        this.validate(object);
        BatchItem result = batchItemRepository.saveAndFlush(object);
        batchItemRepository.refresh(result);
        return this.getProjectionFactory().createProjection(BatchItemProjection.class, result);
    }

    @Override
    public void validate(BatchItem batchItem) throws ObjectNotValidException, ObjectNotFoundException {
        Set<Practitioner> practitioners = batchItem.getPractitioners();
        if (practitioners != null && !batchItem.getPractitioners().isEmpty())
            validateAssociatedPractitioner(batchItem.getPractitioners(), batchItem.getNomenclator());
    }

    private void validateAssociatedPractitioner(Set<Practitioner> practitioners, Nomenclator nomenclator) throws ObjectNotValidException, ObjectNotFoundException {
        for (Practitioner practitioner : practitioners) {
            practitioner = practitionerService.findById(practitioner.getId());
            medicalAuthorizationValidator.validatePractitionerStatus(practitioner);
            medicalAuthorizationValidator.getValidatedNomenclator(practitioner, nomenclator.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <P extends BaseProjection<Long>> EntityModel<P> update(Map<String, Object> objectMap, Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.update(objectMap, objectId);
    }

    @Override
    public Optional<BatchItem> findApplicableBatchItem(Batch batch, MedicalAuthorizationItem medicalAuthorizationItem) {
        return batchItemRepository.findByBatchIdAndNomenclatorId(batch.getId(), medicalAuthorizationItem.getNomenclator().getId());
    }

    @Override
    public long findBatchItemAndGetParentId(long batchItemId) throws ObjectNotFoundException {
        return batchItemRepository.findBatchIdProjectionById(batchItemId)
                .map(i -> i.getBatch().getId())
                .orElseThrow(() -> new ObjectNotFoundException("batchItem.notFound"));
    }

    @Override
    public Optional<BatchItem> applyBatchItemCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        Optional<BatchItem> optionalBatchItem = Optional.ofNullable(medicalAuthorizationItem.getBatchItem());
        if (optionalBatchItem.isPresent()) {
            BatchItem batchItem = optionalBatchItem.get();
            applyBatchItemCoverage(batchItem, medicalAuthorizationItem);
            return optionalBatchItem;
        }
        return Optional.empty();
    }

    private void applyBatchItemCoverage(BatchItem batchItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (!batchItem.getMedicalCenters().isEmpty())
            validateMedicalCenter(batchItem, medicalAuthorizationItem);
        if (!batchItem.getPractitioners().isEmpty())
            validatePractitioner(batchItem, medicalAuthorizationItem);
        validatePeriod(batchItem, medicalAuthorizationItem);
    }

    private void validateMedicalCenter(BatchItem batchItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        MedicalCenter medicalCenter = medicalAuthorizationItem.getMedicalAuthorization().getMedicalCenter();
        Set<Long> medicalCenterIds = batchItem.getMedicalCenters().stream()
                .map(MedicalCenter::getId)
                .collect(Collectors.toSet());
        if (!medicalCenterIds.contains(medicalCenter.getId()))
            buildAndAddFailure(medicalAuthorizationItem,
                    "invalidMedicalCenter",
                    medicalCenter.getName());
    }

    private void validatePractitioner(BatchItem batchItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        Practitioner authorizationPractitioner = medicalAuthorizationItem.getMedicalAuthorization().getPractitioner();
        Set<Long> practitionerIds = batchItem.getPractitioners().stream()
                .map(Practitioner::getId)
                .collect(Collectors.toSet());
        if (!practitionerIds.contains(authorizationPractitioner.getId()))
            buildAndAddFailure(medicalAuthorizationItem,
                    "invalidPractitioner",
                    StringUtils.join(authorizationPractitioner.getLastName(), COMA, WHITESPACE, authorizationPractitioner.getName()));

    }

    private void validatePeriod(BatchItem batchItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (batchItem.getPeriod() != null) {
            long count = medicalAuthorizationItemService.countAllByBatchItemInPeriod(batchItem);
            if (count > batchItem.getAmount()) {
                LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(batchItem.getPeriod());
                RestrictionMessageExtra extra = buildValidatePeriodExtra(dateTimeFrom);
                RestrictionMessage message = restrictionTypeValidator.buildRestrictionMessage("limitExceeded",
                        batchItem.getAmount().toString(),
                        String.valueOf(count),
                        extra);
                Failure failure = restrictionTypeValidator.buildFailure(REJECTION.getInstance(), FailureType.BATCH, message);
                medicalAuthorizationItem.getFailures().add(failure);
            }
        }
    }

    private void buildAndAddFailure(MedicalAuthorizationItem medicalAuthorizationItem, String name, String current) {
        RestrictionMessage message = restrictionTypeValidator.buildRestrictionMessage(name,
                "",
                current,
                null);
        Failure failure = restrictionTypeValidator.buildFailure(REJECTION.getInstance(), FailureType.BATCH, message);
        medicalAuthorizationItem.getFailures().add(failure);
    }

    private RestrictionMessageExtra buildValidatePeriodExtra(LocalDateTime from) {
        return restrictionTypeValidator.buildRestrictionMessageExtra(RestrictionMessageExtraType.AUTHORIZATION_ID,
                Collections.emptyList(),
                from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    @Override
    public JsonNode delete(Long id) throws ObjectNotFoundException {
        BatchItem batchItem = this.findById(id);
        return batchItemSupportService.delete(batchItem);
    }

}
