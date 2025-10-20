package com.capacidad.validationapi.module.procedure.integration;

import com.capacidad.validationapi.AuthUtils;
import com.capacidad.validationapi.IntegrationTest;
import com.capacidad.validationapi.IntegrationTestConstants;
import com.capacidad.validationapi.TestUtils;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.procedure.model.CUDProcedure;
import com.capacidad.validationapi.module.procedure.model.CertificateProcedure;
import com.capacidad.validationapi.module.procedure.model.DisabilityProcedure;
import com.capacidad.validationapi.module.procedure.model.ProcedureResolution;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static com.capacidad.validationapi.IntegrationTestConstants.SCHEMA_INSURANCE_PLAN_1;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ADMIN;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.BENEFICIARY;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ProcedureIntegrationTest extends IntegrationTest {

    @MockBean
    ConnectionFactory connectionFactory;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Utils utils;

    @Autowired
    private AuthUtils authUtils;

    private String adminJwtAccessToken, beneficiaryJwtAccessToken;

    private JSONObject beneficiaryObj, insurancePlanObj;

    @Before
    public void init() throws Exception {
        TestUtils.truncateDatabaseTables(applicationContext,
                "procedure",
                "cud_procedure_diagnosis",
                "insurance_plan",
                "beneficiary",
                "beneficiary_category",
                "beneficiary_insurance_plan",
                "address",
                "phone");

        adminJwtAccessToken = authUtils.obtainAccessToken(ADMIN.toLowerCase(),
                "all:beneficiaries,all:procedures,all:insurance_plans",
                null);

        long insurancePlanId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_INSURANCE_PLANS,
                utils.readFileToJsonObject(SCHEMA_INSURANCE_PLAN_1).toString(),
                adminJwtAccessToken);

        insurancePlanObj = new JSONObject();
        insurancePlanObj.put("id", insurancePlanId);

        initBeneficiaries();

        beneficiaryJwtAccessToken = authUtils.obtainAccessToken(BENEFICIARY.toLowerCase(),
                "read:beneficiaries,read:procedures,create:procedures",
                UUID.fromString(beneficiaryObj.getString("resourceId")));
    }

    @Test
    public void testCreateAndApproveCUDProcedure() throws Exception {
        JSONObject cudProcedureObj = new JSONObject();
        cudProcedureObj.put("description", "cud procedure");
        cudProcedureObj.put("beneficiary", beneficiaryObj);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("file", Collections.singletonList(""));
        params.put("body", Collections.singletonList(cudProcedureObj.toString()));

        JSONObject result = TestUtils.createFormAndGetObject(this.mvc,
                ControllerEndpoints.ENDPOINT_CUD_PROCEDURES, beneficiaryJwtAccessToken, params);

        assertProcedure(result, CUDProcedure.class);
        assertThat(result.getJSONArray("diagnosis")).isEmpty();

        JSONObject cudProcedureResolution = new JSONObject();
        cudProcedureResolution.put("resolution", ProcedureResolution.APPROVE.toString());
        cudProcedureResolution.put("expiration", LocalDate.now().plusDays(10));
        JSONObject diagnosis = new JSONObject();
        diagnosis.put("id", 1);
        JSONArray diagnosisArray = new JSONArray();
        diagnosisArray.put(diagnosis);
        cudProcedureResolution.put("diagnosis", diagnosisArray);

        String procedureEndpoint = StringUtils.join(ENDPOINT_CUD_PROCEDURES, SLASH, result.getLong("id"));

        JSONObject resolveResult = TestUtils.putObject(this.mvc,
                procedureEndpoint, cudProcedureResolution.toString(), adminJwtAccessToken);

        assertThat(resolveResult.getString("closedAt")).isNotEmpty();
        assertThat(resolveResult.getJSONObject("status").getLong("id")).isEqualTo(PROCEDURE_APPROVED.getId());
        assertThat(resolveResult.getString("expiration")).isNotEmpty();
        assertThat(resolveResult.getJSONArray("diagnosis")).hasSize(diagnosisArray.length());
    }

    @Test
    public void testCreateAndApproveCertificateProcedure() throws Exception {
        JSONObject certificateProcedureObj = new JSONObject();
        JSONObject certificateType = new JSONObject();
        certificateType.put("id", 1);
        certificateProcedureObj.put("description", "certificate procedure");
        certificateProcedureObj.put("beneficiary", beneficiaryObj);
        certificateProcedureObj.put("certificateType", certificateType);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("file", Collections.singletonList(""));
        params.put("body", Collections.singletonList(certificateProcedureObj.toString()));

        JSONObject result = TestUtils.createFormAndGetObject(this.mvc,
                ENDPOINT_CERTIFICATE_PROCEDURES, beneficiaryJwtAccessToken, params);

        assertProcedure(result, CertificateProcedure.class);
        assertThat(result.getJSONObject("certificateType")).isNotNull();

        JSONObject certificateProcedureResolution = new JSONObject();
        certificateProcedureResolution.put("resolution", ProcedureResolution.APPROVE.toString());
        certificateProcedureResolution.put("expiration", LocalDate.now().plusDays(10));

        String procedureEndpoint = StringUtils.join(ENDPOINT_CERTIFICATE_PROCEDURES, SLASH, result.getLong("id"));

        JSONObject resolveResult = TestUtils.putObject(this.mvc,
                procedureEndpoint, certificateProcedureResolution.toString(), adminJwtAccessToken);

        assertThat(resolveResult.getString("closedAt")).isNotEmpty();
        assertThat(resolveResult.getJSONObject("status").getLong("id")).isEqualTo(PROCEDURE_APPROVED.getId());
        assertThat(resolveResult.getString("expiration")).isNotEmpty();
    }

    @Test
    public void testCreateAndApproveNullExpirationDisabilityProcedure() throws Exception {
        JSONObject disabilityProcedureObj = new JSONObject();
        disabilityProcedureObj.put("description", "disability procedure");
        disabilityProcedureObj.put("beneficiary", beneficiaryObj);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("file", Collections.singletonList(""));
        params.put("body", Collections.singletonList(disabilityProcedureObj.toString()));

        JSONObject result = TestUtils.createFormAndGetObject(this.mvc,
                ENDPOINT_DISABILITY_PROCEDURES, beneficiaryJwtAccessToken, params);

        assertProcedure(result, DisabilityProcedure.class);

        JSONObject disabilityProcedureResolution = new JSONObject();
        disabilityProcedureResolution.put("resolution", ProcedureResolution.APPROVE.toString());

        String procedureEndpoint = StringUtils.join(ENDPOINT_DISABILITY_PROCEDURES, SLASH, result.getLong("id"));

        JSONObject resolveResult = TestUtils.putObject(this.mvc,
                procedureEndpoint, disabilityProcedureResolution.toString(), adminJwtAccessToken);

        assertThat(resolveResult.getString("closedAt")).isNotEmpty();
        assertThat(resolveResult.getJSONObject("status").getLong("id")).isEqualTo(PROCEDURE_APPROVED.getId());
        assertThat(result.get("expiration")).isEqualTo(null);
    }

    @Test
    public void testCreateAndRejectDisabilityProcedure() throws Exception {
        JSONObject disabilityProcedureObj = new JSONObject();
        disabilityProcedureObj.put("description", "disability procedure");
        disabilityProcedureObj.put("beneficiary", beneficiaryObj);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("file", Collections.singletonList(""));
        params.put("body", Collections.singletonList(disabilityProcedureObj.toString()));

        JSONObject result = TestUtils.createFormAndGetObject(this.mvc,
                ENDPOINT_DISABILITY_PROCEDURES, beneficiaryJwtAccessToken, params);

        assertProcedure(result, DisabilityProcedure.class);

        JSONObject disabilityProcedureResolution = new JSONObject();
        disabilityProcedureResolution.put("resolution", ProcedureResolution.REJECT.toString());
        disabilityProcedureResolution.put("reason", "invalid documents");

        String procedureEndpoint = StringUtils.join(ENDPOINT_DISABILITY_PROCEDURES, SLASH, result.getLong("id"));

        JSONObject resolveResult = TestUtils.putObject(this.mvc,
                procedureEndpoint, disabilityProcedureResolution.toString(), adminJwtAccessToken);

        assertThat(resolveResult.getString("closedAt")).isNotEmpty();
        assertThat(resolveResult.getJSONObject("status").getLong("id")).isEqualTo(PROCEDURE_REJECTED.getId());
        assertThat(result.get("expiration")).isEqualTo(null);
        assertThat(resolveResult.getJSONArray("messages")).hasSize(1);
    }

    private void initBeneficiaries() throws Exception {
        JSONObject beneficiaryJsonObject = utils.readFileToJsonObject(IntegrationTestConstants.SCHEMA_BENEFICIARY);
        JSONArray insurancePlans = new JSONArray();
        JSONObject beneficiaryInsurancePlan = new JSONObject();
        beneficiaryInsurancePlan.put("insurancePlan", insurancePlanObj);
        insurancePlans.put(beneficiaryInsurancePlan);
        beneficiaryJsonObject.put("beneficiaryInsurancePlans", insurancePlans);
        beneficiaryObj = TestUtils.createAndGetObject(
                this.mvc,
                ENDPOINT_BENEFICIARIES,
                beneficiaryJsonObject.toString(),
                adminJwtAccessToken);
    }

    private void assertProcedure(JSONObject result, Class type) {
        assertThat(result.getString("createdAt")).isNotEmpty();
        assertThat(result.get("closedAt")).isEqualTo(null);
        assertThat(result.get("expiration")).isEqualTo(null);
        assertThat(result.getJSONObject("beneficiary")).isNotNull();
        assertThat(result.getString("description")).isNotEmpty();
        assertThat(result.getLong("fileCount")).isEqualTo(0);
        assertThat(result.getJSONArray("messages")).isEmpty();
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(PROCEDURE_REVISION.getId());
        assertThat(result.getString("type")).isEqualTo(type.getSimpleName());
        assertThat(result.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_FILES)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_AUDIT_LOGS)).isNotNull();
    }

}
