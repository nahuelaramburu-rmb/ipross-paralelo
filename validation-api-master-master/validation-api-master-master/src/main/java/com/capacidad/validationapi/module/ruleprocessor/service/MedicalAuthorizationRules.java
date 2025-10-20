package com.capacidad.validationapi.module.ruleprocessor.service;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;

public interface MedicalAuthorizationRules {

    void maxAmountOfValidationItems(RuleConfiguration ruleConfiguration, MedicalAuthorization medicalAuthorization);

    void secureAuthorizationBeneficiaryAgeInARegion(RuleConfiguration ruleConfiguration, MedicalAuthorization medicalAuthorization);

}
