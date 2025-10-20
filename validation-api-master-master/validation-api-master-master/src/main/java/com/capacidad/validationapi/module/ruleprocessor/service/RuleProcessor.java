package com.capacidad.validationapi.module.ruleprocessor.service;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

public interface RuleProcessor {

    void applyMedicalAuthorizationRules(MedicalAuthorization medicalAuthorization);

    void applyMedicalAuthorizationItemRules(MedicalAuthorizationItem medicalAuthorizationItem);

}
