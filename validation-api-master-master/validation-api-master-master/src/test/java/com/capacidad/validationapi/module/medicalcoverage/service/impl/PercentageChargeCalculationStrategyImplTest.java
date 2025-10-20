package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class PercentageChargeCalculationStrategyImplTest {


    @Test
    public void testCalculateAuthorizationItemChargeReturnsValidPercentageValueHalfUp() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorizationItem.setUnitPrice(new BigDecimal(375));
        medicalCoverageItem.setChargeValue(new BigDecimal(5));

        //5 percent of 375 (18.75 int 18) multiply quantity 2 = 36

        PercentageChargeCalculationStrategyImpl percentageChargeCalculationStrategy = new PercentageChargeCalculationStrategyImpl();
        percentageChargeCalculationStrategy.calculateAuthorizationItemCharge(medicalCoverageItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(new BigDecimal("18"));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(new BigDecimal("36"));
    }

    @Test
    public void testCalculateAuthorizationItemChargeReturnsValidPercentageValueHalfDown() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorizationItem.setUnitPrice(new BigDecimal("412.98"));
        medicalCoverageItem.setChargeValue(new BigDecimal(10));

        //5 percent of 375 (18.75 int 18) multiply quantity 2 = 36

        PercentageChargeCalculationStrategyImpl percentageChargeCalculationStrategy = new PercentageChargeCalculationStrategyImpl();
        percentageChargeCalculationStrategy.calculateAuthorizationItemCharge(medicalCoverageItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(new BigDecimal("41"));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(new BigDecimal("82"));
    }

    @Test
    public void testCalculateAuthorizationItemChargeDoNotFailsWhenZeroValue() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorizationItem.setUnitPrice(new BigDecimal("412.98"));
        medicalCoverageItem.setChargeValue(new BigDecimal(0));

        PercentageChargeCalculationStrategyImpl percentageChargeCalculationStrategy = new PercentageChargeCalculationStrategyImpl();
        percentageChargeCalculationStrategy.calculateAuthorizationItemCharge(medicalCoverageItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(new BigDecimal(0));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(new BigDecimal(0));
    }

}
