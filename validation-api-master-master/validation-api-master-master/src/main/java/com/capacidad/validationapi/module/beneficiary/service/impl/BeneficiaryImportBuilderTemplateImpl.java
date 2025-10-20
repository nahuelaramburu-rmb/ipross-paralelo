package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryImportDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryCategory;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.importprocessor.misc.ImportUtils;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.person.model.*;
import com.capacidad.validationapi.module.person.reference.IdTypeReference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportConstants.*;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.DEFAULT_RELATIONSHIP_TYPE;

@Log4j2
@Service
public class BeneficiaryImportBuilderTemplateImpl extends BeneficiaryImportBuilderTemplate {

    private final Utils utils;

    public BeneficiaryImportBuilderTemplateImpl(Utils utils) {
        this.utils = utils;
    }

    @Override
    public String resolveBeneficiaryCode(BeneficiaryImportDTO importDTO) {
        return importDTO.getBeneficiaryCode();
    }

    @Override
    protected Long resolveIdNumber(BeneficiaryImportDTO importDTO) throws ObjectNotValidException {
        return ImportUtils.parseLong(importDTO.getIdNumber()).orElseThrow(() -> new ObjectNotValidException("import.idNumber", importDTO.getIdNumber()));
    }

    @Override
    public void setLastNameAndName(Beneficiary beneficiary, BeneficiaryImportDTO importDTO) throws ObjectNotValidException {
        if (StringUtils.isNotEmpty(importDTO.getLastNameAndName())) {
            String[] lastNameAndName = StringUtils.split(importDTO.getLastNameAndName(), COMA);
            if (lastNameAndName.length != 2)
                throw new ObjectNotValidException("import.lastNameAndName", lastNameAndName);
            String lastName = splitAndFormatName(lastNameAndName[0], WHITESPACE);
            String name = splitAndFormatName(lastNameAndName[1], WHITESPACE);
            beneficiary.setLastName(lastName);
            beneficiary.setName(name);
        } else {
            if (StringUtils.isEmpty(importDTO.getLastName()) || StringUtils.isEmpty(importDTO.getName()))
                throw new ObjectNotValidException("import.lastNameAndName", StringUtils.join(importDTO.getLastName(), importDTO.getName()));
            String lastName = splitAndFormatName(importDTO.getLastName(), WHITESPACE);
            String name = splitAndFormatName(importDTO.getName(), WHITESPACE);
            beneficiary.setLastName(lastName);
            beneficiary.setName(name);
        }
    }

    private String splitAndFormatName(String value, String separator) {
        String[] values = StringUtils.split(value, separator);
        return Stream.of(values)
                .map(ImportUtils::lowerAndCapitalizeStriped)
                .collect(Collectors.joining(WHITESPACE));
    }

    @Override
    public LocalDate resolveDate(BeneficiaryImportDTO importDTO) throws ObjectNotValidException {
        return ImportUtils.parseDate(importDTO.getBirthDate())
                .orElseThrow(() -> new ObjectNotValidException("import.invalidBirthDate", importDTO.getBirthDate()));
    }

    @Override
    public IdType findIdType(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) {
        Map<String, IdType> idTypes = ImportUtils.getProperty(persistedProperties, ID_TYPES_KEY);
        String formattedIdTypeAlias = StringUtils.remove(importDTO.getIdTypeAlias(), DOT);
        String mappedIdTypeAlias = (String) ImportUtils.getTenantMapping(persistedProperties, ID_TYPE_ALIASES_KEY, formattedIdTypeAlias).orElse(formattedIdTypeAlias);
        return ImportUtils.filterMap(idTypes, mappedIdTypeAlias)
                .orElse(getDefaultIdType());
    }

    private IdType getDefaultIdType() {
        return this.utils.getGenericsEntityReference(IdType.class, IdTypeReference.ID.getId());
    }

    @Override
    public Gender resolveGender(BeneficiaryImportDTO importDTO) {
        String formattedGender = importDTO.getGender();
        return StringUtils.startsWith(formattedGender, "m") ? Gender.MASCULINO : Gender.FEMENINO;
    }

    @Override
    public RelationshipType findRelationshipType(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) {
        String mappedRelationshipType = (String) ImportUtils.getTenantMapping(persistedProperties, RELATIONSHIP_TYPES_KEY, importDTO.getRelationshipType()).orElse(importDTO.getRelationshipType());
        Map<String, RelationshipType> relationshipTypes = ImportUtils.getProperty(persistedProperties, RELATIONSHIP_TYPES_KEY);
        return ImportUtils.filterMap(relationshipTypes, mappedRelationshipType)
                .orElse(getDefaultRelationshipType());
    }

    private RelationshipType getDefaultRelationshipType() {
        return this.utils.getGenericsEntityReference(RelationshipType.class, DEFAULT_RELATIONSHIP_TYPE.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public Address resolveAddress(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotFoundException {
        City city = resolveCity(importDTO, persistedProperties);
        Address address = new Address();
        address.setCity(city);
        address.setApartment(importDTO.getApartment());
        address.setStreet(importDTO.getStreet());
        address.setDistrict(importDTO.getDistrict());
        address.setStreetNumber(ImportUtils.parseInteger(importDTO.getStreetNumber()).orElse(null));
        return address;
    }

    @SuppressWarnings("unchecked")
    private City resolveCity(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotFoundException {
        String provinceName = importDTO.getProvince();
        String mappedProvinceName = (String) ImportUtils.getTenantMapping(persistedProperties, PROVINCES_KEY, provinceName).orElse(provinceName);
        Map<String, Map<String, City>> citiesMap = (Map<String, Map<String, City>>) persistedProperties.get(CITIES_KEY);
        if (!citiesMap.containsKey(mappedProvinceName))
            throw new ObjectNotFoundException("import.provinceNotFound", provinceName);
        String cityName = importDTO.getCity();
        String mappedCityName = (String) ImportUtils.getTenantMapping(persistedProperties, CITIES_KEY, cityName).orElse(cityName);
        return ImportUtils.filterMap(citiesMap.get(mappedProvinceName), mappedCityName)
                .orElseThrow(() -> new ObjectNotFoundException("import.cityNotFound", cityName, provinceName));
    }

    @Override
    public Set<BeneficiaryInsurancePlan> resolveInsurancePlans(BeneficiaryImportDTO beneficiaryImportDTO, Map<String, Object> persistedProperties) throws ObjectNotValidException {
        Map<String, InsurancePlan> insurancePlans = ImportUtils.getProperty(persistedProperties, INSURANCE_PLANS_KEY);
        String[] formattedInsurancePlans = StringUtils.split(beneficiaryImportDTO.getInsurancePlans(), COMA);
        Set<BeneficiaryInsurancePlan> beneficiaryInsurancePlans = new HashSet<>();
        for (String insurancePlan : formattedInsurancePlans) {
            buildBeneficiaryInsurancePlanObject(insurancePlan, insurancePlans)
                    .ifPresent(beneficiaryInsurancePlans::add);
        }
        if (beneficiaryInsurancePlans.isEmpty())
            throw new ObjectNotValidException("import.insurancePlansRequired");
        return beneficiaryInsurancePlans;
    }

    private Optional<BeneficiaryInsurancePlan> buildBeneficiaryInsurancePlanObject(String beneficiaryInsurancePlanString, Map<String, InsurancePlan> insurancePlans) {
        Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(beneficiaryInsurancePlanString);
        LocalDate expirationDate = null;
        if (matcher.find()) {
            expirationDate = ImportUtils.parseDate(matcher.group(1)).orElse(null);
            beneficiaryInsurancePlanString = StringUtils.trim(StringUtils.remove(beneficiaryInsurancePlanString, matcher.group(0)));
        }
        String trimmedBeneficiaryInsurancePlanString = StringUtils.trim(beneficiaryInsurancePlanString);
        Optional<InsurancePlan> optInsurancePlan = findInsurancePlan(insurancePlans, trimmedBeneficiaryInsurancePlanString);
        if (optInsurancePlan.isPresent()) {
            InsurancePlan insurancePlan = optInsurancePlan.get();
            BeneficiaryInsurancePlan beneficiaryInsurancePlan = new BeneficiaryInsurancePlan();
            beneficiaryInsurancePlan.setInsurancePlan(insurancePlan);
            beneficiaryInsurancePlan.setPriority(insurancePlan.getPriority());
            beneficiaryInsurancePlan.setExpirationDate(expirationDate);
            return Optional.of(beneficiaryInsurancePlan);
        }
        return Optional.empty();
    }

    private Optional<InsurancePlan> findInsurancePlan(Map<String, InsurancePlan> insurancePlans, String insurancePlan) {
        return ImportUtils.filterMap(insurancePlans, insurancePlan);
    }

    @Override
    public Optional<Company> findCompany(String companyName, Map<String, Object> persistedProperties) throws ObjectNotValidException {
        if (!StringUtils.isEmpty(companyName)) {
            String mappedCompany = (String) ImportUtils.getTenantMapping(persistedProperties, COMPANIES_KEY, companyName).orElse(companyName);
            Map<String, Company> companies = ImportUtils.getProperty(persistedProperties, COMPANIES_KEY);
            return Optional.of(ImportUtils.filterMap(companies, mappedCompany)
                    .orElseThrow(() -> new ObjectNotValidException("import.company", companyName)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BeneficiaryCategory> findBeneficiaryCategory(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotValidException {
        String category = importDTO.getBeneficiaryCategory();
        if (!StringUtils.isEmpty(category)) {
            Map<String, BeneficiaryCategory> beneficiaryCategories = ImportUtils.getProperty(persistedProperties, BENEFICIARY_CATEGORIES_KEY);
            String mappedCategory = (String) ImportUtils.getTenantMapping(persistedProperties, BENEFICIARY_CATEGORIES_KEY, category).orElse(category);
            return Optional.of(ImportUtils.filterMap(beneficiaryCategories, mappedCategory)
                    .orElseThrow(() -> new ObjectNotValidException("import.beneficiaryCategory", category)));
        }
        return Optional.empty();
    }

    @Override
    public void resolveAndSetPaymentMethod(Beneficiary beneficiary, Map<String, Object> persistedProperties) throws ObjectNotValidException {
        PaymentMethod paycheck = utils.getGenericsEntityReference(PaymentMethod.class, PaymentMethodReference.PAYCHECK.getId());
        PaymentMethod voluntary = utils.getGenericsEntityReference(PaymentMethod.class, PaymentMethodReference.VOLUNTARY.getId());
        determinePaycheckCategory(paycheck, beneficiary, persistedProperties);
        determineVoluntaryCategory(voluntary, beneficiary, persistedProperties);
        determineVoluntaryCompany(voluntary, beneficiary, persistedProperties);
        determineDefaultPaymentMethod(voluntary, paycheck, beneficiary);
    }

    @SuppressWarnings("unchecked")
    private void determinePaycheckCategory(PaymentMethod paycheck, Beneficiary beneficiary, Map<String, Object> persistedProperties) throws ObjectNotValidException {
        List<String> paycheckCategories = (List<String>) ImportUtils.getTenantMapping(persistedProperties, PAYCHECK_CATEGORIES_TENANT_MAPPING).orElse(Collections.emptyList());
        BeneficiaryCategory beneficiaryCategory = beneficiary.getBeneficiaryCategory();
        if (beneficiaryCategory != null && paycheckCategories.contains(StringUtils.lowerCase(beneficiaryCategory.getName()))) {
            Optional<Company> optCompany = findCompany(StringUtils.lowerCase(beneficiaryCategory.getName()), persistedProperties);
            optCompany.ifPresent(c -> {
                beneficiary.setCompany(c);
                beneficiary.setPaymentMethod(paycheck);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void determineVoluntaryCategory(PaymentMethod voluntary, Beneficiary beneficiary, Map<String, Object> persistedProperties) {
        if (beneficiary.getPaymentMethod() == null) {
            List<String> voluntaryCategories = (List<String>) ImportUtils.getTenantMapping(persistedProperties, VOLUNTARY_CATEGORIES_TENANT_MAPPING).orElse(Collections.emptyList());
            BeneficiaryCategory beneficiaryCategory = beneficiary.getBeneficiaryCategory();
            if (beneficiaryCategory != null && voluntaryCategories.contains(StringUtils.lowerCase(beneficiaryCategory.getName())))
                beneficiary.setPaymentMethod(voluntary);
        }
    }

    @SuppressWarnings("unchecked")
    private void determineVoluntaryCompany(PaymentMethod voluntary, Beneficiary beneficiary, Map<String, Object> persistedProperties) {
        if (beneficiary.getPaymentMethod() == null) {
            List<String> voluntaryCompanies = (List<String>) ImportUtils.getTenantMapping(persistedProperties, VOLUNTARY_COMPANIES_TENANT_MAPPING).orElse(Collections.emptyList());
            Company company = beneficiary.getCompany();
            if (company != null && voluntaryCompanies.contains(StringUtils.lowerCase(company.getName())))
                beneficiary.setPaymentMethod(voluntary);
        }
    }

    private void determineDefaultPaymentMethod(PaymentMethod voluntary, PaymentMethod paycheck, Beneficiary beneficiary) {
        if (beneficiary.getPaymentMethod() == null) {
            PaymentMethod defaultPaymentMethod = beneficiary.getCompany() != null ? paycheck : voluntary;
            beneficiary.setPaymentMethod(defaultPaymentMethod);
        }
    }

    @Override
    public Optional<Long> resolveWorkIdNumber(BeneficiaryImportDTO importDTO) {
        Optional<Long> parsedWorkIdNumber = ImportUtils.parseLong(importDTO.getWorkIdNumber());
        if (parsedWorkIdNumber.isPresent()) {
            Long workIdNumber = parsedWorkIdNumber.get();
            if (workIdNumber <= 0)
                return Optional.empty();
        }
        return parsedWorkIdNumber;
    }

    @Override
    public Optional<MaritalStatus> findMaritalStatus(BeneficiaryImportDTO beneficiaryImportDTO, Map<String, Object> persistedProperties) {
        Map<String, MaritalStatus> maritalStatuses = ImportUtils.getProperty(persistedProperties, MARITAL_STATUSES_KEY);
        String mappedMaritalStatus = (String) ImportUtils.getTenantMapping(persistedProperties, MARITAL_STATUSES_KEY, beneficiaryImportDTO.getMaritalStatus()).orElse(beneficiaryImportDTO.getMaritalStatus());
        return ImportUtils.filterMap(maritalStatuses, mappedMaritalStatus);
    }

    @Override
    public Optional<Phone> resolvePhone(BeneficiaryImportDTO importDTO) {
        if (StringUtils.isEmpty(importDTO.getPhone()))
            return Optional.empty();
        Optional<Long> phoneNumber = ImportUtils.parseLong(importDTO.getPhone());
        if (phoneNumber.isEmpty())
            return Optional.empty();
        Phone phone = new Phone();
        phone.setPhoneNumber(phoneNumber.get());
        phone.setPhoneType(PhoneType.MOVIL);
        return Optional.of(phone);
    }

    @Override
    public void determineRelatedBeneficiary(Beneficiary beneficiary, BeneficiaryImportDTO importDTO) {
        String relatedBeneficiaryCode = importDTO.getRelatedBeneficiaryCode();
        if (StringUtils.isNotEmpty(relatedBeneficiaryCode)
                && !StringUtils.equals(relatedBeneficiaryCode, beneficiary.getBeneficiaryCode())) {
            Beneficiary holder = new Beneficiary();
            holder.setBeneficiaryCode(relatedBeneficiaryCode);
            beneficiary.setRelatedBeneficiary(holder);
        }
    }

    @Override
    protected void setAuditInfo(Beneficiary beneficiary, ImportProperties properties) {
        JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) properties.getAuthentication();
        String principal = jwtAuthenticationToken.getPrincipal();
        beneficiary.setClientId(jwtAuthenticationToken.getClientId());
        beneficiary.setCreatedBy(principal);
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setModifiedBy(principal);
        beneficiary.setModifiedAt(LocalDateTime.now());
        beneficiary.setTenantId(properties.getTenantId());
    }

}
