package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.contract.model.MaximumAdjustment;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MaximumAdjustmentServiceImplTest {

    @Mock
    private RestrictionTypeValidator restrictionTypeValidator;

    @Mock
    private LocaleHandler localeHandler;

    @Spy
    @InjectMocks
    private MaximumAdjustmentServiceImpl maximumAdjustmentService;

    @Test
    public void testMaximumAdjustmentAppliesWhenCountBiggerThanThreshold() {
        MaximumAdjustment maximumAdjustment = new MaximumAdjustment();
        maximumAdjustment.setThreshold(40L);
        maximumAdjustment.setRestrictionType(new RestrictionType());
        maximumAdjustment.setPeriod(Period.MONTHLY);
        maximumAdjustment.setScope(ContractAdjustmentScope.CONTRACT);

        Contract contract = new Contract();
        contract.setName("myContract");

        maximumAdjustment.setContract(contract);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        BigDecimal value = new BigDecimal(50);
        BigDecimal currentValue = value.add(new BigDecimal(medicalAuthorizationItem.getQuantity()));

        when(maximumAdjustmentService.getLocaleHandler()).thenReturn(localeHandler);
        when(maximumAdjustmentService.getRestrictionTypeValidator()).thenReturn(restrictionTypeValidator);

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();
        RestrictionMessageExtra expectedRestrictionMessageExtra = new RestrictionMessageExtra();

        when(restrictionTypeValidator.buildRestrictionMessageExtra(any(RestrictionMessageExtraType.class),
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(expectedRestrictionMessageExtra);

        when(restrictionTypeValidator.buildRestrictionMessage(maximumAdjustment.getClass().getSimpleName().toLowerCase(),
                maximumAdjustment.getThreshold().toString(),
                currentValue.toString(),
                expectedRestrictionMessageExtra)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                maximumAdjustment.getRestrictionType(),
                FailureType.ADJUSTMENT,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        maximumAdjustmentService.applyContractAdjustment(maximumAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testMaximumAdjustmentDoNotAppliesWhenCountLessThanThreshold() {
        MaximumAdjustment maximumAdjustment = new MaximumAdjustment();
        maximumAdjustment.setThreshold(50L);
        maximumAdjustment.setRestrictionType(new RestrictionType());
        BigDecimal value = new BigDecimal(40);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        maximumAdjustmentService.applyContractAdjustment(maximumAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

}
