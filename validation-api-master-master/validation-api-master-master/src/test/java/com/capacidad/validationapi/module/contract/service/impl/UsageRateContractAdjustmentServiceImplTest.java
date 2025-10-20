package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.contract.model.UsageRateAdjustment;
import com.capacidad.validationapi.module.contract.repository.UsageRateAdjustmentRepository;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UsageRateContractAdjustmentServiceImplTest {

    @Mock
    private RestrictionTypeValidator restrictionTypeValidator;

    @Mock
    private UsageRateAdjustmentRepository usageRateAdjustmentRepository;

    @Mock
    private LocaleHandler localeHandler;

    @Mock
    private RegionService regionService;

    @Spy
    @InjectMocks
    private UsageRateAdjustmentServiceImpl usageRateAdjustmentService;

    @Test
    public void testBaseValidateFailsWhenContractAdjustmentContainsRegionAndCitySimultaneously() {
        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setRegion(new Region());
        contractAdjustment.setCity(new City());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> usageRateAdjustmentService.validate(contractAdjustment));

        assertThat(exception.getMessage()).isEqualTo("adjustment.regionOrCityRequired");
    }

    @Test
    public void testBaseValidateFailsWhenContractAdjustmentContainsRegionAndCityNullSimultaneously() {
        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setRegion(null);
        contractAdjustment.setCity(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> usageRateAdjustmentService.validate(contractAdjustment));

        assertThat(exception.getMessage()).isEqualTo("adjustment.regionOrCityRequired");
    }

    @Test
    public void testBaseValidateFailsWhenContractAdjustmentContainsRegionOnlyButAlreadyExist() throws ObjectNotFoundException {
        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setCity(null);

        Region region = new Region();
        region.setId(1L);
        region.getCities().add(new City());

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        Contract contract = new Contract();
        contract.setId(3L);

        contractAdjustment.setRegion(region);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setContract(contract);

        when(usageRateAdjustmentService.getRegionService()).thenReturn(regionService);
        when(regionService.findById(region.getId())).thenReturn(region);
        when(usageRateAdjustmentRepository.existsByRegionOrCity(nomenclator, contract, region.getCities()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> usageRateAdjustmentService.validate(contractAdjustment));

        assertThat(exception.getMessage()).isEqualTo("adjustment.nomenclatorRegionAlreadyExists");
    }

    @Test
    public void testBaseValidateFailsWhenContractAdjustmentContainsCityOnlyButAlreadyExist() {
        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setRegion(null);

        City city = new City();
        city.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        Contract contract = new Contract();
        contract.setId(3L);

        contractAdjustment.setCity(city);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setContract(contract);

        when(usageRateAdjustmentRepository.existsByRegionOrCity(nomenclator, contract, Collections.singleton(city)))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> usageRateAdjustmentService.validate(contractAdjustment));

        assertThat(exception.getMessage()).isEqualTo("adjustment.nomenclatorCityAlreadyExists");
    }

    @Test
    public void testBaseValidateDoNotFailsWhenContractAdjustmentContainsRegionOnlyAndDoesNotExist() throws ObjectNotValidException, ObjectNotFoundException {
        ContractAdjustment contractAdjustment = new ContractAdjustment();

        contractAdjustment.setCity(null);

        Region region = new Region();
        region.setId(1L);
        region.getCities().add(new City());

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        Contract contract = new Contract();
        contract.setId(3L);

        contractAdjustment.setRegion(region);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setContract(contract);

        when(usageRateAdjustmentService.getRegionService()).thenReturn(regionService);
        when(regionService.findById(region.getId())).thenReturn(region);
        when(usageRateAdjustmentRepository.existsByRegionOrCity(nomenclator, contract, region.getCities()))
                .thenReturn(false);

        usageRateAdjustmentService.validate(contractAdjustment);
    }

    @Test
    public void testBaseValidateDoNotFailsWhenContractAdjustmentContainsCityOnlyAndDoesNotExist() throws ObjectNotValidException, ObjectNotFoundException {
        ContractAdjustment contractAdjustment = new ContractAdjustment();

        contractAdjustment.setRegion(null);

        City city = new City();
        city.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        Contract contract = new Contract();
        contract.setId(3L);

        contractAdjustment.setCity(city);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setContract(contract);

        when(usageRateAdjustmentRepository.existsByRegionOrCity(nomenclator, contract, Collections.singleton(city)))
                .thenReturn(false);

        usageRateAdjustmentService.validate(contractAdjustment);
    }

    @Test
    public void testUsageRateAdjustmentAppliesWhenMonthlyCountBiggerThanThresholdMonthly() {
        UsageRateAdjustment usageRateAdjustment = new UsageRateAdjustment();
        usageRateAdjustment.setCapitaAmount(1000L);
        usageRateAdjustment.setThreshold(new BigDecimal("5.5"));
        usageRateAdjustment.setRestrictionType(new RestrictionType());
        usageRateAdjustment.setPeriod(Period.MONTHLY);
        usageRateAdjustment.setScope(ContractAdjustmentScope.CONTRACT);

        Contract contract = new Contract();
        contract.setName("myContract");

        usageRateAdjustment.setContract(contract);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        BigDecimal value = new BigDecimal(500L);
        BigDecimal currentValue = value.add(new BigDecimal(medicalAuthorizationItem.getQuantity()));

        when(usageRateAdjustmentService.getLocaleHandler()).thenReturn(localeHandler);
        when(usageRateAdjustmentService.getRestrictionTypeValidator()).thenReturn(restrictionTypeValidator);

        BigDecimal expectedValue = currentValue
                .divide(new BigDecimal(usageRateAdjustment.getCapitaAmount()), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(12));

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


        when(restrictionTypeValidator.buildRestrictionMessage(usageRateAdjustment.getClass().getSimpleName().toLowerCase(),
                usageRateAdjustment.getThreshold().toString(),
                expectedValue.toString(),
                expectedRestrictionMessageExtra)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                usageRateAdjustment.getRestrictionType(),
                FailureType.ADJUSTMENT,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        usageRateAdjustmentService.applyContractAdjustment(usageRateAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testUsageRateAdjustmentAppliesWhenYearlyCountBiggerThanThresholdMonthly() {
        UsageRateAdjustment usageRateAdjustment = new UsageRateAdjustment();
        usageRateAdjustment.setCapitaAmount(1000L);
        usageRateAdjustment.setThreshold(new BigDecimal("0.4"));
        usageRateAdjustment.setRestrictionType(new RestrictionType());
        usageRateAdjustment.setPeriod(Period.YEARLY);
        usageRateAdjustment.setScope(ContractAdjustmentScope.CONTRACT);

        Contract contract = new Contract();
        contract.setName("myContract");

        usageRateAdjustment.setContract(contract);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        BigDecimal value = new BigDecimal(500L);
        BigDecimal currentValue = value.add(new BigDecimal(medicalAuthorizationItem.getQuantity()));

        when(usageRateAdjustmentService.getLocaleHandler()).thenReturn(localeHandler);
        when(usageRateAdjustmentService.getRestrictionTypeValidator()).thenReturn(restrictionTypeValidator);

        BigDecimal expectedValue = currentValue
                .divide(new BigDecimal(usageRateAdjustment.getCapitaAmount()), 4, RoundingMode.HALF_UP);

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

        when(restrictionTypeValidator.buildRestrictionMessage(usageRateAdjustment.getClass().getSimpleName().toLowerCase(),
                usageRateAdjustment.getThreshold().toString(),
                expectedValue.toString(),
                expectedRestrictionMessageExtra)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                usageRateAdjustment.getRestrictionType(),
                FailureType.ADJUSTMENT,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        usageRateAdjustmentService.applyContractAdjustment(usageRateAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testUsageRateAdjustmentDoNotAppliesWhenMonthlyCountLessThanThresholdMonthly() {
        UsageRateAdjustment usageRateAdjustment = new UsageRateAdjustment();
        usageRateAdjustment.setCapitaAmount(1000L);
        usageRateAdjustment.setThreshold(new BigDecimal("12.5"));
        usageRateAdjustment.setRestrictionType(new RestrictionType());
        usageRateAdjustment.setPeriod(Period.MONTHLY);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        BigDecimal value = new BigDecimal(500L);

        usageRateAdjustmentService.applyContractAdjustment(usageRateAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testUsageRateAdjustmentDoNotAppliesWhenInvalidPeriod() {
        UsageRateAdjustment usageRateAdjustment = new UsageRateAdjustment();
        usageRateAdjustment.setCapitaAmount(1000L);
        usageRateAdjustment.setThreshold(new BigDecimal("12.5"));
        usageRateAdjustment.setRestrictionType(new RestrictionType());
        usageRateAdjustment.setPeriod(Period.DAILY);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        BigDecimal value = new BigDecimal(500L);

        usageRateAdjustmentService.applyContractAdjustment(usageRateAdjustment, value, medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

}
