package com.capacidad.validationapi.module.practitioner.integration;

import com.capacidad.validationapi.AuthUtils;
import com.capacidad.validationapi.IntegrationTest;
import com.capacidad.validationapi.IntegrationTestConstants;
import com.capacidad.validationapi.TestUtils;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.general.reference.StatusReference;
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

import java.time.LocalDate;

import static com.capacidad.validationapi.IntegrationTestConstants.*;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

public class PractitionerIntegrationTest extends IntegrationTest {

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

    private JSONObject practitionerCategory, organization, medicalCenter, contract;

    @Before
    public void init() throws Exception {
        TestUtils.truncateDatabaseTables(applicationContext,
                "medical_center",
                "contract",
                "organization",
                "practitioner",
                "practitioner_category",
                "practitioner_contracts",
                "practitioner_medical_registrations",
                "practitioner_medical_specialties",
                "practitioner_medical_centers",
                "phone",
                "address");

        TenantContext.clearTenant();
        SecurityContextHolder.clearContext();

        jwtAccessToken = authUtils.obtainAccessToken(
                ADMIN.toLowerCase(),
                "all:practitioners,all:categories,all:organizations,all:medical_centers,all:contracts",
                null);

        long practitionerCategoryId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_PRACTITIONER_CATEGORIES,
                utils.readFileToJsonObject(SCHEMA_PRACTITIONER_CATEGORY_1).toString(),
                jwtAccessToken);

        long organizationId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_ORGANIZATIONS,
                utils.readFileToJsonObject(IntegrationTestConstants.SCHEMA_ORGANIZATION_3).toString(),
                jwtAccessToken);

        long medicalCenterId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_MEDICAL_CENTERS,
                utils.readFileToJsonObject(IntegrationTestConstants.SCHEMA_MEDICAL_CENTER_1).toString(),
                jwtAccessToken);

        practitionerCategory = new JSONObject();
        practitionerCategory.put("id", practitionerCategoryId);

        organization = new JSONObject();
        organization.put("id", organizationId);

        medicalCenter = new JSONObject();
        medicalCenter.put("id", medicalCenterId);

        JSONObject contractObj = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contractObj.put("dateFrom", LocalDate.now().plusMonths(3).toString());
        contractObj.put("dateTo", LocalDate.now().plusMonths(6).toString());
        contractObj.put("organization", organization);

        long contractId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_ORGANIZATION_CONTRACT,
                contractObj.toString(),
                jwtAccessToken);

        contract = new JSONObject();
        contract.put("id", contractId);

    }

    @Test
    public void testCreatePractitionerIsSuccessful() throws Exception {
        JSONObject practitioner = buildPractitionerObject();

        JSONObject result = TestUtils.createAndGetObject(this.mvc,
                ENDPOINT_PRACTITIONERS, practitioner.toString(),
                jwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_MEDICAL_SPECIALTIES)).hasSize(1);
    }

    @Test
    public void testAssociateAndDisassociateContractToPractitionerIsSuccessful() throws Exception {
        JSONObject practitioner = buildPractitionerObject();

        long practitionerId = TestUtils.createAndReturnLocationId(this.mvc,
                ENDPOINT_PRACTITIONERS, practitioner.toString(),
                jwtAccessToken);

        JSONObject contractObj = utils.readFileToJsonObject(SCHEMA_CONTRACT_2);
        contractObj.put("dateFrom", LocalDate.now().plusMonths(3).toString());
        contractObj.put("dateTo", LocalDate.now().plusMonths(6).toString());

        long contractId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_CONTRACTS,
                contractObj.toString(),
                jwtAccessToken);

        String putContractEndpoint = StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId, ENDPOINT_CONTRACTS, SLASH, contractId);
        String getContractsEndpoint = StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId, ENDPOINT_CONTRACTS);

        TestUtils.putObject(this.mvc, putContractEndpoint, jwtAccessToken);
        JSONObject contractsBeforeDisassociation = TestUtils.getAndReturnObject(this.mvc, getContractsEndpoint, jwtAccessToken);

        assertThat(contractsBeforeDisassociation.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_CONTRACTS)).hasSize(2);

        TestUtils.deleteObject(this.mvc, putContractEndpoint, jwtAccessToken);
        JSONObject medicalCentersAfterDisassociation = TestUtils.getAndReturnObject(this.mvc, getContractsEndpoint, jwtAccessToken);

        assertThat(medicalCentersAfterDisassociation.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_CONTRACTS)).hasSize(1);
    }

    @Test
    public void testAssociateAndDisassociatePractitionerToMedicalCenterIsSuccessful() throws Exception {
        JSONObject practitioner = buildPractitionerObject();

        long practitionerId = TestUtils.createAndReturnLocationId(this.mvc,
                ENDPOINT_PRACTITIONERS, practitioner.toString(),
                jwtAccessToken);

        long medicalCenterId = TestUtils.createAndReturnLocationId(this.mvc,
                ENDPOINT_MEDICAL_CENTERS, utils.readFileToJsonObject(SCHEMA_MEDICAL_CENTER_2).toString(),
                jwtAccessToken);

        String putMedicalCenterEndpoint = StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId, ENDPOINT_MEDICAL_CENTERS, SLASH, medicalCenterId);
        String getMedicalCentersEndpoint = StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId, ENDPOINT_MEDICAL_CENTERS);

        TestUtils.putObject(this.mvc, putMedicalCenterEndpoint, jwtAccessToken);
        JSONObject medicalCentersBeforeDisassociation = TestUtils.getAndReturnObject(this.mvc, getMedicalCentersEndpoint, jwtAccessToken);

        assertThat(medicalCentersBeforeDisassociation.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_MEDICAL_CENTERS)).hasSize(2);

        TestUtils.deleteObject(this.mvc, putMedicalCenterEndpoint, jwtAccessToken);
        JSONObject medicalCentersAfterDisassociation = TestUtils.getAndReturnObject(this.mvc, getMedicalCentersEndpoint, jwtAccessToken);

        assertThat(medicalCentersAfterDisassociation.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_MEDICAL_CENTERS)).hasSize(1);
    }

    @Test
    public void testAssociateAndDisassociateMedicalSpecialtyToPractitionerIsSuccessful() throws Exception {
        JSONObject practitioner = buildPractitionerObject();

        long practitionerId = TestUtils.createAndReturnLocationId(this.mvc,
                ENDPOINT_PRACTITIONERS, practitioner.toString(),
                jwtAccessToken);

        long medicalSpecialtyId = 3;

        String putMedicalSpecialtyEndpoint = StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId, SLASH, "medical-specialties", SLASH, medicalSpecialtyId);
        String getMedicalSpecialtiesEndpoint = StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId, SLASH, "medical-specialties");

        TestUtils.putObject(this.mvc, putMedicalSpecialtyEndpoint, jwtAccessToken);
        JSONArray medicalSpecialtiesBeforeDisassociation = TestUtils.getAndReturnArray(this.mvc, getMedicalSpecialtiesEndpoint, jwtAccessToken);

        assertThat(medicalSpecialtiesBeforeDisassociation).hasSize(2);

        TestUtils.deleteObject(this.mvc, putMedicalSpecialtyEndpoint, jwtAccessToken);
        JSONArray medicalSpecialtiesAfterDisassociation = TestUtils.getAndReturnArray(this.mvc, getMedicalSpecialtiesEndpoint, jwtAccessToken);

        assertThat(medicalSpecialtiesAfterDisassociation).hasSize(1);
    }

    @Test
    public void testAddMedicalRegistrationToPractitionerIsSuccessful() throws Exception {
        JSONObject practitioner = buildPractitionerObject();

        long practitionerId = TestUtils.createAndReturnLocationId(this.mvc,
                ENDPOINT_PRACTITIONERS, practitioner.toString(),
                jwtAccessToken);

        long organizationId = TestUtils.createAndReturnLocationId(this.mvc,
                ENDPOINT_ORGANIZATIONS, utils.readFileToJsonObject(SCHEMA_ORGANIZATION_2).toString(),
                jwtAccessToken);

        String medicalRegistrationEndpoint = StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId, ENDPOINT_MEDICAL_REGISTRATIONS);

        JSONObject medicalRegistration = new JSONObject();
        JSONObject organization = new JSONObject();
        organization.put("id", organizationId);
        medicalRegistration.put("organization", organization);
        medicalRegistration.put("registrationCode", "abcabc123123");

        TestUtils.createObject(this.mvc, medicalRegistrationEndpoint, medicalRegistration.toString(), jwtAccessToken);
        JSONObject medicalRegistrations = TestUtils.getAndReturnObject(this.mvc, medicalRegistrationEndpoint, jwtAccessToken);

        assertThat(medicalRegistrations.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_MEDICAL_REGISTRATIONS)).hasSize(2);
    }

    @Test
    public void testUpdatePractitionerStatusIsSuccessful() throws Exception {
        JSONObject practitioner = buildPractitionerObject();

        long practitionerId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_PRACTITIONERS,
                practitioner.toString(), jwtAccessToken);

        JSONObject practitionerPutObject = new JSONObject();
        JSONObject disabledStatusObject = new JSONObject();
        disabledStatusObject.put("id", StatusReference.DISABLED.getId());
        practitionerPutObject.put("status", disabledStatusObject);
        practitionerPutObject.put("statusUpdateDescription", "update reason");

        JSONObject updatedPractitioner = TestUtils.putObject(this.mvc, StringUtils.join(ENDPOINT_PRACTITIONERS, SLASH, practitionerId,
                SLASH, "status"),
                practitionerPutObject.toString(),
                jwtAccessToken);

        assertThat(updatedPractitioner.getJSONObject("status").getLong("id")).isEqualTo(StatusReference.DISABLED.getId());
    }

    private void assertResult(JSONObject result) {
        assertThat(result).isNotNull();
        assertThat(result.getString("createdAt")).isNotEmpty();
        assertThat(result.getLong("id")).isNotNull();
        assertThat(result.getLong("idNumber")).isNotNull();
        assertThat(result.getString("name")).isNotBlank();
        assertThat(result.getString("lastName")).isNotBlank();
        assertThat(result.getString("birthDate")).isNotBlank();
        assertThat(result.getString("gender")).isNotBlank();
        assertThat(result.getString("practitionerCode")).isNotBlank();
        assertThat(result.getJSONObject("idType")).isNotNull();
        assertThat(result.getJSONObject("practitionerCategory")).isNotNull();
        assertThat(result.getLong("workIdNumber")).isNotNull();
        assertThat(result.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_MEDICAL_SPECIALTIES)).isNotEmpty();
        assertThat(result.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_CONTRACTS)).isNotEmpty();
        assertThat(result.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_CONTACT_INFO)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_MEDICAL_CENTERS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_MEDICAL_REGISTRATIONS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_SETTLEMENTS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_BUDGETS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_AUTHORIZATIONS)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_AUTHORIZATION_ITEMS)).isNotNull();
    }

    private JSONObject buildPractitionerObject() {
        JSONObject practitioner = utils.readFileToJsonObject(SCHEMA_PRACTITIONER);
        practitioner.put("practitionerCategory", practitionerCategory);
        JSONArray contracts = new JSONArray();
        contracts.put(contract);
        practitioner.put("contracts", contracts);
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

}
