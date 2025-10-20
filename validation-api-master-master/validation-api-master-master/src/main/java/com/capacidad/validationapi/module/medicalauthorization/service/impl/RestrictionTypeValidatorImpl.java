package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_PENDING;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_REJECTED;

@Component
public class RestrictionTypeValidatorImpl implements RestrictionTypeValidator {

    private final Utils utils;
    private final LocaleHandler localeHandler;
    private final ObjectMapper objectMapper;

    @Autowired
    public RestrictionTypeValidatorImpl(Utils utils,
                                        LocaleHandler localeHandler,
                                        ObjectMapper objectMapper) {
        this.utils = utils;
        this.localeHandler = localeHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    public void applyRestriction(Restriction restriction, MedicalAuthorizationItem medicalAuthorizationItem) {
        applyRestrictionStatus(restriction.getRestrictionType(), medicalAuthorizationItem);
        Failure failure = buildFailure(restriction);
        medicalAuthorizationItem.getFailures().add(failure);
    }

    @Override
    public void applyRestriction(Restriction restriction, MedicalAuthorization medicalAuthorization) {
        RestrictionType restrictionType = restriction.getRestrictionType();
        medicalAuthorization.getMedicalAuthorizationItems().forEach(i -> applyRestrictionStatus(restrictionType, i));
        Failure failure = buildFailure(restriction);
        medicalAuthorization.getFailures().add(failure);
    }

    @Override
    public Restriction buildRestriction(RestrictionType restrictionType, FailureType failureType, RestrictionMessage restrictionMessage) {
        return new Restriction(restrictionType, failureType, restrictionMessage);
    }

    @Override
    public Failure buildFailure(RestrictionType restrictionType, FailureType failureType, RestrictionMessage restrictionMessage) {
        Restriction restriction = new Restriction(restrictionType, failureType, restrictionMessage);
        return buildFailure(restriction);
    }

    @Override
    public RestrictionMessageExtra buildRestrictionMessageExtra(RestrictionMessageExtraType extraType, List<Object> data, String... messageParams) {
        return new RestrictionMessageExtra(extraType, data, messageParams);
    }

    @Override
    public RestrictionMessage buildRestrictionMessage(String name, String allowed, String current, RestrictionMessageExtra extra) {
        return new RestrictionMessage(name, allowed, current, extra);
    }

    private void applyRestrictionStatus(RestrictionType restrictionType, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (restrictionType != null) {
            if (restrictionType.getId().equals(RestrictionTypeReference.REJECTION.getId()))
                medicalAuthorizationItem.setStatus(utils.getGenericsEntityReference(Status.class, VALIDATION_REJECTED.getId()));
            if (restrictionType.getId().equals(RestrictionTypeReference.AUDIT.getId())
                    && (medicalAuthorizationItem.getStatus() == null || !medicalAuthorizationItem.getStatus().getId().equals(VALIDATION_REJECTED.getId())))
                medicalAuthorizationItem.setStatus(utils.getGenericsEntityReference(Status.class, VALIDATION_PENDING.getId()));
        }
    }


    private Failure buildFailure(Restriction restriction) {
        Failure failure = new Failure();
        failure.setFailureType(restriction.getFailureType());
        String namePrefix = restriction.getFailureType().name().toLowerCase();
        String localeName = localeHandler.getLocaleMessage(StringUtils.join(namePrefix, DOT, restriction.getName()), restriction.getAllowed(), restriction.getCurrent()).orElse(restriction.getName());
        failure.setName(localeName);
        failure.setAllowed(restriction.getAllowed());
        failure.setCurrent(restriction.getCurrent());
        buildAndSetExtra(restriction, failure, namePrefix);
        return failure;
    }

    private void buildAndSetExtra(Restriction restriction, Failure failure, String prefix) {
        if (restriction.getExtra() != null) {
            RestrictionMessageExtra extra = restriction.getExtra();
            String extraMessage = localeHandler.getLocaleMessage(StringUtils.join(prefix, DOT, "extra", DOT, restriction.getName()), extra.getMessageParams()).orElse("");
            extra.setMessage(extraMessage);
            failure.setExtra(objectMapper.valueToTree(extra));
        }
    }

}
