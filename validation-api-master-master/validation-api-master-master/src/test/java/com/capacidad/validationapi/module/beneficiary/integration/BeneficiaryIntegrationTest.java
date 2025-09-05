package com.capacidad.validationapi.module.beneficiary.integration;

import com.capacidad.validationapi.AuthUtils;
import com.capacidad.validationapi.IntegrationTest;
import com.capacidad.validationapi.IntegrationTestConstants;
import com.capacidad.validationapi.TestUtils;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.person.reference.RelationshipTypeReference;
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

import java.time.LocalDateTime;

import static com.capacidad.validationapi.IntegrationTestConstants.*;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BeneficiaryIntegrationTest extends IntegrationTest {

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

    private JSONObject beneficiaryCategory, insurancePlan;

    @Before
    public void init() throws Exception {
        TestUtils.truncateDatabaseTables(applicationContext,
                "insurance_plan",
                "beneficiary",
                "beneficiary_category",
                "beneficiary_disease",
                "beneficiary_insurance_plan",
                "expiration",
                "address",
                "phone");

        TenantContext.clearTenant();
        SecurityContextHolder.clearContext();

        jwtAccessToken = authUtils.obtainAccessToken(
                ADMIN.toLowerCase(),
                "all:beneficiaries,all:categories,all:insurance_plans,all:certificates,all:expirations",
                null);

        long beneficiaryCategoryId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_BENEFICIARY_CATEGORIES,
                utils.readFileToJsonObject(SCHEMA_BENEFICIARY_CATEGORY).toString(),
                jwtAccessToken);

        long insurancePlanId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_INSURANCE_PLANS,
                utils.readFileToJsonObject(SCHEMA_INSURANCE_PLAN_1).toString(),
                jwtAccessToken);

        beneficiaryCategory = new JSONObject();
        beneficiaryCategory.put("id", beneficiaryCategoryId);

        insurancePlan = new JSONObject();
        insurancePlan.put("id", insurancePlanId);
    }


    @Test
    public void testCreateBeneficiaryIsSuccessful() throws Exception {
        JSONObject beneficiary = buildBeneficiaryObject();

        JSONObject result = TestUtils.createAndGetObject(this.mvc, ControllerEndpoints.ENDPOINT_BENEFICIARIES,
                beneficiary.toString(), jwtAccessToken);

        assertResult(result);
        assertThat(result.getLong("workIdNumber")).isNotNull();
        assertThat(result.getJSONObject("beneficiaryCategory")).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_BUDGETS)).isNotNull();
    }

    @Test
    public void testAddValidRelativeToBeneficiaryIsSuccessful() throws Exception {
        JSONObject holderBeneficiary = buildBeneficiaryObject();
        long holderBeneficiaryId = TestUtils.createAndReturnLocationId(this.mvc, ControllerEndpoints.ENDPOINT_BENEFICIARIES,
                holderBeneficiary.toString(), jwtAccessToken);

        JSONObject relativeBeneficiary = utils.readFileToJsonObject(IntegrationTestConstants.SCHEMA_RELATED_BENEFICIARY);
        JSONArray insurancePlans = new JSONArray();
        JSONObject beneficiaryInsurancePlan = new JSONObject();
        beneficiaryInsurancePlan.put("insurancePlan", insurancePlan);
        insurancePlans.put(beneficiaryInsurancePlan);
        relativeBeneficiary.put("beneficiaryInsurancePlans", insurancePlans);

        String holderRelativeEndpoint = StringUtils.join(ENDPOINT_BENEFICIARIES, SLASH, holderBeneficiaryId, SLASH, "relatives");

        long relativeId = TestUtils.createAndReturnLocationId(this.mvc, holderRelativeEndpoint,
                relativeBeneficiary.toString(), jwtAccessToken);
        JSONObject relative = TestUtils.getAndReturnObject(this.mvc, StringUtils.join(ENDPOINT_BENEFICIARIES, SLASH, relativeId), jwtAccessToken);

        assertResult(relative);
        assertThat(relative.get("workIdNumber")).isEqualTo(null);
        assertThat(relative.getJSONObject(LINKS).getJSONObject("relatedBeneficiary")).isNotNull();
    }

    @Test
    public void testPatchBeneficiaryIsSuccessful() throws Exception {
        JSONObject beneficiary = buildBeneficiaryObject();
        long beneficiaryId = TestUtils.createAndReturnLocationId(this.mvc, ControllerEndpoints.ENDPOINT_BENEFICIARIES,
                beneficiary.toString(), jwtAccessToken);

        JSONObject beneficiaryPatchObject = new JSONObject();
        beneficiaryPatchObject.put("beneficiaryCode", 321321321);
        beneficiaryPatchObject.put("name", "anotherName");
        beneficiaryPatchObject.put("lastName", "anotherLastName");
        beneficiaryPatchObject.put("idNumber", 333444555);

        JSONObject childRelationshipType = new JSONObject();
        childRelationshipType.put("id", RelationshipTypeReference.SON.getId());
        beneficiaryPatchObject.put("relationshipType", childRelationshipType);

        JSONObject updatedBeneficiary = TestUtils.patchObject(this.mvc, StringUtils.join(ENDPOINT_BENEFICIARIES, SLASH, beneficiaryId), beneficiaryPatchObject.toString(), jwtAccessToken);

        assertThat(updatedBeneficiary.getJSONObject("relationshipType").getLong("id")).isEqualTo(RelationshipTypeReference.SON.getId());
        assertThat(updatedBeneficiary.getString("name")).isEqualTo(beneficiaryPatchObject.getString("name"));
        assertThat(updatedBeneficiary.getString("lastName")).isEqualTo(beneficiaryPatchObject.getString("lastName"));
        assertThat(updatedBeneficiary.getLong("idNumber")).isEqualTo(beneficiaryPatchObject.getLong("idNumber"));
    }

    @Test
    public void testUpdateBeneficiaryStatusIsSuccessful() throws Exception {
        JSONObject beneficiary = buildBeneficiaryObject();
        long beneficiaryId = TestUtils.createAndReturnLocationId(this.mvc, ControllerEndpoints.ENDPOINT_BENEFICIARIES,
                beneficiary.toString(), jwtAccessToken);

        JSONObject beneficiaryPutObject = new JSONObject();
        JSONObject noCoverageStatusObject = new JSONObject();
        noCoverageStatusObject.put("id", StatusReference.BENEFICIARY_WITHOUT_COVERAGE.getId());
        beneficiaryPutObject.put("status", noCoverageStatusObject);
        beneficiaryPutObject.put("statusUpdateDescription", "update reason");

        JSONObject updatedBeneficiary = TestUtils.putObject(this.mvc, StringUtils.join(ENDPOINT_BENEFICIARIES, SLASH, beneficiaryId, SLASH, "status"),
                beneficiaryPutObject.toString(),
                jwtAccessToken);

        assertThat(updatedBeneficiary.getJSONObject("status").getLong("id")).isEqualTo(StatusReference.BENEFICIARY_WITHOUT_COVERAGE.getId());
    }

    @Test
    public void testAddExpirationToBeneficiaryIsSuccessful() throws Exception {
        JSONObject beneficiary = buildBeneficiaryObject();
        long beneficiaryId = TestUtils.createAndReturnLocationId(this.mvc, ControllerEndpoints.ENDPOINT_BENEFICIARIES,
                beneficiary.toString(), jwtAccessToken);

        JSONObject expiration = utils.readFileToJsonObject(SCHEMA_EXPIRATION);
        LocalDateTime expirationDate = LocalDateTime.now().plusMonths(6);
        expiration.put("expirationDate", expirationDate);
        JSONObject result = TestUtils.createAndGetObject(this.mvc, StringUtils.join(ENDPOINT_BENEFICIARIES, SLASH, beneficiaryId, ENDPOINT_EXPIRATIONS),
                expiration.toString(), jwtAccessToken);

        assertThat(result).isNotNull();
        assertThat(result.getString("reason")).isNotBlank();
        assertThat(result.getString("expirationDate")).isNotBlank();
    }

    @Test
    public void testAddExpirationFailsWhenBeneficiaryAlreadyContainsValidExpiration() throws Exception {
        JSONObject beneficiary = buildBeneficiaryObject();
        long beneficiaryId = TestUtils.createAndReturnLocationId(this.mvc, ControllerEndpoints.ENDPOINT_BENEFICIARIES,
                beneficiary.toString(), jwtAccessToken);

        JSONObject expiration = utils.readFileToJsonObject(SCHEMA_EXPIRATION);
        LocalDateTime expirationDate = LocalDateTime.now().plusMonths(6);
        expiration.put("expirationDate", expirationDate);

        TestUtils.createObject(this.mvc, StringUtils.join(ENDPOINT_BENEFICIARIES, SLASH, beneficiaryId, ENDPOINT_EXPIRATIONS),
                expiration.toString(), jwtAccessToken);

        LocalDateTime expirationDate2 = LocalDateTime.now().plusMonths(8);
        expiration.put("expirationDate", expirationDate2);

        TestUtils.createObject(this.mvc, StringUtils.join(ENDPOINT_BENEFICIARIES, SLASH, beneficiaryId, ENDPOINT_EXPIRATIONS),
                expiration.toString(), status().isConflict(), jwtAccessToken);
    }

    private void assertResult(JSONObject result) {
        assertThat(result).isNotNull();
        assertThat(result.getString("createdAt")).isNotEmpty();
        assertThat(result.getLong("id")).isNotNull();
        assertThat(result.getString("resourceId")).isNotBlank();
        assertThat(result.getLong("idNumber")).isNotNull();
        assertThat(result.getString("name")).isNotBlank();
        assertThat(result.getString("lastName")).isNotBlank();
        assertThat(result.getString("birthDate")).isNotBlank();
        assertThat(result.getString("gender")).isNotBlank();
        assertThat(result.getString("beneficiaryCode")).isNotBlank();
        assertThat(result.getJSONObject("idType")).isNotNull();
        assertThat(result.getJSONObject("relationshipType")).isNotNull();
        assertThat(result.getJSONObject("paymentMethod")).isNotNull();
        assertThat(result.getBoolean("activeBatch")).isFalse();
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(StatusReference.BENEFICIARY_WITH_COVERAGE.getId());
        assertThat(result.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_INSURANCE_PLANS)).hasSize(1);
        assertThat(result.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_ICD10DISEASES)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_EXPIRATIONS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_CHARGES)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_CONTACT_INFO)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_AUTHORIZATIONS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_RELATIVES)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_EXPIRATIONS)).isNotNull();
    }

    private JSONObject buildBeneficiaryObject() {
        JSONObject beneficiaryJsonObject = utils.readFileToJsonObject(IntegrationTestConstants.SCHEMA_BENEFICIARY);
        JSONArray insurancePlans = new JSONArray();
        JSONObject beneficiaryInsurancePlan = new JSONObject();
        beneficiaryInsurancePlan.put("insurancePlan", insurancePlan);
        insurancePlans.put(beneficiaryInsurancePlan);
        beneficiaryJsonObject.put("beneficiaryInsurancePlans", insurancePlans);
        beneficiaryJsonObject.put("beneficiaryCategory", beneficiaryCategory);
        return beneficiaryJsonObject;
    }

}
