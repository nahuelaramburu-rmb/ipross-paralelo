package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.service.ChargeCalculationStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageChargeCalculationStrategyImpl implements ChargeCalculationStrategy {
    @Override
    public void calculateAuthorizationItemCharge(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        BigDecimal medicalAuthorizationItemUnitPrice = medicalAuthorizationItem.getUnitPrice();
        BigDecimal chargePercentage = medicalCoverageItem.getChargeValue();
        BigDecimal chargeUnitPrice = medicalAuthorizationItemUnitPrice.multiply(chargePercentage).divide(new BigDecimal(100), 0, RoundingMode.DOWN);
        medicalAuthorizationItem.setChargeUnitPrice(chargeUnitPrice);
        medicalAuthorizationItem.setChargeSubtotal(chargeUnitPrice.multiply(new BigDecimal(medicalAuthorizationItem.getQuantity())));
    }
}