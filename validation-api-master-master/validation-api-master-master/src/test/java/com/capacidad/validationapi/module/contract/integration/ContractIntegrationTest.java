package com.capacidad.validationapi.module.contract.integration;

import com.capacidad.validationapi.AuthUtils;
import com.capacidad.validationapi.IntegrationTest;
import com.capacidad.validationapi.TestUtils;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static com.capacidad.validationapi.IntegrationTestConstants.*;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

public class ContractIntegrationTest extends IntegrationTest {
    @MockBean
    ConnectionFactory connectionFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MockMvc mvc;

    @Autowired
    private Utils utils;

    @Autowired
    private AuthUtils authUtils;

    private String jwtAccessToken;

    @Before
    public void init() throws Exception {
        TestUtils.truncateDatabaseTables(applicationContext,
                "contract",
                "contract_item",
                "contract_adjustment",
                "region",
                "medical_specialties_practices",
                "medical_practice",
                "nomenclator",
                "organization",
                "medical_center",
                "practitioner",
                "practitioner_category",
                "practitioner_medical_registrations",
                "practitioner_medical_specialties",
                "practitioner_medical_centers",
                "phone",
                "address");

        TenantContext.clearTenant();
        SecurityContextHolder.clearContext();

        jwtAccessToken = authUtils.obtainAccessToken(
                ADMIN.toLowerCase(),
                "all:beneficiaries,all:categories,all:contracts,all:adjustments," +
                        "all:practitioners,all:regions,all:medical_centers,all:organizations,all:nomenclators,all:medical_registrations",
                null);
    }

    @Test
    public void testBaseContractIsSuccessful() throws Exception {
        JSONObject contract = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contract.put("dateFrom", LocalDate.now().plusMonths(3).toString());
        contract.put("dateTo", LocalDate.now().plusMonths(6).toString());

        JSONObject result = TestUtils.createAndGetObject(this.mvc, ENDPOINT_CONTRACTS, contract.toString(), jwtAccessToken);

        assertResult(result);
        assertThat(result.getString("dateFrom")).isNotNull();
        assertThat(result.getString("dateTo")).isNotNull();
    }

    @Test
    public void testCreateOrganizationContractIsSuccessful() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);

        assertResult(result);
        assertThat(result.getJSONObject("organization").getLong("id")).isEqualTo(organizationId);
        assertThat(result.getString("dateFrom")).isNotNull();
        assertThat(result.getString("dateTo")).isNotNull();
    }

    @Test
    public void testCreatePractitionerContractIsSuccessful() throws Exception {
        long practitionerCategoryId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_PRACTITIONER_CATEGORIES,
                utils.readFileToJsonObject(SCHEMA_PRACTITIONER_CATEGORY_1).toString(),
                jwtAccessToken);

        long organizationId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_ORGANIZATIONS,
                utils.readFileToJsonObject(SCHEMA_ORGANIZATION_3).toString(),
                jwtAccessToken);

        long medicalCenterId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_MEDICAL_CENTERS,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_CENTER_1).toString(),
                jwtAccessToken);

        JSONObject practitionerObj = buildPractitionerObject(practitionerCategoryId, medicalCenterId, organizationId);

        long practitionerId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_PRACTITIONERS,
                practitionerObj.toString(), jwtAccessToken);

        JSONObject contract = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contract.put("dateFrom", LocalDate.now().plusMonths(3).toString());
        contract.put("dateTo", LocalDate.now().plusMonths(6).toString());

        JSONObject practitioner = new JSONObject();
        practitioner.put("id", practitionerId);
        contract.put("practitioner", practitioner);

        JSONObject result = TestUtils.createAndGetObject(this.mvc, ENDPOINT_PRACTITIONER_CONTRACT, contract.toString(), jwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("practitioner").getLong("id")).isEqualTo(practitionerId);
        assertThat(result.getString("dateFrom")).isNotNull();
        assertThat(result.getString("dateTo")).isNotNull();
    }

    @Test
    public void testCreateMedicalCenterContractIsSuccessful() throws Exception {
        long medicalCenterId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_MEDICAL_CENTERS,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_CENTER_1).toString(),
                jwtAccessToken);

        JSONObject contract = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contract.put("dateFrom", LocalDate.now().plusMonths(3).toString());
        contract.put("dateTo", LocalDate.now().plusMonths(6).toString());

        JSONObject medicalCenter = new JSONObject();
        medicalCenter.put("id", medicalCenterId);
        contract.put("medicalCenter", medicalCenter);

        JSONObject result = TestUtils.createAndGetObject(this.mvc, ENDPOINT_MEDICAL_CENTER_CONTRACT, contract.toString(), jwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("medicalCenter").getLong("id")).isEqualTo(medicalCenterId);
        assertThat(result.getString("dateFrom")).isNotNull();
        assertThat(result.getString("dateTo")).isNotNull();
    }

    @Test
    public void testAddNotCategorizedFixedContractItemsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        JSONObject fixedContractItem1 = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        fixedContractItem1.put("nomenclator", nomenclatorObj1);
        fixedContractItem1.put("refundable", false);
        fixedContractItem1.put("value", new BigDecimal("321.47").setScale(2, RoundingMode.HALF_UP));

        JSONObject fixedContractItem2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        fixedContractItem2.put("nomenclator", nomenclatorObj1);
        fixedContractItem2.put("refundable", false);
        fixedContractItem2.put("value", new BigDecimal("412.98").setScale(2, RoundingMode.HALF_UP));

        String fixedContractItemEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_FIXED_CONTRACT_ITEMS);

        TestUtils.createObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem1.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem2.toString(),
                jwtAccessToken);

        JSONObject contractItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(fixedContractItemEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray contractItemsEmbedded = contractItems.getJSONObject(EMBEDDED).getJSONArray("contractItems");

        assertThat(contractItemsEmbedded).hasSize(2);
        contractItemsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getBigDecimal("value")).isNotNull();
            assertThat(itemObj.get("practitionerCategory")).isEqualTo(null);
        });
    }

    @Test
    public void testAddCategorizedFixedContractItemsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        long practitionerCategory1Id = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_PRACTITIONER_CATEGORIES,
                utils.readFileToJsonObject(SCHEMA_PRACTITIONER_CATEGORY_1).toString(),
                jwtAccessToken);
        JSONObject practitionerCategory1Obj = new JSONObject();
        practitionerCategory1Obj.put("id", practitionerCategory1Id);

        long practitionerCategory2Id = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_PRACTITIONER_CATEGORIES,
                utils.readFileToJsonObject(SCHEMA_PRACTITIONER_CATEGORY_2).toString(),
                jwtAccessToken);
        JSONObject practitionerCategory2Obj = new JSONObject();
        practitionerCategory2Obj.put("id", practitionerCategory2Id);

        JSONObject fixedContractItem1 = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        fixedContractItem1.put("nomenclator", nomenclatorObj1);
        fixedContractItem1.put("value", new BigDecimal("321.47").setScale(2, RoundingMode.HALF_UP));
        fixedContractItem1.put("practitionerCategory", practitionerCategory1Obj);
        fixedContractItem1.put("refundable", false);

        JSONObject fixedContractItem2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        fixedContractItem2.put("nomenclator", nomenclatorObj1);
        fixedContractItem2.put("value", new BigDecimal("412.98").setScale(2, RoundingMode.HALF_UP));
        fixedContractItem2.put("practitionerCategory", practitionerCategory2Obj);
        fixedContractItem2.put("refundable", false);

        String fixedContractItemEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_FIXED_CONTRACT_ITEMS);

        TestUtils.createObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem1.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem2.toString(),
                jwtAccessToken);

        JSONObject contractItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(fixedContractItemEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray contractItemsEmbedded = contractItems.getJSONObject(EMBEDDED).getJSONArray("contractItems");

        assertThat(contractItemsEmbedded).hasSize(2);
        contractItemsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getBigDecimal("value")).isNotNull();
            assertThat(itemObj.get("practitionerCategory")).isNotNull();
        });
    }

    @Test
    public void testAddUsageRateCityAdjustmentsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        JSONObject auditRestrictionType = new JSONObject();
        auditRestrictionType.put("id", RestrictionTypeReference.AUDIT.getId());

        JSONObject rejectionRestrictionType = new JSONObject();
        rejectionRestrictionType.put("id", RestrictionTypeReference.REJECTION.getId());

        JSONObject usageRateAdjustment = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        JSONObject cityObj1 = new JSONObject();
        cityObj1.put("id", 1L);
        usageRateAdjustment.put("nomenclator", nomenclatorObj1);
        usageRateAdjustment.put("city", cityObj1);
        usageRateAdjustment.put("threshold", new BigDecimal("6.6").setScale(2, RoundingMode.HALF_UP));
        usageRateAdjustment.put("capitaAmount", 1000L);
        usageRateAdjustment.put("period", Period.YEARLY);
        usageRateAdjustment.put("restrictionType", auditRestrictionType);
        usageRateAdjustment.put("scope", ContractAdjustmentScope.CONTRACT);

        JSONObject usageRateAdjustment2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        JSONObject cityObj2 = new JSONObject();
        cityObj2.put("id", 2L);
        usageRateAdjustment2.put("nomenclator", nomenclatorObj2);
        usageRateAdjustment2.put("city", cityObj2);
        usageRateAdjustment2.put("threshold", new BigDecimal("3.4").setScale(2, RoundingMode.HALF_UP));
        usageRateAdjustment2.put("capitaAmount", 500L);
        usageRateAdjustment2.put("period", Period.MONTHLY);
        usageRateAdjustment2.put("restrictionType", rejectionRestrictionType);
        usageRateAdjustment2.put("scope", ContractAdjustmentScope.CONTRACT);

        String usageRateAdjustmentsEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_USAGE_RATE_ADJUSTMENTS);

        TestUtils.createObject(this.mvc,
                usageRateAdjustmentsEndpoint,
                usageRateAdjustment.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                usageRateAdjustmentsEndpoint,
                usageRateAdjustment2.toString(),
                jwtAccessToken);

        JSONObject adjustments = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(usageRateAdjustmentsEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray adjustmentsEmbedded = adjustments.getJSONObject(EMBEDDED).getJSONArray("adjustments");

        assertThat(adjustmentsEmbedded).hasSize(2);
        adjustmentsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getJSONObject("city")).isNotNull();
            assertThat(itemObj.get("region")).isEqualTo(null);
            assertThat(itemObj.getBigDecimal("threshold")).isNotNull();
            assertThat(itemObj.getLong("capitaAmount")).isNotNull();
            assertThat(itemObj.getString("period")).isNotNull();
            assertThat(itemObj.getJSONObject("restrictionType")).isNotNull();
        });
    }

    @Test
    public void testAddUsageRateRegionAdjustmentsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        long region1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_REGIONS,
                utils.readFileToJsonObject(SCHEMA_REGION_1).toString(), jwtAccessToken);

        long region2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_REGIONS,
                utils.readFileToJsonObject(SCHEMA_REGION_2).toString(), jwtAccessToken);

        JSONObject auditRestrictionType = new JSONObject();
        auditRestrictionType.put("id", RestrictionTypeReference.AUDIT.getId());

        JSONObject rejectionRestrictionType = new JSONObject();
        rejectionRestrictionType.put("id", RestrictionTypeReference.REJECTION.getId());

        JSONObject usageRateAdjustment = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        JSONObject region1Object = new JSONObject();
        region1Object.put("id", region1);
        usageRateAdjustment.put("nomenclator", nomenclatorObj1);
        usageRateAdjustment.put("region", region1Object);
        usageRateAdjustment.put("threshold", new BigDecimal("6.6").setScale(2, RoundingMode.HALF_UP));
        usageRateAdjustment.put("capitaAmount", 1000L);
        usageRateAdjustment.put("period", Period.YEARLY);
        usageRateAdjustment.put("restrictionType", auditRestrictionType);
        usageRateAdjustment.put("scope", ContractAdjustmentScope.CONTRACT);

        JSONObject usageRateAdjustment2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        JSONObject region2Object = new JSONObject();
        region2Object.put("id", region2);
        usageRateAdjustment2.put("nomenclator", nomenclatorObj2);
        usageRateAdjustment2.put("region", region2Object);
        usageRateAdjustment2.put("threshold", new BigDecimal("3.4").setScale(2, RoundingMode.HALF_UP));
        usageRateAdjustment2.put("capitaAmount", 500L);
        usageRateAdjustment2.put("period", Period.MONTHLY);
        usageRateAdjustment2.put("restrictionType", rejectionRestrictionType);
        usageRateAdjustment2.put("scope", ContractAdjustmentScope.CONTRACT);

        String usageRateAdjustmentsEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_USAGE_RATE_ADJUSTMENTS);

        TestUtils.createObject(this.mvc,
                usageRateAdjustmentsEndpoint,
                usageRateAdjustment.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                usageRateAdjustmentsEndpoint,
                usageRateAdjustment2.toString(),
                jwtAccessToken);

        JSONObject adjustments = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(usageRateAdjustmentsEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray adjustmentsEmbedded = adjustments.getJSONObject(EMBEDDED).getJSONArray("adjustments");

        assertThat(adjustmentsEmbedded).hasSize(2);
        adjustmentsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getJSONObject("region")).isNotNull();
            assertThat(itemObj.get("city")).isEqualTo(null);
            assertThat(itemObj.getBigDecimal("threshold")).isNotNull();
            assertThat(itemObj.getLong("capitaAmount")).isNotNull();
            assertThat(itemObj.getString("period")).isNotNull();
            assertThat(itemObj.getJSONObject("restrictionType")).isNotNull();
        });
    }

    @Test
    public void testAddMaximumCityAdjustmentsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        JSONObject auditRestrictionType = new JSONObject();
        auditRestrictionType.put("id", RestrictionTypeReference.AUDIT.getId());

        JSONObject rejectionRestrictionType = new JSONObject();
        rejectionRestrictionType.put("id", RestrictionTypeReference.REJECTION.getId());

        JSONObject maximumAdjustment = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        JSONObject cityObj1 = new JSONObject();
        cityObj1.put("id", 1L);
        maximumAdjustment.put("nomenclator", nomenclatorObj1);
        maximumAdjustment.put("city", cityObj1);
        maximumAdjustment.put("threshold", new BigDecimal(10).setScale(2, RoundingMode.HALF_UP));
        maximumAdjustment.put("period", Period.YEARLY);
        maximumAdjustment.put("restrictionType", auditRestrictionType);
        maximumAdjustment.put("scope", ContractAdjustmentScope.CONTRACT);

        JSONObject maximumAdjustment2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        JSONObject cityObj2 = new JSONObject();
        cityObj2.put("id", 2L);
        maximumAdjustment2.put("nomenclator", nomenclatorObj2);
        maximumAdjustment2.put("city", cityObj2);
        maximumAdjustment2.put("threshold", new BigDecimal(15).setScale(2, RoundingMode.HALF_UP));
        maximumAdjustment2.put("period", Period.MONTHLY);
        maximumAdjustment2.put("restrictionType", rejectionRestrictionType);
        maximumAdjustment2.put("scope", ContractAdjustmentScope.CONTRACT);

        String maximumAdjustmentsEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_MAXIMUM_ADJUSTMENTS);

        TestUtils.createObject(this.mvc,
                maximumAdjustmentsEndpoint,
                maximumAdjustment.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                maximumAdjustmentsEndpoint,
                maximumAdjustment2.toString(),
                jwtAccessToken);

        JSONObject adjustments = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(maximumAdjustmentsEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray adjustmentsEmbedded = adjustments.getJSONObject(EMBEDDED).getJSONArray("adjustments");

        assertThat(adjustmentsEmbedded).hasSize(2);
        adjustmentsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getJSONObject("city")).isNotNull();
            assertThat(itemObj.get("region")).isEqualTo(null);
            assertThat(itemObj.getBigDecimal("threshold")).isNotNull();
            assertThat(itemObj.getString("period")).isNotNull();
            assertThat(itemObj.getJSONObject("restrictionType")).isNotNull();
        });
    }

    @Test
    public void testAddMaximumRegionAdjustmentsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        long region1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_REGIONS,
                utils.readFileToJsonObject(SCHEMA_REGION_1).toString(), jwtAccessToken);

        long region2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_REGIONS,
                utils.readFileToJsonObject(SCHEMA_REGION_2).toString(), jwtAccessToken);

        JSONObject auditRestrictionType = new JSONObject();
        auditRestrictionType.put("id", RestrictionTypeReference.AUDIT.getId());

        JSONObject rejectionRestrictionType = new JSONObject();
        rejectionRestrictionType.put("id", RestrictionTypeReference.REJECTION.getId());

        JSONObject maximumAdjustment = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        JSONObject region1Object = new JSONObject();
        region1Object.put("id", region1);
        maximumAdjustment.put("nomenclator", nomenclatorObj1);
        maximumAdjustment.put("region", region1Object);
        maximumAdjustment.put("threshold", new BigDecimal(10).setScale(2, RoundingMode.HALF_UP));
        maximumAdjustment.put("period", Period.YEARLY);
        maximumAdjustment.put("restrictionType", auditRestrictionType);
        maximumAdjustment.put("scope", ContractAdjustmentScope.CONTRACT);

        JSONObject maximumAdjustment2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        JSONObject region2Object = new JSONObject();
        region2Object.put("id", region2);
        maximumAdjustment2.put("nomenclator", nomenclatorObj2);
        maximumAdjustment2.put("region", region2Object);
        maximumAdjustment2.put("threshold", new BigDecimal(15).setScale(2, RoundingMode.HALF_UP));
        maximumAdjustment2.put("period", Period.MONTHLY);
        maximumAdjustment2.put("restrictionType", rejectionRestrictionType);
        maximumAdjustment2.put("scope", ContractAdjustmentScope.CONTRACT);

        String maximumAdjustmentsEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_MAXIMUM_ADJUSTMENTS);

        TestUtils.createObject(this.mvc,
                maximumAdjustmentsEndpoint,
                maximumAdjustment.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                maximumAdjustmentsEndpoint,
                maximumAdjustment2.toString(),
                jwtAccessToken);

        JSONObject adjustments = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(maximumAdjustmentsEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray adjustmentsEmbedded = adjustments.getJSONObject(EMBEDDED).getJSONArray("adjustments");

        assertThat(adjustmentsEmbedded).hasSize(2);
        adjustmentsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getJSONObject("region")).isNotNull();
            assertThat(itemObj.get("city")).isEqualTo(null);
            assertThat(itemObj.getBigDecimal("threshold")).isNotNull();
            assertThat(itemObj.getString("period")).isNotNull();
            assertThat(itemObj.getJSONObject("restrictionType")).isNotNull();
        });
    }

    @Test
    public void testAddMonetaryCityAdjustmentsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        JSONObject auditRestrictionType = new JSONObject();
        auditRestrictionType.put("id", RestrictionTypeReference.AUDIT.getId());

        JSONObject rejectionRestrictionType = new JSONObject();
        rejectionRestrictionType.put("id", RestrictionTypeReference.REJECTION.getId());

        JSONObject monetaryAdjustment = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        JSONObject cityObj1 = new JSONObject();
        cityObj1.put("id", 1L);
        monetaryAdjustment.put("nomenclator", nomenclatorObj1);
        monetaryAdjustment.put("city", cityObj1);
        monetaryAdjustment.put("threshold", new BigDecimal(10).setScale(2, RoundingMode.HALF_UP));
        monetaryAdjustment.put("period", Period.YEARLY);
        monetaryAdjustment.put("restrictionType", auditRestrictionType);
        monetaryAdjustment.put("scope", ContractAdjustmentScope.CONTRACT);

        JSONObject monetaryAdjustment2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        JSONObject cityObj2 = new JSONObject();
        cityObj2.put("id", 2L);
        monetaryAdjustment2.put("nomenclator", nomenclatorObj2);
        monetaryAdjustment2.put("city", cityObj2);
        monetaryAdjustment2.put("threshold", new BigDecimal(15).setScale(2, RoundingMode.HALF_UP));
        monetaryAdjustment2.put("period", Period.MONTHLY);
        monetaryAdjustment2.put("restrictionType", rejectionRestrictionType);
        monetaryAdjustment2.put("scope", ContractAdjustmentScope.CONTRACT);

        String monetaryAdjustmentsEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_MONETARY_ADJUSTMENTS);

        TestUtils.createObject(this.mvc,
                monetaryAdjustmentsEndpoint,
                monetaryAdjustment.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                monetaryAdjustmentsEndpoint,
                monetaryAdjustment2.toString(),
                jwtAccessToken);

        JSONObject adjustments = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(monetaryAdjustmentsEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray adjustmentsEmbedded = adjustments.getJSONObject(EMBEDDED).getJSONArray("adjustments");

        assertThat(adjustmentsEmbedded).hasSize(2);
        adjustmentsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getJSONObject("city")).isNotNull();
            assertThat(itemObj.get("region")).isEqualTo(null);
            assertThat(itemObj.getBigDecimal("threshold")).isNotNull();
            assertThat(itemObj.getString("period")).isNotNull();
            assertThat(itemObj.getJSONObject("restrictionType")).isNotNull();
        });
    }

    @Test
    public void testAddMonetaryRegionAdjustmentsToOrganizationContract() throws Exception {
        long organizationId = createOrganization();
        JSONObject result = createOrganizationContract(organizationId);
        long contractId = result.getLong("id");

        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), jwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), jwtAccessToken);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), jwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), jwtAccessToken);

        long region1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_REGIONS,
                utils.readFileToJsonObject(SCHEMA_REGION_1).toString(), jwtAccessToken);

        long region2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_REGIONS,
                utils.readFileToJsonObject(SCHEMA_REGION_2).toString(), jwtAccessToken);

        JSONObject auditRestrictionType = new JSONObject();
        auditRestrictionType.put("id", RestrictionTypeReference.AUDIT.getId());

        JSONObject rejectionRestrictionType = new JSONObject();
        rejectionRestrictionType.put("id", RestrictionTypeReference.REJECTION.getId());

        JSONObject monetaryAdjustment = new JSONObject();
        JSONObject nomenclatorObj1 = new JSONObject();
        nomenclatorObj1.put("id", nomenclator1);
        JSONObject region1Object = new JSONObject();
        region1Object.put("id", region1);
        monetaryAdjustment.put("nomenclator", nomenclatorObj1);
        monetaryAdjustment.put("region", region1Object);
        monetaryAdjustment.put("threshold", new BigDecimal(10).setScale(2, RoundingMode.HALF_UP));
        monetaryAdjustment.put("period", Period.YEARLY);
        monetaryAdjustment.put("restrictionType", auditRestrictionType);
        monetaryAdjustment.put("scope", ContractAdjustmentScope.CONTRACT);

        JSONObject monetaryAdjustment2 = new JSONObject();
        JSONObject nomenclatorObj2 = new JSONObject();
        nomenclatorObj2.put("id", nomenclator2);
        JSONObject region2Object = new JSONObject();
        region2Object.put("id", region2);
        monetaryAdjustment2.put("nomenclator", nomenclatorObj2);
        monetaryAdjustment2.put("region", region2Object);
        monetaryAdjustment2.put("threshold", new BigDecimal(15).setScale(2, RoundingMode.HALF_UP));
        monetaryAdjustment2.put("period", Period.MONTHLY);
        monetaryAdjustment2.put("restrictionType", rejectionRestrictionType);
        monetaryAdjustment2.put("scope", ContractAdjustmentScope.CONTRACT);

        String monetaryAdjustmentsEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_MONETARY_ADJUSTMENTS);

        TestUtils.createObject(this.mvc,
                monetaryAdjustmentsEndpoint,
                monetaryAdjustment.toString(),
                jwtAccessToken);

        TestUtils.createObject(this.mvc,
                monetaryAdjustmentsEndpoint,
                monetaryAdjustment2.toString(),
                jwtAccessToken);

        JSONObject adjustments = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(monetaryAdjustmentsEndpoint, "?page=1&size=10"),
                jwtAccessToken);

        JSONArray adjustmentsEmbedded = adjustments.getJSONObject(EMBEDDED).getJSONArray("adjustments");

        assertThat(adjustmentsEmbedded).hasSize(2);
        adjustmentsEmbedded.forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("nomenclator")).isNotNull();
            assertThat(itemObj.getJSONObject("region")).isNotNull();
            assertThat(itemObj.get("city")).isEqualTo(null);
            assertThat(itemObj.getBigDecimal("threshold")).isNotNull();
            assertThat(itemObj.getString("period")).isNotNull();
            assertThat(itemObj.getJSONObject("restrictionType")).isNotNull();
        });
    }

    private void assertResult(JSONObject result) {
        assertThat(result).isNotNull();
        assertThat(result.getString("createdAt")).isNotEmpty();
        assertThat(result.getString("name")).isNotBlank();
        assertThat(result.getString("contractCode")).isNotBlank();
        assertThat(result.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_FIXED_CONTRACT_ITEMS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_MAXIMUM_ADJUSTMENTS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_USAGE_RATE_ADJUSTMENTS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_MONETARY_ADJUSTMENTS)).isNotNull();
    }

    private JSONObject buildPractitionerObject(long practitionerCategoryId, long medicalCenterId, long organizationId) {
        JSONObject practitionerCategory = new JSONObject();
        practitionerCategory.put("id", practitionerCategoryId);

        JSONObject organization = new JSONObject();
        organization.put("id", organizationId);

        JSONObject medicalCenter = new JSONObject();
        medicalCenter.put("id", medicalCenterId);

        JSONObject practitioner = utils.readFileToJsonObject(SCHEMA_PRACTITIONER);
        practitioner.put("practitionerCategory", practitionerCategory);

        JSONArray medicalCenters = new JSONArray();
        medicalCenters.put(medicalCenter);
        practitioner.put("medicalCenters", medicalCenters);

        JSONArray medicalRegistrations = new JSONArray();
        JSONObject medicalRegistration = new JSONObject();
        medicalRegistration.put("organization", organization);
        medicalRegistration.put("registrationCode", "123456");
        medicalRegistrations.put(medicalRegistration);
        practitioner.put("medicalRegistrations", medicalRegistrations);
        return practitioner;
    }

    private JSONObject createOrganizationContract(long organizationId) throws Exception {
        JSONObject contract = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contract.put("dateFrom", LocalDate.now().plusMonths(3).toString());
        contract.put("dateTo", LocalDate.now().plusMonths(6).toString());

        JSONObject organization = new JSONObject();
        organization.put("id", organizationId);
        contract.put("organization", organization);

        return TestUtils.createAndGetObject(this.mvc, ENDPOINT_ORGANIZATION_CONTRACT, contract.toString(), jwtAccessToken);
    }

    private long createOrganization() throws Exception {
        return TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_ORGANIZATIONS,
                utils.readFileToJsonObject(SCHEMA_ORGANIZATION_1).toString(), jwtAccessToken);
    }


}
