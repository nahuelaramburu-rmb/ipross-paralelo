package com.capacidad.validationapi.module.ruleprocessor.service;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;

public interface MedicalAuthorizationItemRules {

    void beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod(RuleConfiguration ruleConfiguration, MedicalAuthorizationItem medicalAuthorizationItem);

    void timedNomenclators(RuleConfiguration ruleConfiguration, MedicalAuthorizationItem medicalAuthorizationItem);

}
