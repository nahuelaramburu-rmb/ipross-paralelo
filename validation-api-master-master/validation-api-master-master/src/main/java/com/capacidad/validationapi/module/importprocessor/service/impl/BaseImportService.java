package com.capacidad.validationapi.module.importprocessor.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.importprocessor.model.ImportObject;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.importprocessor.service.ImportService;
import com.capacidad.validationapi.module.render.service.impl.CSVReaderWrapper;
import com.capacidad.validationapi.module.storage.service.impl.BaseFileValidator;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.SmartValidator;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.WHITESPACE;

@Getter
@Log4j2
public abstract class BaseImportService<T extends ImportObject> extends BaseFileValidator implements ImportService<T> {

    private final Map<String, Object> persistedProperties;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    @Qualifier("customLocaleValidator")
    private SmartValidator validator;
    @Autowired
    private LocaleHandler localeHandler;
    @Autowired
    private HikariDataSourcePoolMetadata processingDataSourcePoolMetadata;
    @Autowired
    private IdentityClientService tokenVerifier;

    public BaseImportService() {
        this.persistedProperties = new HashMap<>();
    }

    @Override
    public void importMultipartFile(ImportProperties properties) throws ObjectNotValidException {
        log.debug("({}) - HikariPool Idle Connections before importMultipartFile: {}", this.getClass(),
                this.getProcessingDataSourcePoolMetadata().getIdle());
        JWTAuthenticationToken authenticationToken = (JWTAuthenticationToken) properties.getAuthentication();
        this.getTokenVerifier().verify(authenticationToken.getToken());
    }

    @Override
    public void validateFile(MultipartFile file) throws ObjectNotValidException {
        super.validate(file, getApplicationProperties().getFileImportProcessorExtensionList(),
                Collections.emptyList());
    }

    @Override
    public void validateImportObject(T importObject) throws ObjectNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(importObject, importObject.getClass().getSimpleName());
        this.getValidator().validate(importObject, bindingResult);
        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                if (fieldError.getRejectedValue() != null) {
                    String defaultMessage = StringUtils.join(COMA, WHITESPACE, fieldError.getDefaultMessage());
                    String field = fieldError.getField();
                    String fieldMessage = localeHandler.getLocaleMessage(StringUtils.join("fields.", field)).orElse(field);
                    throw new ObjectNotValidException("base.invalidFieldValue", fieldMessage, defaultMessage);
                }
            }
        }
    }

    @Override
    public CSVReaderWrapper<T> buildCsvReader(ImportProperties properties) {
        return new CSVReaderWrapper<>(properties);
    }

    public IdentityClientService getTokenVerifier() {
        return this.tokenVerifier;
    }

    public HikariDataSourcePoolMetadata getProcessingDataSourcePoolMetadata() {
        return this.processingDataSourcePoolMetadata;
    }

}
