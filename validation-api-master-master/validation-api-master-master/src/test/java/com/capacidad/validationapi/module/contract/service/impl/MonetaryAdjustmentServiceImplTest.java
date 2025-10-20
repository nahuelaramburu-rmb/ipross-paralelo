package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.contract.model.MonetaryAdjustment;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MonetaryAdjustmentServiceImplTest {

    @Mock
    private RestrictionTypeValidator restrictionTypeValidator;

    @Mock
    private LocaleHandler localeHandler;

    @Spy
    @InjectMocks
    private MonetaryAdjustmentServiceImpl monetaryAdjustmentService;

    @Test
    public void testMonetaryAdjustmentAppliesWhenCountBiggerThanThreshold() {
        MonetaryAdjustment monetaryAdjustment = new MonetaryAdjustment();
        monetaryAdjustment.setThreshold(new BigDecimal(2578));
        monetaryAdjustment.setRestrictionType(new RestrictionType());
        monetaryAdjustment.setPeriod(Period.MONTHLY);
        monetaryAdjustment.setScope(ContractAdjustmentScope.CONTRACT);

        Contract contract = new Contract();
        contract.setName("myContract");

        monetaryAdjustment.setContract(contract);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setSubtotal(new BigDecimal(100));

        BigDecimal value = new BigDecimal(3800);
        BigDecimal currentValue = value.add(medicalAuthorizationItem.getSubtotal());

        when(monetaryAdjustmentService.getLocaleHandler()).thenReturn(localeHandler);
        when(monetaryAdjustmentService.getRestrictionTypeValidator()).thenReturn(restrictionTypeValidator);

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

        when(restrictionTypeValidator.buildRestrictionMessage(monetaryAdjustment.getClass().getSimpleName().toLowerCase(),
                StringUtils.join("$", monetaryAdjustment.getThreshold().toString()),
                StringUtils.join("$", currentValue.toString()),
                expectedRestrictionMessageExtra)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                monetaryAdjustment.getRestrictionType(),
                FailureType.ADJUSTMENT,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        monetaryAdjustmentService.applyContractAdjustment(monetaryAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testMonetaryAdjustmentDoNotAppliesWhenCountLessThanThreshold() {
        MonetaryAdjustment monetaryAdjustment = new MonetaryAdjustment();
        monetaryAdjustment.setThreshold(new BigDecimal(2578));
        monetaryAdjustment.setRestrictionType(new RestrictionType());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setSubtotal(new BigDecimal(150));

        BigDecimal value = new BigDecimal(1434);

        monetaryAdjustmentService.applyContractAdjustment(monetaryAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

}
