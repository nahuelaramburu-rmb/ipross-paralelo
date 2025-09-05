package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.service.ChargeCalculationStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedChargeCalculationStrategyImpl implements ChargeCalculationStrategy {
    @Override
    public void calculateAuthorizationItemCharge(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        BigDecimal chargeUnitPrice = medicalCoverageItem.getChargeValue();
        medicalAuthorizationItem.setChargeUnitPrice(chargeUnitPrice.setScale(0, RoundingMode.HALF_DOWN));
        medicalAuthorizationItem.setChargeSubtotal(chargeUnitPrice.multiply(new BigDecimal(medicalAuthorizationItem.getQuantity())).setScale(0, RoundingMode.HALF_DOWN));
    }
}
