package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.service.ChargeCalculationStrategy;

import java.math.BigDecimal;


public class FreeChargeCalculationStrategyImpl implements ChargeCalculationStrategy {

    @Override
    public void calculateAuthorizationItemCharge(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(0));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal(0));
    }
}
