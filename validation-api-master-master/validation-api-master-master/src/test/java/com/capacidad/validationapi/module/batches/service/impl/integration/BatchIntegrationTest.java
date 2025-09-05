package com.capacidad.validationapi.module.batches.service.impl.integration;

import com.capacidad.validationapi.AuthUtils;
import com.capacidad.validationapi.IntegrationTest;
import com.capacidad.validationapi.IntegrationTestConstants;
import com.capacidad.validationapi.TestUtils;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.general.model.Period;
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

import java.time.LocalDate;

import static com.capacidad.validationapi.IntegrationTestConstants.*;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ADMIN;
import static com.capacidad.validationapi.module.general.reference.StatusReference.BATCH_ACTIVE;
import static com.capacidad.validationapi.module.general.reference.StatusReference.BATCH_PENDING;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchIntegrationTest extends IntegrationTest {

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

    private String adminJwtAccessToken;

    private JSONObject beneficiaryObj,
            insurancePlanObj,
            nomenclator1Obj,
            nomenclator2Obj,
            practitionerObj,
            medicalCenterObj,
            organizationObj;

    @Before
    public void init() throws Exception {
        TestUtils.truncateDatabaseTables(applicationContext,
                "nomenclator",
                "insurance_plan",
                "medical_specialties_practices",
                "medical_practice",
                "batch_diagnosis",
                "batch",
                "batch_item",
                "beneficiary",
                "beneficiary_category",
                "beneficiary_insurance_plan",
                "address",
                "phone");

        adminJwtAccessToken = authUtils.obtainAccessToken(ADMIN.toLowerCase(),
                "all:beneficiaries,all:insurance_plans,all:batches," +
                        "all:medical_centers,all:organizations,all:practitioners,all:nomenclators",
                null);

        long insurancePlanId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_INSURANCE_PLANS,
                utils.readFileToJsonObject(SCHEMA_INSURANCE_PLAN_1).toString(),
                adminJwtAccessToken);

        insurancePlanObj = new JSONObject();
        insurancePlanObj.put("id", insurancePlanId);

        initBeneficiaries();

        initNomenclators();

    }

    @Test
    public void testCreateBatchIsSuccessful() throws Exception {
        initMedicalCenters();
        initOrganizations();
        initPractitioners();

        JSONObject batch = new JSONObject();
        batch.put("dateFrom", LocalDate.now());
        batch.put("dateTo", LocalDate.now().plusDays(90));
        batch.put("beneficiary", beneficiaryObj);

        JSONObject batchItem1 = new JSONObject();
        batchItem1.put("nomenclator", nomenclator1Obj);
        batchItem1.put("amount", 2);
        batchItem1.put("period", Period.MONTHLY.toString());
        JSONArray medCenArray = new JSONArray();
        medCenArray.put(medicalCenterObj);
        batchItem1.put("medicalCenters", medCenArray);
        JSONArray practitionerArray = new JSONArray();
        practitionerArray.put(practitionerObj);
        batchItem1.put("practitioners", practitionerArray);

        JSONObject batchItem2 = new JSONObject();
        batchItem2.put("nomenclator", nomenclator2Obj);
        batchItem2.put("amount", 1);
        batchItem2.put("period", Period.WEEKLY.toString());

        JSONArray batchItems = new JSONArray();
        batchItems.put(batchItem1);
        batchItems.put(batchItem2);

        batch.put("batchItems", batchItems);

        JSONObject result = TestUtils.createAndGetObject(this.mvc,
                ENDPOINT_BATCHES, batch.toString(),
                adminJwtAccessToken);

        String batchItemsEndpoint = StringUtils.join(ENDPOINT_BATCHES, SLASH, result.getLong("id"), SLASH, "batch-items");

        JSONObject items = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(batchItemsEndpoint, "?page=1&size=10"),
                adminJwtAccessToken);

        assertThat(result.getString("dateFrom")).isNotEmpty();
        assertThat(result.getString("dateTo")).isNotEmpty();
        assertThat(result.get("statusUpdateDescription")).isEqualTo(null);
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(BATCH_ACTIVE.getId());
        assertThat(result.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_FILES)).isNotNull();
        assertThat(items.getJSONObject(EMBEDDED).getJSONArray("batchItems")).hasSize(2);
        items.getJSONObject(EMBEDDED).getJSONArray("batchItems").forEach(i -> {
            JSONObject item = (JSONObject) i;
            JSONArray practitionerArr = item.getJSONObject(EMBEDDED).getJSONArray("practitioners");
            JSONArray medCenArr = item.getJSONObject(EMBEDDED).getJSONArray("medicalCenters");
            assertThat(item.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
            if (item.getJSONObject("nomenclator").getLong("id") == nomenclator1Obj.getLong("id")) {
                assertThat(item.getLong("amount")).isEqualTo(batchItem1.getLong("amount"));
                assertThat(item.getString("period")).isEqualTo(batchItem1.getString("period"));
                JSONObject practitioner = (JSONObject) practitionerArr.get(0);
                JSONObject medCen = (JSONObject) medCenArr.get(0);
                assertThat(practitioner.getLong("id")).isEqualTo(practitionerObj.getLong("id"));
                assertThat(medCen.getLong("id")).isEqualTo(medicalCenterObj.getLong("id"));
            }
            if (item.getJSONObject("nomenclator").getLong("id") == nomenclator2Obj.getLong("id")) {
                assertThat(item.getLong("amount")).isEqualTo(batchItem2.getLong("amount"));
                assertThat(item.getString("period")).isEqualTo(batchItem2.getString("period"));
                assertThat(practitionerArr).isEmpty();
                assertThat(medCenArr).isEmpty();
            }
        });
    }

    @Test
    public void testCreateBatchThenAddBatchItemIsSuccessful() throws Exception {
        initMedicalCenters();
        initOrganizations();
        initPractitioners();

        JSONObject batch = new JSONObject();
        batch.put("dateFrom", LocalDate.now());
        batch.put("dateTo", LocalDate.now().plusDays(90));
        batch.put("beneficiary", beneficiaryObj);

        JSONObject diagnosis = new JSONObject();
        diagnosis.put("id", 5);
        JSONArray diagnosisList = new JSONArray();
        diagnosisList.put(diagnosis);
        batch.put("diagnosis", diagnosisList);

        JSONObject batchItem1 = new JSONObject();
        batchItem1.put("nomenclator", nomenclator1Obj);
        batchItem1.put("amount", 2);
        batchItem1.put("period", Period.MONTHLY.toString());
        batchItem1.put("medicalCenter", medicalCenterObj);
        batchItem1.put("practitioner", practitionerObj);

        JSONObject batchItem2 = new JSONObject();
        batchItem2.put("nomenclator", nomenclator2Obj);
        batchItem2.put("amount", 1);
        batchItem2.put("period", Period.WEEKLY.toString());

        JSONArray batchItems = new JSONArray();
        batchItems.put(batchItem1);
        batch.put("batchItems", batchItems);

        JSONObject result = TestUtils.createAndGetObject(this.mvc,
                ENDPOINT_BATCHES, batch.toString(),
                adminJwtAccessToken);

        assertThat(result.getJSONArray("diagnosis").getJSONObject(0).getLong("id"))
                .isEqualTo(diagnosis.getLong("id"));

        String batchEndpoint = StringUtils.join(ENDPOINT_BATCHES, SLASH, result.getLong("id"), SLASH, "batch-items");

        JSONObject batchItemResult = TestUtils.postAndGetObject(this.mvc,
                batchEndpoint, batchItem2.toString(), adminJwtAccessToken);

        assertThat(batchItemResult.getJSONObject("nomenclator").getLong("id"))
                .isEqualTo(nomenclator2Obj.getLong("id"));
    }

    @Test
    public void testPatchBatchIsSuccessful() throws Exception {
        JSONObject batch = new JSONObject();
        batch.put("dateFrom", LocalDate.now());
        batch.put("dateTo", LocalDate.now().plusDays(90));
        batch.put("beneficiary", beneficiaryObj);

        JSONObject diagnosis = new JSONObject();
        diagnosis.put("id", 5);
        JSONArray diagnosisList = new JSONArray();
        diagnosisList.put(diagnosis);
        batch.put("diagnosis", diagnosisList);

        JSONObject batchItem2 = new JSONObject();
        batchItem2.put("nomenclator", nomenclator2Obj);
        batchItem2.put("amount", 1);
        batchItem2.put("period", Period.WEEKLY.toString());

        JSONArray batchItems = new JSONArray();
        batchItems.put(batchItem2);
        batch.put("batchItems", batchItems);

        JSONObject result = TestUtils.createAndGetObject(this.mvc,
                ENDPOINT_BATCHES, batch.toString(),
                adminJwtAccessToken);

        String batchEndpoint = StringUtils.join(ENDPOINT_BATCHES, SLASH, result.getLong("id"));

        JSONObject patchBatch = new JSONObject();
        patchBatch.put("dateFrom", LocalDate.now().plusDays(10));
        patchBatch.put("dateTo", LocalDate.now().plusDays(15));

        JSONObject diagnosisPatch = new JSONObject();
        diagnosisPatch.put("id", 6);
        JSONArray diagnosisPatchList = new JSONArray();
        diagnosisPatchList.put(diagnosisPatch);
        patchBatch.put("diagnosis", diagnosisPatchList);
        patchBatch.put("diagnosis", diagnosisPatchList);

        JSONObject patchResult = TestUtils.patchObject(this.mvc,
                batchEndpoint, patchBatch.toString(), adminJwtAccessToken);

        assertThat(patchResult.getString("dateFrom")).isEqualTo(patchBatch.get("dateFrom").toString());
        assertThat(patchResult.getString("dateTo")).isEqualTo(patchBatch.get("dateTo").toString());
        assertThat(patchResult.getJSONArray("diagnosis").getJSONObject(0).getLong("id")).isEqualTo(diagnosisPatch.getLong("id"));
        assertThat(patchResult.getJSONObject("status").getLong("id")).isEqualTo(BATCH_PENDING.getId());
    }

    private void initNomenclators() throws Exception {
        String medicalPracticeEndpoint = StringUtils.join(ENDPOINT_NOMENCLATORS, SLASH, "medical-practices");
        JSONObject medicalPractice1 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_1).toString(), adminJwtAccessToken);
        JSONObject nom1 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_1);
        nom1.put("medicalPractice", medicalPractice1);
        long nomenclator1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom1.toString(), adminJwtAccessToken);
        nomenclator1Obj = new JSONObject();
        nomenclator1Obj.put("id", nomenclator1);

        JSONObject medicalPractice2 = TestUtils.createObjectAndGetResult(this.mvc, medicalPracticeEndpoint,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_PRACTICE_2).toString(), adminJwtAccessToken);
        JSONObject nom2 = utils.readFileToJsonObject(SCHEMA_NOMENCLATOR_2);
        nom2.put("medicalPractice", medicalPractice2);
        long nomenclator2 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_NOMENCLATORS,
                nom2.toString(), adminJwtAccessToken);
        nomenclator2Obj = new JSONObject();
        nomenclator2Obj.put("id", nomenclator2);
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

    private void initMedicalCenters() throws Exception {
        medicalCenterObj = TestUtils.createAndGetObject(this.mvc, ENDPOINT_MEDICAL_CENTERS,
                utils.readFileToJsonObject(SCHEMA_MEDICAL_CENTER_1).toString(),
                adminJwtAccessToken);
    }

    private void initOrganizations() throws Exception {
        long organizationId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_ORGANIZATIONS,
                utils.readFileToJsonObject(SCHEMA_ORGANIZATION_1).toString(),
                adminJwtAccessToken);
        organizationObj = new JSONObject();
        organizationObj.put("id", organizationId);
    }

    private void initPractitioners() throws Exception {
        JSONObject practitioner = utils.readFileToJsonObject(SCHEMA_PRACTITIONER);
        JSONArray medicalCenters = new JSONArray();
        medicalCenters.put(medicalCenterObj);
        practitioner.put("medicalCenters", medicalCenters);
        JSONArray medicalRegistrations = new JSONArray();
        JSONObject medicalRegistration = new JSONObject();
        medicalRegistration.put("organization", organizationObj);
        medicalRegistration.put("registrationCode", "123456");
        medicalRegistrations.put(medicalRegistration);
        practitioner.put("medicalRegistrations", medicalRegistrations);
        practitionerObj = TestUtils.createAndGetObject(this.mvc,
                ENDPOINT_PRACTITIONERS, practitioner.toString(),
                adminJwtAccessToken);
    }

}
