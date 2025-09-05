package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.validationapi.module.medicalauthorization.model.*;

import java.util.List;

public interface RestrictionTypeValidator {

    void applyRestriction(Restriction restriction, MedicalAuthorizationItem medicalAuthorizationItem);

    void applyRestriction(Restriction restriction, MedicalAuthorization medicalAuthorization);

    Restriction buildRestriction(RestrictionType restrictionType, FailureType failureType, RestrictionMessage restrictionMessage);

    Failure buildFailure(RestrictionType restrictionType, FailureType failureType, RestrictionMessage restrictionMessage);

    RestrictionMessageExtra buildRestrictionMessageExtra(RestrictionMessageExtraType extraType, List<Object> data, String... messageParams);

    RestrictionMessage buildRestrictionMessage(String name, String allowed, String current, RestrictionMessageExtra extra);

}
