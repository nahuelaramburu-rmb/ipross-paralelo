package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class FixedChargeCalculationStrategyImplTest {

    @Test
    public void testCalculateAuthorizationItemChargeReturnsValidFixedValue() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        medicalCoverageItem.setChargeValue(new BigDecimal("80.93"));

        //80.93 multiply quantity 2 is 161.86

        FixedChargeCalculationStrategyImpl fixedChargeCalculationStrategy = new FixedChargeCalculationStrategyImpl();
        fixedChargeCalculationStrategy.calculateAuthorizationItemCharge(medicalCoverageItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(new BigDecimal("80.93").setScale(0, RoundingMode.HALF_DOWN));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(new BigDecimal("161.86").setScale(0, RoundingMode.HALF_DOWN));
    }

    @Test
    public void testCalculateAuthorizationItemChargeDoNotFailsWhenZeroValue() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        medicalCoverageItem.setChargeValue(new BigDecimal(0));

        FixedChargeCalculationStrategyImpl fixedChargeCalculationStrategy = new FixedChargeCalculationStrategyImpl();
        fixedChargeCalculationStrategy.calculateAuthorizationItemCharge(medicalCoverageItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(new BigDecimal(0));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(new BigDecimal(0));
    }

}
