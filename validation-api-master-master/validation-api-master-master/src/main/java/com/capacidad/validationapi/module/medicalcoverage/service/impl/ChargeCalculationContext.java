package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.service.ChargeCalculationStrategy;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
public class ChargeCalculationContext implements ChargeCalculationStrategy {

    private ChargeCalculationStrategy chargeCalculationStrategy;

    private void clearContext() {
        this.setChargeCalculationStrategy(null);
    }

    @Override
    public void calculateAuthorizationItemCharge(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (chargeCalculationStrategy != null)
            chargeCalculationStrategy.calculateAuthorizationItemCharge(medicalCoverageItem, medicalAuthorizationItem);
        clearContext();
    }
}
