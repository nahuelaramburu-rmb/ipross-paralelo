package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryImportDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryCategory;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.person.model.*;
import com.capacidad.validationapi.module.person.reference.IdTypeReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.*;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.PAYCHECK;
import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportConstants.*;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.DEFAULT_RELATIONSHIP_TYPE;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.SON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryImportBuilderTemplateImplTest {

    @Mock
    private Utils utils;

    @InjectMocks
    private BeneficiaryImportBuilderTemplateImpl importBuilder;

    @Test
    public void testResolveWorkIdNumberReturnsEmptyWhenEmpty() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setWorkIdNumber("");

        Optional<Long> workIdNumber = importBuilder.resolveWorkIdNumber(importDTO);

        assertThat(workIdNumber).isEmpty();
    }

    @Test
    public void testResolveWorkIdNumberReturnsEmptyWhenZero() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setWorkIdNumber("0");

        Optional<Long> workIdNumber = importBuilder.resolveWorkIdNumber(importDTO);

        assertThat(workIdNumber).isEmpty();
    }

    @Test
    public void testResolveWorkIdNumberReturnsEmptyWhenNegative() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setWorkIdNumber("-1");

        Optional<Long> workIdNumber = importBuilder.resolveWorkIdNumber(importDTO);

        assertThat(workIdNumber).isEmpty();
    }

    @Test
    public void testResolveWorkIdNumberReturnsValueWhenPositive() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setWorkIdNumber("1");

        Optional<Long> workIdNumber = importBuilder.resolveWorkIdNumber(importDTO);

        assertThat(workIdNumber).contains(1L);
    }

    @Test
    public void testResolveIdNumberThrowsExceptionWhenInvalid() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setIdNumber("invalid");

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.resolveIdNumber(importDTO));

        assertThat(exception.getMessage()).isEqualTo("import.idNumber");
    }

    @Test
    public void testResolveIdNumberReturnsValueWhenValid() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setIdNumber("11223344");

        Long result = importBuilder.resolveIdNumber(importDTO);

        assertThat(result).isEqualTo(Long.valueOf(importDTO.getIdNumber()));
    }

    @Test
    public void testResolveBeneficiaryCodeReturnsValueWhenValid() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setBeneficiaryCode("beneficiary-code");

        String result = importBuilder.resolveBeneficiaryCode(importDTO);

        assertThat(result).isEqualTo(importDTO.getBeneficiaryCode());
    }

    @Test
    public void testSetLastNameAndNameSameColumnThrowsExceptionWhenInvalidSeparator() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setLastNameAndName("Lastname. Name");
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.setLastNameAndName(new Beneficiary(), importDTO));

        assertThat(exception.getMessage()).isEqualTo("import.lastNameAndName");
    }

    @Test
    public void testSetLastNameAndNameSameColumnSetsDataWhenValidSeparator() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        Beneficiary beneficiary = new Beneficiary();
        importDTO.setLastNameAndName("Lastname, Name");

        importBuilder.setLastNameAndName(beneficiary, importDTO);

        assertThat(beneficiary.getLastName()).isEqualTo("Lastname");
        assertThat(beneficiary.getName()).isEqualTo("Name");
    }

    @Test
    public void testSetLastNameAndNameSingleColumnsThrowsExceptionWhenEmptyName() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setLastName("Lastname");

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.setLastNameAndName(new Beneficiary(), importDTO));

        assertThat(exception.getMessage()).isEqualTo("import.lastNameAndName");
    }

    @Test
    public void testSetLastNameAndNameSingleColumnsThrowsExceptionWhenEmptyLastName() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setName("Name");

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.setLastNameAndName(new Beneficiary(), importDTO));

        assertThat(exception.getMessage()).isEqualTo("import.lastNameAndName");
    }

    @Test
    public void testSetLastNameAndNameSingleColumnsSetsDataWhenNotEmptyFields() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        Beneficiary beneficiary = new Beneficiary();
        importDTO.setName("Name");
        importDTO.setLastName("Lastname");

        importBuilder.setLastNameAndName(beneficiary, importDTO);

        assertThat(beneficiary.getLastName()).isEqualTo("Lastname");
        assertThat(beneficiary.getName()).isEqualTo("Name");
    }

    @Test
    public void testResolveDateThrowsExceptionWhenInvalidFormat() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setBirthDate("2020/08/01");

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.resolveDate(importDTO));

        assertThat(exception.getMessage()).isEqualTo("import.invalidBirthDate");
    }

    @Test
    public void testResolveDateReturnsLocalDateWhenValidFormat() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setBirthDate("01/08/2020");

        LocalDate localDate = importBuilder.resolveDate(importDTO);

        assertThat(localDate).isEqualTo(LocalDate.of(2020, 8, 1));
    }

    @Test
    public void testFindIdTypeReturnsDefaultWhenNotFound() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setIdTypeAlias("invalidIdType");

        IdType idType = new IdType();
        idType.setAlias("invalid");

        IdType defaultIdType = new IdType();
        defaultIdType.setId(IdTypeReference.ID.getId());

        Map<String, IdType> idTypeMap = new HashMap<>();
        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(ID_TYPES_KEY, idTypeMap);

        when(utils.getGenericsEntityReference(IdType.class, IdTypeReference.ID.getId())).thenReturn(defaultIdType);

        IdType result = importBuilder.findIdType(importDTO, persistedProperties);

        assertThat(result).isEqualTo(defaultIdType);
    }

    @Test
    public void testFindIdTypeReturnsWhenFound() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setIdTypeAlias("D.N.I".toLowerCase());

        IdType idType = new IdType();
        idType.setAlias("DNI");

        Map<String, IdType> idTypeMap = new HashMap<>();
        idTypeMap.put(idType.getAlias().toLowerCase(), idType);
        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(ID_TYPES_KEY, idTypeMap);

        IdType result = importBuilder.findIdType(importDTO, persistedProperties);

        assertThat(result).isEqualTo(idType);
    }

    @Test
    public void testResolveGenderReturnsMaleWhenStartsWithM() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setGender("masculino");

        Gender result = importBuilder.resolveGender(importDTO);

        assertThat(result).isEqualTo(Gender.MASCULINO);
    }

    @Test
    public void testResolveGenderReturnsFemaleByDefault() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setGender("default");

        Gender result = importBuilder.resolveGender(importDTO);

        assertThat(result).isEqualTo(Gender.FEMENINO);
    }

    @Test
    public void testFindRelationshipTypeReturnsDefaultWhenNotFound() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setRelationshipType("invalidRelationshipType");

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setName("Titular");

        Map<String, RelationshipType> relationshipTypeMap = new HashMap<>();
        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(RELATIONSHIP_TYPES_KEY, relationshipTypeMap);

        RelationshipType defaultRelationshipType = new RelationshipType();
        defaultRelationshipType.setId(DEFAULT_RELATIONSHIP_TYPE.getId());

        when(utils.getGenericsEntityReference(RelationshipType.class, DEFAULT_RELATIONSHIP_TYPE.getId())).thenReturn(defaultRelationshipType);

        RelationshipType result = importBuilder.findRelationshipType(importDTO, persistedProperties);

        assertThat(result).isEqualTo(defaultRelationshipType);
    }

    @Test
    public void testFindRelationshipTypeReturnsWhenFound() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setRelationshipType("titular");

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setName("Titular");

        Map<String, RelationshipType> relationshipTypeMap = new HashMap<>();
        relationshipTypeMap.put(relationshipType.getName().toLowerCase(), relationshipType);
        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(RELATIONSHIP_TYPES_KEY, relationshipTypeMap);

        RelationshipType result = importBuilder.findRelationshipType(importDTO, persistedProperties);

        assertThat(result).isEqualTo(relationshipType);
    }

    @Test
    public void testResolveAddressThrowsExceptionWhenCityDoesNotExists() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setCity("city2");
        importDTO.setProvince("province2");

        City city = new City();
        city.setName("city1");

        Map<String, Object> persistedProperties = new HashMap<>();
        Map<String, City> cityMap = new HashMap<>();

        cityMap.put(city.getName().toLowerCase(), city);

        Map<String, Map<String, City>> provinceCityMap = new HashMap<>();

        provinceCityMap.put("province2", cityMap);
        persistedProperties.put(CITIES_KEY, provinceCityMap);

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> importBuilder.resolveAddress(importDTO, persistedProperties));

        assertThat(exception.getMessage()).isEqualTo("import.cityNotFound");
    }

    @Test
    public void testResolveAddressThrowsExceptionWhenProvinceDoesNotExist() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setCity("city2");
        importDTO.setProvince("province2");

        City city = new City();
        city.setName("city1");

        Map<String, Object> persistedProperties = new HashMap<>();
        Map<String, City> cityMap = new HashMap<>();

        cityMap.put(city.getName().toLowerCase(), city);

        Map<String, Map<String, City>> provinceCityMap = new HashMap<>();

        provinceCityMap.put("province1", cityMap);
        persistedProperties.put(CITIES_KEY, provinceCityMap);

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> importBuilder.resolveAddress(importDTO, persistedProperties));

        assertThat(exception.getMessage()).isEqualTo("import.provinceNotFound");
    }

    @Test
    public void testResolveAddressReturnsSuccessfullyWhenCityFoundAndExistentProvince() throws ObjectNotFoundException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setCity("city1");
        importDTO.setProvince("province1");
        importDTO.setStreet("street");
        importDTO.setStreetNumber("123");
        importDTO.setApartment("apartment");
        importDTO.setDistrict("district");

        City city = new City();
        city.setName("city1");

        Map<String, City> cityMap = new HashMap<>();

        cityMap.put(city.getName().toLowerCase(), city);

        Map<String, Map<String, City>> provinceCityMap = new HashMap<>();
        provinceCityMap.put("province1", cityMap);

        Map<String, Object> persistedProperties = new HashMap<>();
        provinceCityMap.put("province1", cityMap);
        persistedProperties.put(CITIES_KEY, provinceCityMap);

        Address result = importBuilder.resolveAddress(importDTO, persistedProperties);

        assertThat(result.getCity()).isEqualTo(city);
        assertThat(result.getStreet()).isEqualTo(importDTO.getStreet());
        assertThat(result.getStreetNumber()).isEqualTo(Integer.valueOf(importDTO.getStreetNumber()));
        assertThat(result.getDistrict()).isEqualTo(importDTO.getDistrict());
        assertThat(result.getApartment()).isEqualTo(importDTO.getApartment());
    }

    @Test
    public void testResolveInsurancePlansThrowsExceptionWhenEmpty() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setInsurancePlans("");

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.resolveInsurancePlans(importDTO, new HashMap<>()));

        assertThat(exception.getMessage()).isEqualTo("import.insurancePlansRequired");
    }

    @Test
    public void testResolveInsurancePlansThrowsExceptionWhenNotFoundWithoutExpiration() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setInsurancePlans("invalidPlan");

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setName("insurancePlan1");

        Map<String, InsurancePlan> insurancePlanMap = new HashMap<>();
        insurancePlanMap.put(insurancePlan.getName().toLowerCase(), insurancePlan);

        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(INSURANCE_PLANS_KEY, insurancePlanMap);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.resolveInsurancePlans(importDTO, persistedProperties));

        assertThat(exception.getMessage()).isEqualTo("import.insurancePlansRequired");
    }

    @Test
    public void testResolveInsurancePlansReturnsSuccessfullyWhenFoundWithoutExpiration() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setInsurancePlans("insurancePlan1".toLowerCase());

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setName("insurancePlan1");

        Map<String, InsurancePlan> insurancePlanMap = new HashMap<>();
        insurancePlanMap.put(insurancePlan.getName().toLowerCase(), insurancePlan);

        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(INSURANCE_PLANS_KEY, insurancePlanMap);

        Set<BeneficiaryInsurancePlan> result = importBuilder.resolveInsurancePlans(importDTO, persistedProperties);

        assertThat(result.size()).isEqualTo(1);
        BeneficiaryInsurancePlan beneficiaryInsurancePlan = result.iterator().next();
        assertThat(beneficiaryInsurancePlan.getInsurancePlan()).isEqualTo(insurancePlan);
        assertThat(beneficiaryInsurancePlan.getExpirationDate()).isNull();
    }

    @Test
    public void testResolveInsurancePlansReturnsSuccessfullyWhenFoundWithExpiration() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setInsurancePlans("insuranceplan1 (01/08/2020)");

        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setName("insurancePlan1");

        Map<String, InsurancePlan> insurancePlanMap = new HashMap<>();
        insurancePlanMap.put(insurancePlan.getName().toLowerCase(), insurancePlan);

        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(INSURANCE_PLANS_KEY, insurancePlanMap);

        Set<BeneficiaryInsurancePlan> result = importBuilder.resolveInsurancePlans(importDTO, persistedProperties);

        assertThat(result.size()).isEqualTo(1);
        BeneficiaryInsurancePlan beneficiaryInsurancePlan = result.iterator().next();
        assertThat(beneficiaryInsurancePlan.getInsurancePlan()).isEqualTo(insurancePlan);
        assertThat(beneficiaryInsurancePlan.getExpirationDate()).isEqualTo(LocalDate.of(2020, 8, 1));
    }

    @Test
    public void testFindCompanyReturnsEmptyWhenEmptyName() throws ObjectNotValidException {
        Optional<Company> result = importBuilder.findCompany(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindCompanyThrowsExceptionWhenNotFound() {
        Map<String, Object> persistedProperties = new HashMap<>();
        Map<String, Company> companyMap = new HashMap<>();
        persistedProperties.put(COMPANIES_KEY, companyMap);

        Company newCompany = new Company();
        newCompany.setName("myCompany");
        Address newCompanyAddress = new Address();
        City companyCity = new City();
        newCompanyAddress.setCity(companyCity);
        newCompany.setAddress(newCompanyAddress);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.findCompany(newCompany.getName(), persistedProperties));

        assertThat(exception.getMessage()).isEqualTo("import.company");
    }

    @Test
    public void testFindOrCreateCompanyReturnsCompanyWhenFound() throws ObjectNotValidException {
        Map<String, Object> persistedProperties = new HashMap<>();

        Company company = new Company();
        company.setName("myCompany");

        Map<String, Company> companyMap = new HashMap<>();
        companyMap.put(company.getName().toLowerCase(), company);

        persistedProperties.put(COMPANIES_KEY, companyMap);

        Optional<Company> result = importBuilder.findCompany(company.getName().toLowerCase(), persistedProperties);

        assertThat(result).contains(company);
    }

    @Test
    public void testFindBeneficiaryCategoryReturnsEmptyWhenEmptyName() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setBeneficiaryCategory("");
        Optional<BeneficiaryCategory> result = importBuilder.findBeneficiaryCategory(importDTO, null);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindBeneficiaryCategoryThrowsExceptionWhenNotFound() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setBeneficiaryCategory("myCategory");

        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(BENEFICIARY_CATEGORIES_KEY, new HashMap<>());

        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setName(importDTO.getBeneficiaryCategory());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> importBuilder.findBeneficiaryCategory(importDTO, persistedProperties));

        assertThat(exception.getMessage()).isEqualTo("import.beneficiaryCategory");
    }

    @Test
    public void testFindBeneficiaryCategoryReturnsCategoryWhenFound() throws ObjectNotValidException {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setBeneficiaryCategory("myCategory".toLowerCase());

        Map<String, Object> persistedProperties = new HashMap<>();
        Map<String, BeneficiaryCategory> beneficiaryCategoryMap = new HashMap<>();

        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setName(importDTO.getBeneficiaryCategory());
        beneficiaryCategoryMap.put(beneficiaryCategory.getName().toLowerCase(), beneficiaryCategory);

        persistedProperties.put(BENEFICIARY_CATEGORIES_KEY, beneficiaryCategoryMap);

        Optional<BeneficiaryCategory> result = importBuilder.findBeneficiaryCategory(importDTO, persistedProperties);

        assertThat(result).contains(beneficiaryCategory);
    }

    @Test
    public void testResolveAndSetPaymentMethodDeterminesPaycheckCategory() throws ObjectNotValidException {
        Address beneficiaryAddress = new Address();
        City city = new City();
        beneficiaryAddress.setCity(city);
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAddress(beneficiaryAddress);
        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setName("myCategory".toLowerCase());
        beneficiary.setBeneficiaryCategory(beneficiaryCategory);

        Map<String, Object> persistedProperties = new HashMap<>();
        Map<String, Object> tenantMappings = new HashMap<>();
        List<String> paycheckCategory = Collections.singletonList("mycategory");
        tenantMappings.put(PAYCHECK_CATEGORIES_TENANT_MAPPING, paycheckCategory);
        persistedProperties.put(TENANT_MAPPINGS, tenantMappings);

        Company company = new Company();
        company.setName(beneficiaryCategory.getName());
        Map<String, Company> companyMap = new HashMap<>();
        companyMap.put(company.getName().toLowerCase(), company);
        persistedProperties.put(COMPANIES_KEY, companyMap);

        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());

        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());

        when(utils.getGenericsEntityReference(PaymentMethod.class, PAYCHECK.getId())).thenReturn(paycheck);
        when(utils.getGenericsEntityReference(PaymentMethod.class, VOLUNTARY.getId())).thenReturn(voluntary);

        importBuilder.resolveAndSetPaymentMethod(beneficiary, persistedProperties);

        assertThat(beneficiary.getPaymentMethod()).isEqualTo(paycheck);
        assertThat(beneficiary.getCompany()).isEqualTo(company);
    }

    @Test
    public void testResolveAndSetPaymentMethodDeterminesVoluntaryCategory() throws ObjectNotValidException {
        Address beneficiaryAddress = new Address();
        City city = new City();
        beneficiaryAddress.setCity(city);
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAddress(beneficiaryAddress);
        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setName("myCategory");
        beneficiary.setBeneficiaryCategory(beneficiaryCategory);

        Map<String, Object> persistedProperties = new HashMap<>();
        Map<String, Object> tenantMappings = new HashMap<>();
        List<String> voluntaryCategory = Collections.singletonList("mycategory");
        tenantMappings.put(VOLUNTARY_CATEGORIES_TENANT_MAPPING, voluntaryCategory);
        persistedProperties.put(TENANT_MAPPINGS, tenantMappings);

        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());

        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());

        when(utils.getGenericsEntityReference(PaymentMethod.class, PAYCHECK.getId())).thenReturn(paycheck);
        when(utils.getGenericsEntityReference(PaymentMethod.class, VOLUNTARY.getId())).thenReturn(voluntary);

        importBuilder.resolveAndSetPaymentMethod(beneficiary, persistedProperties);

        assertThat(beneficiary.getPaymentMethod()).isEqualTo(voluntary);
    }

    @Test
    public void testResolveAndSetPaymentMethodDeterminesVoluntaryCompany() throws ObjectNotValidException {
        Address beneficiaryAddress = new Address();
        City city = new City();
        beneficiaryAddress.setCity(city);
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAddress(beneficiaryAddress);
        Company company = new Company();
        company.setName("myCompany".toLowerCase());
        beneficiary.setCompany(company);

        Map<String, Object> persistedProperties = new HashMap<>();
        Map<String, Object> tenantMappings = new HashMap<>();
        List<String> voluntaryCompany = Collections.singletonList("mycompany");
        tenantMappings.put(VOLUNTARY_COMPANIES_TENANT_MAPPING, voluntaryCompany);
        persistedProperties.put(TENANT_MAPPINGS, tenantMappings);

        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());

        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());

        when(utils.getGenericsEntityReference(PaymentMethod.class, PAYCHECK.getId())).thenReturn(paycheck);
        when(utils.getGenericsEntityReference(PaymentMethod.class, VOLUNTARY.getId())).thenReturn(voluntary);

        importBuilder.resolveAndSetPaymentMethod(beneficiary, persistedProperties);

        assertThat(beneficiary.getPaymentMethod()).isEqualTo(voluntary);
        assertThat(beneficiary.getCompany()).isEqualTo(company);
    }

    @Test
    public void testResolveAndSetPaymentMethodDeterminesDefaultPaycheckCompany() throws ObjectNotValidException {
        Address beneficiaryAddress = new Address();
        City city = new City();
        beneficiaryAddress.setCity(city);
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAddress(beneficiaryAddress);
        Company company = new Company();
        company.setName("myCompany");
        beneficiary.setCompany(company);

        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());

        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());

        when(utils.getGenericsEntityReference(PaymentMethod.class, PAYCHECK.getId())).thenReturn(paycheck);
        when(utils.getGenericsEntityReference(PaymentMethod.class, VOLUNTARY.getId())).thenReturn(voluntary);

        importBuilder.resolveAndSetPaymentMethod(beneficiary, new HashMap<>());

        assertThat(beneficiary.getPaymentMethod()).isEqualTo(paycheck);
        assertThat(beneficiary.getCompany()).isEqualTo(company);
    }

    @Test
    public void testResolveAndSetPaymentMethodDeterminesDefaultVoluntaryCompany() throws ObjectNotValidException {
        Address beneficiaryAddress = new Address();
        City city = new City();
        beneficiaryAddress.setCity(city);
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAddress(beneficiaryAddress);
        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setName("myCategory");
        beneficiary.setBeneficiaryCategory(beneficiaryCategory);

        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());

        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());

        when(utils.getGenericsEntityReference(PaymentMethod.class, PAYCHECK.getId())).thenReturn(paycheck);
        when(utils.getGenericsEntityReference(PaymentMethod.class, VOLUNTARY.getId())).thenReturn(voluntary);

        importBuilder.resolveAndSetPaymentMethod(beneficiary, new HashMap<>());

        assertThat(beneficiary.getPaymentMethod()).isEqualTo(voluntary);
    }

    @Test
    public void testFindMaritalStatusReturnsWhenFound() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setMaritalStatus("maritalStatus".toLowerCase());

        MaritalStatus maritalStatus = new MaritalStatus();
        maritalStatus.setName("maritalStatus");

        Map<String, MaritalStatus> maritalStatusMap = new HashMap<>();
        maritalStatusMap.put(maritalStatus.getName().toLowerCase(), maritalStatus);

        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(MARITAL_STATUSES_KEY, maritalStatusMap);

        Optional<MaritalStatus> result = importBuilder.findMaritalStatus(importDTO, persistedProperties);

        assertThat(result).contains(maritalStatus);
    }

    @Test
    public void testResolvePhoneReturnsEmptyWhenEmpty() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setPhone("");

        Optional<Phone> result = importBuilder.resolvePhone(importDTO);

        assertThat(result).isEmpty();
    }

    @Test
    public void testResolvePhoneReturnsEmptyWhenInvalid() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setPhone("invalidPhone");

        Optional<Phone> result = importBuilder.resolvePhone(importDTO);

        assertThat(result).isEmpty();
    }

    @Test
    public void testResolvePhoneReturnsValueWhenNotEmpty() {
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setPhone("155434543");

        Optional<Phone> result = importBuilder.resolvePhone(importDTO);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getPhoneNumber()).isEqualTo(Long.valueOf(importDTO.getPhone()));
        assertThat(result.get().getPhoneType()).isEqualTo(PhoneType.MOVIL);
    }

    @Test
    public void testDetermineRelatedBeneficiaryNotRelatedWhenEmptyRelatedBeneficiaryCode() {
        Beneficiary beneficiary = new Beneficiary();
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();

        importBuilder.determineRelatedBeneficiary(beneficiary, importDTO);

        assertThat(beneficiary.getRelatedBeneficiary()).isNull();
    }

    @Test
    public void testDetermineRelatedBeneficiaryNotRelatedWhenEqualRelatedBeneficiaryCode() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode("11223344");
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setRelatedBeneficiaryCode(beneficiary.getBeneficiaryCode());

        importBuilder.determineRelatedBeneficiary(beneficiary, importDTO);

        assertThat(beneficiary.getRelatedBeneficiary()).isNull();
    }

    @Test
    public void testDetermineRelatedBeneficiaryRelatedWhenNotEqualRelatedBeneficiaryCodeAndNotHolder() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode("11223344");
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();
        importDTO.setRelatedBeneficiaryCode("44332211");

        RelationshipType son = new RelationshipType();
        son.setId(SON.getId());

        beneficiary.setRelationshipType(son);

        importBuilder.determineRelatedBeneficiary(beneficiary, importDTO);

        assertThat(beneficiary.getRelatedBeneficiary().getBeneficiaryCode()).isEqualTo(importDTO.getRelatedBeneficiaryCode());
    }

}
