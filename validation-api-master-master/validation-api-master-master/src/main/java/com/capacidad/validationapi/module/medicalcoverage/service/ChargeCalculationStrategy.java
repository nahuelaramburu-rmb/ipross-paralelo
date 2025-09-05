package com.capacidad.validationapi.module.medicalcoverage.service;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;

public interface ChargeCalculationStrategy {

    void calculateAuthorizationItemCharge(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem);

}
