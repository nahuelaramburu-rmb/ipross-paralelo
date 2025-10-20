package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.medicalcoverage.model.ChargeType;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.reference.ChargeTypeReference;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import com.capacidad.validationapi.module.medicalcoverage.repository.MedicalCoverageRepository;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageItemService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.person.model.Gender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class MedicalCoverageServiceImplTest {

    @Mock
    private RegionService regionService;

    @Mock
    private MedicalCoverageItemService medicalCoverageItemService;

    @Mock
    private ChargeCalculationContext chargeCalculationContext;

    @Mock
    private MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Mock
    private MedicalCoverageRepository medicalCoverageRepository;


    @Mock
    private RestrictionTypeValidator restrictionTypeValidator;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private MedicalCoverageServiceImpl medicalCoverageService;

    @Test
    public void testValidateFailsWhenRegionAndCitySpecified() {
        MedicalCoverage medicalCoverage = new MedicalCoverage();

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        medicalCoverage.setRegion(new Region());
        medicalCoverage.setCity(new City());

        medicalCoverage.setInsurancePlan(insurancePlan);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageService.validate(medicalCoverage));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.regionOrCity");
    }

    @Test
    public void testValidateFailsWhenNoRegionNorCitySpecified() {
        MedicalCoverage medicalCoverage = new MedicalCoverage();

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        medicalCoverage.setRegion(null);
        medicalCoverage.setCity(null);

        medicalCoverage.setInsurancePlan(insurancePlan);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageService.validate(medicalCoverage));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.regionOrCity");
    }

    @Test
    public void testValidateFailsWhenCoverageAlreadyExistForCity() {
        MedicalCoverage medicalCoverage = new MedicalCoverage();

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        City city = new City();
        city.setId(1L);

        medicalCoverage.setCity(city);

        medicalCoverage.setInsurancePlan(insurancePlan);

        when(medicalCoverageRepository.existsByCityOrRegion(insurancePlan, Collections.singleton(city)))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> medicalCoverageService.validate(medicalCoverage));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.cityAlreadyExists");
    }

    @Test
    public void testValidateDoNotFailsWhenCoverageDoesNotExistsForCity() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalCoverage medicalCoverage = new MedicalCoverage();

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        City city = new City();
        city.setId(1L);

        medicalCoverage.setCity(city);

        medicalCoverage.setInsurancePlan(insurancePlan);

        when(medicalCoverageRepository.existsByCityOrRegion(insurancePlan, Collections.singleton(city)))
                .thenReturn(false);

        medicalCoverageService.validate(medicalCoverage);
    }

    @Test
    public void testValidateFailsWhenCoverageAlreadyExistForRegion() throws ObjectNotFoundException {
        MedicalCoverage medicalCoverage = new MedicalCoverage();

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        Region region = new Region();
        region.setId(1L);

        City city1 = new City();
        city1.setId(1L);
        City city2 = new City();
        city2.setId(2L);

        region.getCities().add(city1);
        region.getCities().add(city2);

        medicalCoverage.setRegion(region);

        medicalCoverage.setInsurancePlan(insurancePlan);

        when(regionService.findById(region.getId())).thenReturn(region);
        when(medicalCoverageRepository.existsByCityOrRegion(insurancePlan, region.getCities()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> medicalCoverageService.validate(medicalCoverage));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.alreadyExistsInRegion");
    }

    @Test
    public void testValidateUpdateFailsWhenRegionInUse() throws ObjectNotFoundException {
        Region region = new Region();
        region.setId(1L);

        City city = new City();
        city.setId(1L);

        region.getCities().add(city);

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);
        medicalCoverage.setRegion(region);
        medicalCoverage.setInsurancePlan(insurancePlan);

        when(regionService.findById(region.getId())).thenReturn(region);
        when(medicalCoverageRepository.existsByCityOrRegionAndIdIsNot(insurancePlan, region.getCities(),
                medicalCoverage.getId()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> medicalCoverageService.validateUpdate(medicalCoverage));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.alreadyExistsInRegion");
    }

    @Test
    public void testValidateUpdateDoNotFailWhenRegionNotInUse() throws ObjectNotFoundException, ObjectNotValidException {
        Region region = new Region();
        region.setId(1L);

        City city = new City();
        city.setId(1L);

        region.getCities().add(city);
        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);
        medicalCoverage.setRegion(region);
        medicalCoverage.setInsurancePlan(insurancePlan);

        when(regionService.findById(region.getId())).thenReturn(region);
        when(medicalCoverageRepository.existsByCityOrRegionAndIdIsNot(insurancePlan, region.getCities(),
                medicalCoverage.getId()))
                .thenReturn(false);

        medicalCoverageService.validateUpdate(medicalCoverage);
    }

    @Test
    public void testValidateUpdateFailsWhenCityInUse() {
        City city = new City();
        city.setId(1L);

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);
        medicalCoverage.setCity(city);
        medicalCoverage.setInsurancePlan(insurancePlan);

        when(medicalCoverageRepository.existsByCityOrRegionAndIdIsNot(insurancePlan, Collections.singleton(city),
                medicalCoverage.getId()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> medicalCoverageService.validateUpdate(medicalCoverage));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.cityAlreadyExists");
    }

    @Test
    public void testValidateUpdateDoNotFailWhenCityNotInUse() throws ObjectNotFoundException, ObjectNotValidException {
        City city = new City();
        city.setId(1L);

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);
        medicalCoverage.setCity(city);
        medicalCoverage.setInsurancePlan(insurancePlan);

        when(medicalCoverageRepository.existsByCityOrRegionAndIdIsNot(insurancePlan, Collections.singleton(city),
                medicalCoverage.getId())).thenReturn(false);

        medicalCoverageService.validateUpdate(medicalCoverage);
    }

    @Test
    public void testValidateDoNotFailsWhenCoverageDoesNotExistForRegion() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalCoverage medicalCoverage = new MedicalCoverage();

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        Region region = new Region();
        region.setId(1L);

        City city1 = new City();
        city1.setId(1L);
        City city2 = new City();
        city2.setId(2L);

        region.getCities().add(city1);
        region.getCities().add(city2);

        medicalCoverage.setRegion(region);

        medicalCoverage.setInsurancePlan(insurancePlan);

        when(regionService.findById(region.getId())).thenReturn(region);
        when(medicalCoverageRepository.existsByCityOrRegion(insurancePlan, region.getCities())).thenReturn(false);

        medicalCoverageService.validate(medicalCoverage);
    }

    @Test
    public void testFindApplicableContractFailsWhenSinglePlanAndCoverageDoesNotExist() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(medicalCoverageRepository.findByInsurancePlanIdAndCityOrRegion(insurancePlan, Collections.singleton(city))).thenReturn(Optional.empty());

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> medicalCoverageService.findApplicableCoverage(medicalAuthorizationItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.applicableCoverageNotFound");
    }

    @Test
    public void testFindApplicableCoverageFailsWhenSinglePlanAndCoverageItemDoesNotExist() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(medicalCoverageRepository.findByInsurancePlanIdAndCityOrRegion(insurancePlan, Collections.singleton(city))).thenReturn(Optional.of(medicalCoverage));
        when(medicalCoverageItemService.findMedicalCoverageItem(medicalCoverage.getId(), nomenclator.getId())).thenReturn(Optional.empty());

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> medicalCoverageService.findApplicableCoverage(medicalAuthorizationItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverage.applicableCoverageNotFound");
    }

    @Test
    public void testApplyMedicalCoverageFailsWhenAuditRequiredAndAuthorizationApproved() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.FEMENINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.INDISTINTO);
        medicalCoverageItem.setAuditRequired(true);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAgeTo(200);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(medicalAuthorizationItem));
        when(restrictionTypeValidator.buildRestrictionMessage("validateAuditRequired",
                "-",
                "-",
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                any(RestrictionType.class),
                any(FailureType.class),
                any(RestrictionMessage.class))).thenReturn(expectedRestriction);

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testApplyMedicalCoverageToItemHasNoFailuresWhenAnyGender() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.FEMENINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.INDISTINTO);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAgeTo(200);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(medicalAuthorizationItem));

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testApplyMedicalCoverageToItemHasNoFailuresWhenMaleGender() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAgeTo(200);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(medicalAuthorizationItem));

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testApplyMedicalCoverageToItemHasNoFailuresWhenAgeFromNull() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setAgeTo(200);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(medicalAuthorizationItem));

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testApplyMedicalCoverageToItemHasNoFailuresWhenFixedMaxQuantityNull() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testApplyMedicalCoverageToItemHasNoFailuresWhenFixedMaxDaysNull() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testApplyMedicalCoverageToItemFailsOnInvalidGender() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.FEMENINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        medicalCoverageItem.setRestrictionType(restrictionType);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("validateGender",
                medicalCoverageItem.getGender().toString(),
                beneficiary.getGender().toString(),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.MEDICAL_COVERAGE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testApplyMedicalCoverageToItemFailsOnFixedQuantity() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        medicalCoverageItem.setRestrictionType(restrictionType);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        MedicalAuthorization medicalAuthorization1 = new MedicalAuthorization();
        medicalAuthorization1.setId(2L);
        MedicalAuthorization medicalAuthorization2 = new MedicalAuthorization();
        medicalAuthorization2.setId(3L);

        List<MedicalAuthorizationItem> medicalAuthorizationItems = new ArrayList<>();
        medicalAuthorizationItems.add(medicalAuthorizationItem);

        MedicalAuthorizationItem medicalAuthorizationItem1 = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization3 = new MedicalAuthorization();
        medicalAuthorization3.setId(3L);
        medicalAuthorizationItem1.setMedicalAuthorization(medicalAuthorization3);

        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization4 = new MedicalAuthorization();
        medicalAuthorization4.setId(4L);
        medicalAuthorizationItem2.setMedicalAuthorization(medicalAuthorization4);

        medicalAuthorizationItems.add(medicalAuthorizationItem1);
        medicalAuthorizationItems.add(medicalAuthorizationItem2);


        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(medicalAuthorizationItems);

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("validateFixedQuantity",
                medicalCoverageItem.getFixedMaxQuantity().toString(),
                String.valueOf(medicalAuthorizationItems.size() + medicalAuthorizationItem.getQuantity()),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.MEDICAL_COVERAGE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testApplyMedicalCoverageToItemFailsOnAgeFrom() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setAgeFrom(50);
        medicalCoverageItem.setAgeTo(80);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        medicalCoverageItem.setRestrictionType(restrictionType);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        int beneficiaryAge = Period.between(beneficiary.getBirthDate(), LocalDate.now()).getYears();

        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(medicalAuthorizationItem));

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("validateAge",
                String.format("%d - %d", medicalCoverageItem.getAgeFrom(), medicalCoverageItem.getAgeTo()),
                String.valueOf(beneficiaryAge),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.MEDICAL_COVERAGE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);


        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testApplyMedicalCoverageToItemFailsOnAgeTo() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setAgeFrom(10);
        medicalCoverageItem.setAgeTo(20);
        medicalCoverageItem.setAwaitDays(0);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        medicalCoverageItem.setRestrictionType(restrictionType);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        int beneficiaryAge = Period.between(beneficiary.getBirthDate(), LocalDate.now()).getYears();

        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(medicalAuthorizationItem));

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("validateAge",
                String.format("%d - %d", medicalCoverageItem.getAgeFrom(), medicalCoverageItem.getAgeTo()),
                String.valueOf(beneficiaryAge),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.MEDICAL_COVERAGE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testApplyMedicalCoverageToItemFailsOnAwaitDays() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setGender(Gender.MASCULINO);
        beneficiary.setBirthDate(LocalDate.now().minusYears(21));
        beneficiary.setCreatedAt(LocalDateTime.now());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(insurancePlan);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);

        City city = new City();
        city.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setCity(city);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setGender(Gender.MASCULINO);
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(2);
        medicalCoverageItem.setAgeFrom(0);
        medicalCoverageItem.setAgeTo(200);
        medicalCoverageItem.setAwaitDays(1);
        medicalCoverageItem.setMedicalCoverage(medicalCoverage);

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.AUDIT.getId());

        medicalCoverageItem.setRestrictionType(restrictionType);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        LocalDate awaitDays = beneficiary.getCreatedAt().toLocalDate().plusDays(medicalCoverageItem.getAwaitDays());

        when(medicalAuthorizationItemService.getBeneficiaryAuthorizationItemAmountInPeriod(any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(medicalAuthorizationItem));

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("validateAwaitDays",
                medicalCoverageItem.getAwaitDays().toString(),
                awaitDays.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.MEDICAL_COVERAGE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testCalculateAuthorizationItemChargesExecutePercentageStrategyWhenChargeTypeIsPercentage() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        ChargeType chargeType = new ChargeType();
        chargeType.setId(ChargeTypeReference.PERCENTAGE.getId());
        medicalCoverageItem.setChargeType(chargeType);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        medicalCoverageService.calculateAuthorizationItemCharges(medicalAuthorizationItem);

        verify(chargeCalculationContext, times(1)).setChargeCalculationStrategy(any(PercentageChargeCalculationStrategyImpl.class));
    }

    @Test
    public void testCalculateAuthorizationItemChargesExecuteFixedStrategyWhenChargeTypeIsFixedValue() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        ChargeType chargeType = new ChargeType();
        chargeType.setId(ChargeTypeReference.FIXED_VALUE.getId());
        medicalCoverageItem.setChargeType(chargeType);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        medicalCoverageService.calculateAuthorizationItemCharges(medicalAuthorizationItem);

        verify(chargeCalculationContext, times(1)).setChargeCalculationStrategy(any(FixedChargeCalculationStrategyImpl.class));
    }

    @Test
    public void testCalculateAuthorizationItemChargesExecuteFreeStrategyWhenCoverageHasFreePractices() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        int quantity = 1;
        int freeDays = 30;
        medicalCoverageItem.setFreeMaxQuantity(quantity);
        medicalCoverageItem.setFreeMaxDays(freeDays);


        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);


        medicalCoverageService.calculateAuthorizationItemCharges(medicalAuthorizationItem);

        verify(chargeCalculationContext, times(1)).setChargeCalculationStrategy(any(FreeChargeCalculationStrategyImpl.class));
    }

    @Test
    public void testCalculateAuthorizationItemChargesDoNotExecuteFreeStrategyWhenCoverageHasExhaustedFreePractices() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        int quantity = 1;
        int freeDays = 30;
        medicalCoverageItem.setFreeMaxQuantity(quantity);
        medicalCoverageItem.setFreeMaxDays(freeDays);
        medicalCoverageItem.setChargeType(ChargeTypeReference.FIXED_VALUE.getInstance());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalCoverageItem(medicalCoverageItem);

        when(medicalAuthorizationItemService.countBeneficiaryFreeAuthorizationItemAmountInPeriod(medicalAuthorizationItem, freeDays))
                .thenReturn(1);

        medicalCoverageService.calculateAuthorizationItemCharges(medicalAuthorizationItem);

        verify(chargeCalculationContext, times(1)).setChargeCalculationStrategy(any(FixedChargeCalculationStrategyImpl.class));
    }

    @Test
    public void testDeleteExecutesSuccessfully() throws ObjectNotFoundException {
        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setDeleted(true);
        MedicalCoverageItem medicalCoverageItem1 = new MedicalCoverageItem();
        medicalCoverageItem1.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());
        MedicalCoverageItem medicalCoverageItem2 = new MedicalCoverageItem();

        medicalCoverage.getMedicalCoverageItems().add(medicalCoverageItem2);
        medicalCoverage.getMedicalCoverageItems().add(medicalCoverageItem1);
        medicalCoverage.getMedicalCoverageItems().add(medicalCoverageItem);

        doReturn(medicalCoverage).when(medicalCoverageService).findById(medicalCoverage.getId());
        doReturn(new ObjectMapper()).when(medicalCoverageService).getObjectMapper();
        doReturn(applicationEventPublisher).when(medicalCoverageService).getApplicationEventPublisher();

        JsonNode result = medicalCoverageService.delete(medicalCoverage.getId());

        assertThat(result.get("id").asLong()).isEqualTo(medicalCoverage.getId());
        assertThat(medicalCoverage.getDeleted()).isTrue();
        assertThat(medicalCoverage.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(medicalCoverage.getMedicalCoverageItems()).hasSize(1);
        MedicalCoverageItem itemResult = medicalCoverage.getMedicalCoverageItems().iterator().next();
        assertThat(itemResult.getDeleted()).isTrue();
        assertThat(itemResult.getDeletionToken()).isEqualTo(medicalCoverage.getDeletionToken());

        verify(medicalCoverageRepository, Mockito.times(1)).save(medicalCoverage);
        verify(applicationEventPublisher, Mockito.times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

}
