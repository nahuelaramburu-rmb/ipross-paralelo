package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.contract.model.*;
import com.capacidad.validationapi.module.contract.repository.ContractAdjustmentRepository;
import com.capacidad.validationapi.module.contract.service.MaximumAdjustmentService;
import com.capacidad.validationapi.module.contract.service.MonetaryAdjustmentService;
import com.capacidad.validationapi.module.contract.service.UsageRateAdjustmentService;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationCondition;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContractAdjustmentServiceImplTest {

    @Mock
    private ContractAdjustmentRepository contractAdjustmentRepository;

    @Mock
    private UsageRateAdjustmentService usageRateAdjustmentService;

    @Mock
    private MaximumAdjustmentService maximumAdjustmentService;

    @Mock
    private MonetaryAdjustmentService monetaryAdjustmentService;

    @Mock
    private MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Mock
    private RegionService regionService;

    @Mock
    private Utils utils;

    @Spy
    @InjectMocks
    private ContractAdjustmentServiceImpl contractAdjustmentService;

    @Before
    public void init() {
        doReturn(utils).when(contractAdjustmentService).getUtils();
    }

    @Test
    public void testContractAdjustmentNotAppliedWhenNotFoundForMedAuthItem() {
        City city = new City();
        city.setId(1L);

        Contract contract = new Contract();
        contract.setId(2L);
        contract.setTransitCondition(false);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(3L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(city);
        medicalAuthorization.setContract(contract);
        medicalAuthorization.setBeneficiary(new Beneficiary());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        contract.getContractAdjustments().add(new ContractAdjustment());

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(contract);
        medicalAuthorizationItem.setContractItem(contractItem);

        when(contractAdjustmentRepository.findByContractIdAndNomenclatorIdAndRegionCitiesId
                (contract, nomenclator, Collections.singleton(city)))
                .thenReturn(Optional.empty());

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(contractAdjustmentService, never()).applyContractAdjustment(any(), any(), any());
    }

    @Test
    public void testMonetaryAdjustmentAppliesCorrectlyInCity() {
        City city = new City();
        city.setId(1L);

        Contract contract = new Contract();
        contract.setId(2L);
        contract.setTransitCondition(false);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(3L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(city);
        medicalAuthorization.setContract(contract);
        medicalAuthorization.setBeneficiary(new Beneficiary());
        medicalAuthorization.setPractitioner(new Practitioner());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(580));

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(contract);
        medicalAuthorizationItem.setContractItem(contractItem);

        MonetaryAdjustment contractAdjustment = new MonetaryAdjustment();
        contractAdjustment.setCity(city);
        contractAdjustment.setContract(contract);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setPeriod(Period.MONTHLY);

        contract.getContractAdjustments().add(contractAdjustment);

        when(contractAdjustmentRepository.findByContractIdAndNomenclatorIdAndRegionCitiesId
                (contract, nomenclator, Collections.singleton(city)))
                .thenReturn(Optional.of(contractAdjustment));

        BigDecimal sum = new BigDecimal(1500);

        when(medicalAuthorizationItemService
                .sumNotTransitSubtotalsByContractAdjustmentAndPractitioner(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(sum);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(monetaryAdjustmentService, times(1))
                .applyContractAdjustment(contractAdjustment, sum, medicalAuthorizationItem);
    }

    @Test
    public void testMonetaryAdjustmentAppliesCorrectlyInRegion() {
        City city = new City();
        city.setId(1L);

        Contract contract = new Contract();
        contract.setId(2L);
        contract.setTransitCondition(false);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(3L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(city);
        medicalAuthorization.setContract(contract);
        medicalAuthorization.setBeneficiary(new Beneficiary());
        medicalAuthorization.setPractitioner(new Practitioner());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(580));

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(contract);
        medicalAuthorizationItem.setContractItem(contractItem);

        MonetaryAdjustment contractAdjustment = new MonetaryAdjustment();
        contractAdjustment.setRegion(new Region());
        contractAdjustment.setContract(contract);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setPeriod(Period.MONTHLY);

        contract.getContractAdjustments().add(contractAdjustment);

        when(contractAdjustmentRepository.findByContractIdAndNomenclatorIdAndRegionCitiesId
                (contract, nomenclator, Collections.singleton(city)))
                .thenReturn(Optional.of(contractAdjustment));

        BigDecimal sum = new BigDecimal(1500);

        when(medicalAuthorizationItemService
                .sumNotTransitSubtotalsByContractAdjustmentAndPractitioner(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(sum);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(monetaryAdjustmentService, times(1))
                .applyContractAdjustment(contractAdjustment, sum, medicalAuthorizationItem);
    }

    @Test
    public void testUsageRateAdjustmentAppliesCorrectlyInCity() {
        City city = new City();
        city.setId(1L);

        Contract contract = new Contract();
        contract.setId(2L);
        contract.setTransitCondition(false);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(3L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(city);
        medicalAuthorization.setContract(contract);
        medicalAuthorization.setBeneficiary(new Beneficiary());
        medicalAuthorization.setPractitioner(new Practitioner());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setQuantity(2);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(contract);
        medicalAuthorizationItem.setContractItem(contractItem);

        UsageRateAdjustment contractAdjustment = new UsageRateAdjustment();
        contractAdjustment.setCity(city);
        contractAdjustment.setContract(contract);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setPeriod(Period.MONTHLY);

        contract.getContractAdjustments().add(contractAdjustment);

        when(contractAdjustmentRepository.findByContractIdAndNomenclatorIdAndRegionCitiesId
                (contract, nomenclator, Collections.singleton(city)))
                .thenReturn(Optional.of(contractAdjustment));

        long count = 65L;

        when(medicalAuthorizationItemService
                .countNotTransitByContractAdjustmentAndPractitioner(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(count);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(usageRateAdjustmentService, times(1))
                .applyContractAdjustment(contractAdjustment, new BigDecimal(count), medicalAuthorizationItem);
    }

    @Test
    public void testMaximumAdjustmentAppliesCorrectlyInRegion() {
        City city = new City();
        city.setId(1L);

        Contract contract = new Contract();
        contract.setId(2L);
        contract.setTransitCondition(false);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(3L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(city);
        medicalAuthorization.setContract(contract);
        medicalAuthorization.setBeneficiary(new Beneficiary());
        medicalAuthorization.setPractitioner(new Practitioner());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setQuantity(2);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(contract);
        medicalAuthorizationItem.setContractItem(contractItem);

        MaximumAdjustment contractAdjustment = new MaximumAdjustment();
        contractAdjustment.setRegion(new Region());
        contractAdjustment.setContract(contract);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setPeriod(Period.MONTHLY);

        contract.getContractAdjustments().add(contractAdjustment);

        when(contractAdjustmentRepository.findByContractIdAndNomenclatorIdAndRegionCitiesId
                (contract, nomenclator, Collections.singleton(city)))
                .thenReturn(Optional.of(contractAdjustment));

        long count = 37L;

        when(medicalAuthorizationItemService
                .countNotTransitByContractAdjustmentAndPractitioner(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(count);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(maximumAdjustmentService, times(1))
                .applyContractAdjustment(contractAdjustment, new BigDecimal(count), medicalAuthorizationItem);
    }

    @Test
    public void testContractAdjustmentDoNotAppliesCorrectlyWhenInvalidContractType() {
        City city = new City();
        city.setId(1L);

        Contract contract = new Contract();
        contract.setId(2L);
        contract.setTransitCondition(false);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(3L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(city);
        medicalAuthorization.setContract(contract);
        medicalAuthorization.setBeneficiary(new Beneficiary());
        medicalAuthorization.setPractitioner(new Practitioner());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setQuantity(2);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(contract);
        medicalAuthorizationItem.setContractItem(contractItem);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setRegion(new Region());
        contractAdjustment.setContract(contract);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setPeriod(Period.MONTHLY);

        contract.getContractAdjustments().add(contractAdjustment);

        when(contractAdjustmentRepository.findByContractIdAndNomenclatorIdAndRegionCitiesId
                (contract, nomenclator, Collections.singleton(city)))
                .thenReturn(Optional.of(contractAdjustment));

        long count = 37L;

        when(medicalAuthorizationItemService
                .countNotTransitByContractAdjustmentAndPractitioner(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(count);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(maximumAdjustmentService, never())
                .applyContractAdjustment(any(), any(), any());
        verify(usageRateAdjustmentService, never())
                .applyContractAdjustment(any(), any(), any());
        verify(monetaryAdjustmentService, never())
                .applyContractAdjustment(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsFalseWhenTrueTransitConditionButInvalidContractInstance() {
        Contract contract = new Contract();
        contract.setContractAdjustments(Collections.emptySet());
        contract.setTransitCondition(true);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(contract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address address = new Address();
        address.setCity(new City());
        beneficiary.setAddress(address);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        medicalAuthorizationItem.setContractItem(contractItem);

        verify(utils, never()).getEntityReference(AuthorizationCondition.class, AuthorizationConditionReference.TRANSIT.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsFalseWhenTrueTransitConditionOrganizationContractInstanceSameCity() {
        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setContractAdjustments(Collections.emptySet());
        organizationContract.setTransitCondition(true);

        Organization organization = new Organization();
        City organizationCity = new City();
        organizationCity.setId(3L);
        organization.setRegion(null);
        Address organizationAddress = new Address();
        organizationAddress.setCity(organizationCity);
        organization.setAddress(organizationAddress);

        organizationContract.setOrganization(organization);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(organizationContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address address = new Address();
        address.setCity(organizationCity);
        beneficiary.setAddress(address);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(utils, never()).getEntityReference(AuthorizationCondition.class, AuthorizationConditionReference.TRANSIT.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsTrueWhenTrueTransitConditionOrganizationContractInstanceDifferentCity() {
        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setContractAdjustments(Collections.emptySet());
        organizationContract.setTransitCondition(true);

        Organization organization = new Organization();
        City organizationCity = new City();
        organizationCity.setId(3L);
        organization.setRegion(null);
        Address organizationAddress = new Address();
        organizationAddress.setCity(organizationCity);
        organization.setAddress(organizationAddress);

        organizationContract.setOrganization(organization);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(organizationContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address beneficiaryAddress = new Address();
        City beneficiaryCity = new City();
        beneficiaryCity.setId(5L);
        beneficiaryAddress.setCity(beneficiaryCity);
        beneficiary.setAddress(beneficiaryAddress);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        AuthorizationCondition transit = new AuthorizationCondition();
        transit.setId(AuthorizationConditionReference.TRANSIT.getId());

        when(utils.getGenericsEntityReference(AuthorizationCondition.class, transit.getId())).thenReturn(transit);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getAuthorizationCondition().getId()).isEqualTo(transit.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsFalseWhenTrueTransitConditionOrganizationContractInstanceCityFromRegion() {
        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setContractAdjustments(Collections.emptySet());
        organizationContract.setTransitCondition(true);

        Organization organization = new Organization();
        Region region = new Region();
        organization.setRegion(region);

        organizationContract.setOrganization(organization);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(organizationContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address beneficiaryAddress = new Address();
        City beneficiaryCity = new City();
        beneficiaryAddress.setCity(beneficiaryCity);
        beneficiary.setAddress(beneficiaryAddress);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        when(regionService.cityBelongToRegion(region, beneficiaryCity)).thenReturn(true);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(utils, never()).getEntityReference(AuthorizationCondition.class, AuthorizationConditionReference.TRANSIT.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsTrueWhenTrueTransitConditionOrganizationContractInstanceCityNotFromRegion() {
        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setContractAdjustments(Collections.emptySet());
        organizationContract.setTransitCondition(true);

        Organization organization = new Organization();
        Region region = new Region();
        organization.setRegion(region);

        organizationContract.setOrganization(organization);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(organizationContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address beneficiaryAddress = new Address();
        City beneficiaryCity = new City();
        beneficiaryAddress.setCity(beneficiaryCity);
        beneficiary.setAddress(beneficiaryAddress);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        AuthorizationCondition transit = new AuthorizationCondition();
        transit.setId(AuthorizationConditionReference.TRANSIT.getId());

        when(regionService.cityBelongToRegion(region, beneficiaryCity)).thenReturn(false);
        when(utils.getGenericsEntityReference(AuthorizationCondition.class, transit.getId())).thenReturn(transit);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getAuthorizationCondition().getId()).isEqualTo(transit.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsFalseWhenTrueTransitConditionPractitionerContractInstanceSameCity() {
        PractitionerContract practitionerContract = new PractitionerContract();
        practitionerContract.setContractAdjustments(Collections.emptySet());
        practitionerContract.setTransitCondition(true);

        Practitioner practitioner = new Practitioner();
        City practitionerCity = new City();
        practitionerCity.setId(4L);
        Address practitionerAddress = new Address();
        practitionerAddress.setCity(practitionerCity);
        practitioner.setAddress(practitionerAddress);

        practitionerContract.setPractitioner(practitioner);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(practitionerContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address beneficiaryAddress = new Address();
        beneficiaryAddress.setCity(practitionerCity);
        beneficiary.setAddress(beneficiaryAddress);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(utils, never()).getEntityReference(AuthorizationCondition.class, AuthorizationConditionReference.TRANSIT.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsTrueWhenTrueTransitConditionPractitionerContractInstanceDifferentCity() {
        PractitionerContract practitionerContract = new PractitionerContract();
        practitionerContract.setContractAdjustments(Collections.emptySet());
        practitionerContract.setTransitCondition(true);

        Practitioner practitioner = new Practitioner();
        City practitionerCity = new City();
        practitionerCity.setId(4L);
        Address practitionerAddress = new Address();
        practitionerAddress.setCity(practitionerCity);
        practitioner.setAddress(practitionerAddress);

        practitionerContract.setPractitioner(practitioner);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(practitionerContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address beneficiaryAddress = new Address();
        City beneficiaryCity = new City();
        beneficiaryCity.setId(7L);
        beneficiaryAddress.setCity(beneficiaryCity);
        beneficiary.setAddress(beneficiaryAddress);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        AuthorizationCondition transit = new AuthorizationCondition();
        transit.setId(AuthorizationConditionReference.TRANSIT.getId());

        when(utils.getGenericsEntityReference(AuthorizationCondition.class, transit.getId())).thenReturn(transit);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getAuthorizationCondition().getId()).isEqualTo(transit.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsFalseWhenTrueTransitConditionMedicalCenterContractInstanceSameCity() {
        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();
        medicalCenterContract.setContractAdjustments(Collections.emptySet());
        medicalCenterContract.setTransitCondition(true);

        MedicalCenter medicalCenter = new MedicalCenter();
        City medicalCenterCity = new City();
        medicalCenterCity.setId(4L);
        Address medicalCenterAddress = new Address();
        medicalCenterAddress.setCity(medicalCenterCity);
        medicalCenter.setAddress(medicalCenterAddress);

        medicalCenterContract.setMedicalCenter(medicalCenter);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(medicalCenterContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address beneficiaryAddress = new Address();
        beneficiaryAddress.setCity(medicalCenterCity);
        beneficiary.setAddress(beneficiaryAddress);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        verify(utils, never()).getEntityReference(AuthorizationCondition.class, AuthorizationConditionReference.TRANSIT.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }

    @Test
    public void testEvaluateTransitAdjustmentReturnsTrueWhenTrueTransitConditionMedicalCenterContractInstanceDifferentCity() {
        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();
        medicalCenterContract.setContractAdjustments(Collections.emptySet());
        medicalCenterContract.setTransitCondition(true);

        MedicalCenter medicalCenter = new MedicalCenter();
        City medicalCenterCity = new City();
        medicalCenterCity.setId(4L);
        Address medicalCenterAddress = new Address();
        medicalCenterAddress.setCity(medicalCenterCity);
        medicalCenter.setAddress(medicalCenterAddress);

        medicalCenterContract.setMedicalCenter(medicalCenter);

        ContractItem contractItem = new ContractItem();
        contractItem.setContract(medicalCenterContract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        Address beneficiaryAddress = new Address();
        City beneficiaryCity = new City();
        beneficiaryCity.setId(7L);
        beneficiaryAddress.setCity(beneficiaryCity);
        beneficiary.setAddress(beneficiaryAddress);
        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setContractItem(contractItem);

        AuthorizationCondition transit = new AuthorizationCondition();
        transit.setId(AuthorizationConditionReference.TRANSIT.getId());

        when(utils.getGenericsEntityReference(AuthorizationCondition.class, transit.getId())).thenReturn(transit);

        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getAuthorizationCondition().getId()).isEqualTo(transit.getId());
        verify(contractAdjustmentRepository, never()).findByContractIdAndNomenclatorIdAndRegionCitiesId(any(), any(), any());
    }
}
