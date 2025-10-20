package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryCategoryService;
import com.capacidad.validationapi.module.company.service.CompanyService;
import com.capacidad.validationapi.module.importprocessor.service.ImportPropertiesInitializer;
import com.capacidad.validationapi.module.insuranceplan.service.InsurancePlanService;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Province;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.model.MaritalStatus;
import com.capacidad.validationapi.module.person.model.RelationshipType;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportConstants.*;

@Component
@Log4j2
public class BeneficiaryImportPropertiesInitializer implements ImportPropertiesInitializer {

    private static final String ID_TEXT_SQL_REGEX = "(\\s?\\p{Digit}+)\\s?,\\s?'(.*)'";
    private static final String ID_TEXT_NULL_ID_SQL_REGEX = String.format("%s\\s?,\\s?null\\s?,(\\s?\\p{Digit}+)", ID_TEXT_SQL_REGEX);
    private final InsurancePlanService insurancePlanService;
    private final BeneficiaryCategoryService beneficiaryCategoryService;
    private final PropertiesService propertiesService;
    private final CompanyService companyService;
    private final Map<String, Map<String, City>> cityMap;
    private final Map<String, IdType> idTypeMap;
    private final Map<String, RelationshipType> relationshipTypeMap;
    private final Map<String, MaritalStatus> maritalStatusMap;
    @PersistenceContext(unitName = "processing")
    private EntityManager processingEntityManager;

    public BeneficiaryImportPropertiesInitializer(InsurancePlanService insurancePlanService,
                                                  BeneficiaryCategoryService beneficiaryCategoryService,
                                                  PropertiesService propertiesService,
                                                  CompanyService companyService) {
        this.insurancePlanService = insurancePlanService;
        this.beneficiaryCategoryService = beneficiaryCategoryService;
        this.propertiesService = propertiesService;
        this.companyService = companyService;
        this.cityMap = initializeCityObjectsMap();
        this.idTypeMap = initializeIdTypeMap();
        this.relationshipTypeMap = initializeRelationshipTypeMap();
        this.maritalStatusMap = initializeMaritalStatusMap();
    }

    @Override
    public Map<String, Object> initializeProperties() {
        Map<String, Object> persistedProperties = new HashMap<>();
        persistedProperties.put(CITIES_KEY, cityMap);
        persistedProperties.put(ID_TYPES_KEY, idTypeMap);
        persistedProperties.put(MARITAL_STATUSES_KEY, maritalStatusMap);
        persistedProperties.put(RELATIONSHIP_TYPES_KEY, relationshipTypeMap);
        persistedProperties.put(INSURANCE_PLANS_KEY, insurancePlanService
                .findAllInsurancePlansTypedQuery(this.getProcessingEntityManager()).stream()
                .collect(Collectors.toMap(insurancePlan -> StringUtils.lowerCase(insurancePlan.getName()), Function.identity())));
        persistedProperties.put(BENEFICIARY_CATEGORIES_KEY, beneficiaryCategoryService
                .findAllBeneficiaryCategoriesTypedQuery(this.getProcessingEntityManager()).stream()
                .collect(Collectors.toMap(beneficiaryCategory -> StringUtils.lowerCase(beneficiaryCategory.getName()), Function.identity())));
        persistedProperties.put(TENANT_MAPPINGS, propertiesService
                .getPropertiesTypedQuery(this.getProcessingEntityManager()).getMappings());
        persistedProperties.put(COMPANIES_KEY, companyService
                .findAllCompaniesTypedQuery(this.getProcessingEntityManager()).stream()
                .collect(Collectors.toMap(company -> StringUtils.lowerCase(company.getName()), Function.identity())));
        return persistedProperties;
    }

    private Map<String, Map<String, City>> initializeCityObjectsMap() {
        Optional<Resource[]> provinceResources = Utils.readResourcesFromPattern("/data/cities/*.sql");
        Map<Long, Province> provinceMap = new ConcurrentHashMap<>();
        if (provinceResources.isPresent()) {
            Resource[] resources = provinceResources.get();
            for (Resource provinceResource : resources)
                addProvinceMatchesToMap(provinceMap, provinceResource);
        }
        return buildCityMap(provinceMap);
    }

    private void addProvinceMatchesToMap(Map<Long, Province> provinceMap, Resource resource) {
        Pattern pattern = Pattern.compile(ID_TEXT_NULL_ID_SQL_REGEX);
        pattern.matcher(Utils.readResourceAsString(resource))
                .results().parallel().forEach(r -> {
            Long provinceId = Long.valueOf(StringUtils.trim(r.group(3)));
            Province province = provinceMap.computeIfAbsent(provinceId,
                    p -> {
                        Province newProvince = new Province();
                        newProvince.setId(provinceId);
                        return newProvince;
                    });
            City city = new City();
            city.setId(Long.valueOf(StringUtils.trim(r.group(1))));
            city.setName(StringUtils.trim(r.group(2)));
            province.getCities().add(city);
        });
    }

    private Map<String, Map<String, City>> buildCityMap(Map<Long, Province> provinceMap) {
        List<Province> provinceList = buildProvinceList(provinceMap);
        Map<String, Map<String, City>> cityObjectsMap = new HashMap<>();
        provinceList.forEach(p -> {
            Map<String, City> citiesMap = p.getCities().stream()
                    .collect(Collectors.toMap(city -> StringUtils.lowerCase(city.getName()), Function.identity()));
            cityObjectsMap.put(StringUtils.lowerCase(p.getName()), citiesMap);
        });
        return cityObjectsMap;
    }

    private List<Province> buildProvinceList(Map<Long, Province> provinceMap) {
        String provinceFileData = Utils.readResourceAsString("/data/provinces.sql");
        Pattern pattern = Pattern.compile(ID_TEXT_SQL_REGEX);
        return pattern.matcher(provinceFileData)
                .results()
                .map(r -> {
                    Province province = provinceMap.get(Long.valueOf(StringUtils.trim(r.group(1))));
                    province.setName(StringUtils.lowerCase(StringUtils.trim(r.group(2))));
                    return province;
                })
                .collect(Collectors.toList());
    }

    private Map<String, IdType> initializeIdTypeMap() {
        String idTypesFileData = Utils.readResourceAsString("/data/id_types.sql");
        Pattern pattern = Pattern.compile(String.format("%s\\s?,\\s?'(.*)'", ID_TEXT_SQL_REGEX));
        return pattern.matcher(idTypesFileData)
                .results()
                .map(r -> {
                    IdType idType = new IdType();
                    idType.setId(Long.valueOf(StringUtils.trim(r.group(1))));
                    idType.setName(StringUtils.trim(r.group(2)));
                    idType.setAlias(StringUtils.trim(r.group(3)));
                    return idType;
                })
                .collect(Collectors.toMap(idType -> StringUtils.lowerCase(idType.getAlias()), Function.identity()));
    }

    private Map<String, RelationshipType> initializeRelationshipTypeMap() {
        String idTypesFileData = Utils.readResourceAsString("/data/relationship_types.sql");
        Pattern pattern = Pattern.compile(ID_TEXT_SQL_REGEX);
        return pattern.matcher(idTypesFileData)
                .results()
                .map(r -> {
                    RelationshipType relationshipType = new RelationshipType();
                    relationshipType.setId(Long.valueOf(StringUtils.trim(r.group(1))));
                    relationshipType.setName(StringUtils.trim(r.group(2)));
                    return relationshipType;
                })
                .collect(Collectors.toMap(relationshipType -> StringUtils.lowerCase(relationshipType.getName()), Function.identity()));
    }

    private Map<String, MaritalStatus> initializeMaritalStatusMap() {
        String idTypesFileData = Utils.readResourceAsString("/data/marital_status.sql");
        Pattern pattern = Pattern.compile(ID_TEXT_SQL_REGEX);
        return pattern.matcher(idTypesFileData)
                .results()
                .map(r -> {
                    MaritalStatus maritalStatus = new MaritalStatus();
                    maritalStatus.setId(Long.valueOf(StringUtils.trim(r.group(1))));
                    maritalStatus.setName(StringUtils.trim(r.group(2)));
                    return maritalStatus;
                })
                .collect(Collectors.toMap(maritalStatus -> StringUtils.lowerCase(maritalStatus.getName()), Function.identity()));
    }

    protected EntityManager getProcessingEntityManager() {
        return this.processingEntityManager;
    }

}
