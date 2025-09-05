package com.capacidad.validationapi.module.medicalauthorization.integration;

import com.capacidad.validationapi.AuthUtils;
import com.capacidad.validationapi.IntegrationTest;
import com.capacidad.validationapi.IntegrationTestConstants;
import com.capacidad.validationapi.TestUtils;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.medicalcoverage.reference.ChargeTypeReference;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.settlement.model.SettlementOperation;
import com.capacidad.validationapi.module.totp.Authenticator;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.hashids.Hashids;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import static com.capacidad.validationapi.IntegrationTestConstants.*;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;
import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference.*;
import static com.capacidad.validationapi.module.medicalauthorization.service.impl.QRMedicalAuthorizationServiceImpl.QR_TYPE;
import static com.capacidad.validationapi.module.medicalauthorization.service.impl.QRMedicalAuthorizationServiceImplTest.buildBeneficiaryQrJson;
import static com.capacidad.validationapi.module.premedicalauthorization.service.impl.PreMedicalAuthorizationServiceImpl.PRE_MEDICAL_AUTHORIZATION_CODE_KEY;
import static com.capacidad.validationapi.module.premedicalauthorization.service.impl.PreMedicalAuthorizationServiceImpl.PRE_MEDICAL_AUTHORIZATION_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MedicalAuthorizationIntegrationTest extends IntegrationTest {

    private static final String ENCRYPTION_KEY = "PdSgVkYpPdSgVkYp";
    private static final String ENCRYPTION_IV = "1234567891111111";
    @MockBean
    ConnectionFactory connectionFactory;

    @MockBean
    AmqpAdmin amqpAdmin;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MockMvc mvc;

    @Autowired
    private Utils utils;

    @Autowired
    private Hashids hashids;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private AuthUtils authUtils;

    private String adminJwtAccessToken, medicalCenterJwtAccessToken, practitionerJwtAccessToken;

    private JSONObject medicalCoverageItem1Obj,
            medicalCoverageItem2Obj,
            contractItem1Obj,
            contractItem2Obj,
            contractObj,
            nomenclator1Obj,
            nomenclator2Obj,
            region1Obj,
            medicalCenterObj,
            organizationObj,
            insurancePlanObj,
            specialInsurancePlanObj,
            beneficiaryObj,
            practitionerObj;

    private static String encryptData(String dataToEncrypt, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] secretKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
            byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] contentBytes = cipher.doFinal(dataToEncrypt.getBytes());
            return new String(Base64.getEncoder().encode(contentBytes));
        } catch (Exception e) {
            return "";
        }
    }

    @Before
    public void init() throws Exception {
        TestUtils.truncateDatabaseTables(applicationContext,
                "insurance_plan",
                "batch_diagnosis",
                "batch",
                "batch_item",
                "beneficiary",
                "beneficiary_insurance_plan",
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
                "practitioner_contracts",
                "practitioner_medical_registrations",
                "practitioner_medical_specialties",
                "practitioner_medical_centers",
                "phone",
                "address",
                "medical_coverage",
                "medical_coverage_item",
                "budget",
                "budget_item",
                "settlement",
                "settlement_item",
                "rule_configuration",
                "medical_authorization",
                "medical_authorization_item",
                "medical_authorization_failure",
                "medical_authorization_item_failure",
                "pre_medical_authorization",
                "pre_medical_authorization");

        TenantContext.clearTenant();
        SecurityContextHolder.clearContext();

        adminJwtAccessToken = authUtils.obtainAccessToken(ADMIN.toLowerCase(),
                "all:beneficiaries,all:nomenclators,all:regions,all:contracts,all:organizations," +
                        "all:medical_centers,all:practitioners,all:medical_registrations,all:categories,all:insurance_plans," +
                        "all:medical_coverages,all:medical_authorizations,all:rules,all:settlements,all:companies,all:beneficiary_budgets," +
                        "all:batches,all:pre_authorizations,read:medical_authorization_items,all:audit_trays",
                null);

        initMedicalCenters();

        medicalCenterJwtAccessToken = authUtils.obtainAccessToken(MEDICAL_CENTER.toLowerCase(),
                "read:beneficiaries,read:nomenclators,read:regions,read:contracts,read:organizations," +
                        "read:medical_centers,read:practitioners,read:medical_registrations,read:categories,read:insurance_plans," +
                        "read:medical_coverages,create:medical_authorizations,read:medical_authorizations," +
                        "update:medical_authorizations,read:practitioner_budgets,update:practitioner_budgets,read:medical_authorization_items",
                UUID.fromString(medicalCenterObj.getString("resourceId")));

        initNomenclators();
        initRegions();
        initOrganizations();
        initInsurancePlans();
        initPractitioners();

        practitionerJwtAccessToken = authUtils.obtainAccessToken(PRACTITIONER.toLowerCase(),
                "read:beneficiaries,read:nomenclators,read:regions,read:contracts,read:organizations," +
                        "read:medical_centers,read:practitioners,read:medical_registrations,read:categories,read:insurance_plans," +
                        "read:medical_coverages,create:medical_authorizations,read:medical_authorizations," +
                        "update:medical_authorizations,read:practitioner_budgets,update:practitioner_budgets,read:medical_authorization_items",
                UUID.fromString(practitionerObj.getString("resourceId")));
    }

    @Test
    public void testQRAuthorizationWithOrganizationContractIsApprovedAndSettlementAndPractitionerBudgetsAreSuccessful() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject medicalAuthorization = new JSONObject();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(beneficiaryObj.getLong("idNumber"));
        IdType idType = new IdType();
        idType.setId(beneficiaryObj.getJSONObject("idType").getLong("id"));
        beneficiary.setIdType(idType);

        String qrJson = buildBeneficiaryQrJson(beneficiary, false).toString();
        String encryptedBeneficiary = encryptData(qrJson, ENCRYPTION_KEY, ENCRYPTION_IV);

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("encryptedQr", encryptedBeneficiary);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/qr-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(result.get("preMedicalAuthorization")).isEqualTo(null);
        assertItems(result);

        JSONObject practitionerCurrentItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATION_ITEMS, "?page=1&size=50"),
                practitionerJwtAccessToken);
        JSONArray practitionerItems = practitionerCurrentItems.getJSONObject(EMBEDDED).getJSONArray("authorizationItems");

        JSONObject fullItem1 = findItem(practitionerItems, contractItem1Obj.getJSONObject("nomenclator"));
        JSONObject fullItem2 = findItem(practitionerItems, contractItem2Obj.getJSONObject("nomenclator"));

        assertApprovedItemValues(fullItem1, fullItem2);
        assertSettlement(fullItem1, fullItem2);
        assertPractitionerBudget(fullItem1, fullItem2);
    }

    @Test
    public void testQRAuthorizationAsPractitionerWithOrganizationContractIsApprovedAndSettlementAndPractitionerBudgetsAreSuccessful() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject medicalAuthorization = new JSONObject();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(beneficiaryObj.getLong("idNumber"));
        IdType idType = new IdType();
        idType.setId(beneficiaryObj.getJSONObject("idType").getLong("id"));
        beneficiary.setIdType(idType);

        String qrJson = buildBeneficiaryQrJson(beneficiary, false).toString();
        String encryptedBeneficiary = encryptData(qrJson, ENCRYPTION_KEY, ENCRYPTION_IV);

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("medicalCenter", medicalCenterObj);
        medicalAuthorization.put("encryptedQr", encryptedBeneficiary);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/qr-authorization"),
                medicalAuthorization.toString(),
                practitionerJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                practitionerJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(result.get("preMedicalAuthorization")).isEqualTo(null);
        assertItems(result);

        JSONObject practitionerCurrentItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATION_ITEMS, "?page=1&size=50"),
                practitionerJwtAccessToken);
        JSONArray practitionerItems = practitionerCurrentItems.getJSONObject(EMBEDDED).getJSONArray("authorizationItems");

        JSONObject fullItem1 = findItem(practitionerItems, contractItem1Obj.getJSONObject("nomenclator"));
        JSONObject fullItem2 = findItem(practitionerItems, contractItem2Obj.getJSONObject("nomenclator"));

        assertApprovedItemValues(fullItem1, fullItem2);
        assertSettlement(fullItem1, fullItem2);
    }

    @Test
    public void testIdAuthorizationWithPractitionerContractIsApprovedAndSettlementAndBeneficiaryBudgetsAreSuccessful() throws Exception {
        JSONObject companyObj = TestUtils.createAndGetObject(this.mvc, ENDPOINT_COMPANIES,
                utils.readFileToJsonObject(SCHEMA_COMPANY_1).toString(),
                adminJwtAccessToken);

        JSONObject beneficiaryJsonObject = utils.readFileToJsonObject(IntegrationTestConstants.SCHEMA_BENEFICIARY);
        JSONArray insurancePlans = new JSONArray();
        JSONObject beneficiaryInsurancePlan = new JSONObject();
        beneficiaryInsurancePlan.put("insurancePlan", insurancePlanObj);
        insurancePlans.put(beneficiaryInsurancePlan);
        beneficiaryJsonObject.put("beneficiaryInsurancePlans", insurancePlans);
        beneficiaryJsonObject.put("company", companyObj);
        JSONObject paycheck = new JSONObject();
        paycheck.put("id", PaymentMethodReference.PAYCHECK.getId());
        beneficiaryJsonObject.put("paymentMethod", paycheck);
        beneficiaryObj = TestUtils.createAndGetObject(
                this.mvc,
                ENDPOINT_BENEFICIARIES,
                beneficiaryJsonObject.toString(),
                adminJwtAccessToken);

        initPractitionerContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject medicalAuthorization = new JSONObject();

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("idType", beneficiaryObj.getJSONObject("idType"));
        beneficiaryCopy.put("idNumber", beneficiaryObj.getLong("idNumber"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        medicalAuthorization.put("medicalCenter", medicalCenterObj);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_MANUAL_ID_NUMBER.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
        assertItems(result);

        JSONObject practitionerCurrentItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATION_ITEMS, "?page=1&size=50"),
                practitionerJwtAccessToken);
        JSONArray practitionerItems = practitionerCurrentItems.getJSONObject(EMBEDDED).getJSONArray("authorizationItems");

        JSONObject fullItem1 = findItem(practitionerItems, contractItem1Obj.getJSONObject("nomenclator"));
        JSONObject fullItem2 = findItem(practitionerItems, contractItem2Obj.getJSONObject("nomenclator"));

        assertApprovedItemValues(fullItem1, fullItem2);
        assertSettlement(fullItem1, fullItem2);
        assertBeneficiaryBudget(fullItem1, fullItem2);
    }

    @Test
    public void testOTPAuthorizationWithMedicalCenterContractIsApprovedAndSettlementIsSuccessful() throws Exception {
        initBeneficiaries();
        initMedicalCenterContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject medicalAuthorization = new JSONObject();

        String beneficiaryHashKey = hashids.encode(beneficiaryObj.getLong("idNumber"), beneficiaryObj.getLong("idNumber"));
        String hexKey = Hex.encodeHexString(beneficiaryHashKey.getBytes()).toUpperCase();
        String base32Key = new Base32().encodeAsString(hexKey.getBytes()).replace(EQUAL, "");
        int beneficiaryOtp = authenticator.getTotpPassword(base32Key, Instant.now().toEpochMilli());

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("id", beneficiaryObj.getLong("id"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("otp", beneficiaryOtp);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/otp-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_MANUAL_CODE.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
        assertItems(result);

        JSONObject practitionerCurrentItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATION_ITEMS, "?page=1&size=50"),
                practitionerJwtAccessToken);
        JSONArray practitionerItems = practitionerCurrentItems.getJSONObject(EMBEDDED).getJSONArray("authorizationItems");

        JSONObject fullItem1 = findItem(practitionerItems, contractItem1Obj.getJSONObject("nomenclator"));
        JSONObject fullItem2 = findItem(practitionerItems, contractItem2Obj.getJSONObject("nomenclator"));

        assertApprovedItemValues(fullItem1, fullItem2);
        assertSettlement(fullItem1, fullItem2);
    }

    @Test
    public void testMagstripeAuthorizationWithOrganizationContractIsApproved() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject medicalAuthorization = new JSONObject();

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("beneficiaryCode", beneficiaryObj.getString("beneficiaryCode"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/magstripe-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_MANUAL_MAGSTRIPE.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
        assertItems(result);
    }

    @Test
    public void testQRAuthorizationIsPendingWhenCoverageIsLimited() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initLimitedMedicalCoverages();
        initAuditTray();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(beneficiaryObj.getLong("idNumber"));
        IdType idType = new IdType();
        idType.setId(beneficiaryObj.getJSONObject("idType").getLong("id"));
        beneficiary.setIdType(idType);

        String qrJson = buildBeneficiaryQrJson(beneficiary, false).toString();
        String encryptedBeneficiary = encryptData(qrJson, ENCRYPTION_KEY, ENCRYPTION_IV);

        JSONObject medicalAuthorization = new JSONObject();
        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("encryptedQr", encryptedBeneficiary);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/qr-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_PENDING.getId());

        BigDecimal approvedItemCharges = calculateCharges(item2, medicalCoverageItem2Obj, contractItem2Obj);
        assertThat(result.getBigDecimal("chargeTotal").setScale(0, RoundingMode.DOWN))
                .isEqualTo(approvedItemCharges);
    }

    @Test
    public void testIdAuthorizationIsPendingWhenAuditRulesNotPassedAndSettlementFails() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();
        initAuditRules();
        initAuditTray();

        JSONObject medicalAuthorization = new JSONObject();

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("idType", beneficiaryObj.getJSONObject("idType"));
        beneficiaryCopy.put("idNumber", beneficiaryObj.getLong("idNumber"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult1 = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        assertThat(minorObjectResult1.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());

        JSONObject minorObjectResult2 = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult2.getLong("id")),
                medicalCenterJwtAccessToken);


        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_MANUAL_ID_NUMBER.getId());
        assertThat(result.getBigDecimal("chargeTotal")).isEqualTo(new BigDecimal(0).setScale(0, RoundingMode.HALF_UP));
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_PENDING.getId());

        JSONArray medAuthItemIds = new JSONArray();
        result.getJSONObject(EMBEDDED).getJSONArray("authorizationItems").forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_PENDING.getId());
            medAuthItemIds.put(itemObj.getLong("id"));
        });

        JSONObject settlement = new JSONObject();
        settlement.put("practitioner", practitionerObj);
        settlement.put("medicalAuthorizationItemIds", medAuthItemIds);
    }

    @Test
    public void testIdAuthorizationIsRejectedWhenRejectionRulesNotPassedAndSettlementFails() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();
        initRejectionRules();

        JSONObject medicalAuthorization = new JSONObject();

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("idType", beneficiaryObj.getJSONObject("idType"));
        beneficiaryCopy.put("idNumber", beneficiaryObj.getLong("idNumber"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult1 = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        assertThat(minorObjectResult1.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());

        JSONObject minorObjectResult2 = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult2.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_MANUAL_ID_NUMBER.getId());
        assertThat(result.getBigDecimal("chargeTotal")).isEqualTo(new BigDecimal(0).setScale(0, RoundingMode.HALF_UP));
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_REJECTED.getId());

        JSONArray medAuthItemIds = new JSONArray();
        result.getJSONObject(EMBEDDED).getJSONArray("authorizationItems").forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_REJECTED.getId());
            medAuthItemIds.put(itemObj.getLong("id"));
        });

        JSONObject settlement = new JSONObject();
        settlement.put("practitioner", practitionerObj);
        settlement.put("medicalAuthorizationItemIds", medAuthItemIds);
    }

    @Test
    public void testApprovedIdAuthorizationIsCancelledSuccessfully() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject medicalAuthorization = new JSONObject();

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("idType", beneficiaryObj.getJSONObject("idType"));
        beneficiaryCopy.put("idNumber", beneficiaryObj.getLong("idNumber"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        assertThat(minorObjectResult.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());

        JSONObject cancellationObj = new JSONObject();
        cancellationObj.put("cancellationReason", "cancelled");

        JSONObject cancelledResult = TestUtils.putAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id"), "/status"),
                cancellationObj.toString(),
                medicalCenterJwtAccessToken);

        assertThat(cancelledResult.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_CANCELLED.getId());
        cancelledResult.getJSONObject(EMBEDDED).getJSONArray("authorizationItems").forEach(item -> {
            JSONObject itemObj = (JSONObject) item;
            assertThat(itemObj.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_CANCELLED.getId());
        });
        assertThat(cancelledResult.getBigDecimal("chargeTotal")).isEqualTo(new BigDecimal(0));
    }

    @Test
    public void testIdAuthorizationWithOrganizationContractAppliesBeneficiaryBatchSuccessfully() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initBatches();

        JSONObject medicalAuthorization = new JSONObject();

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("idType", beneficiaryObj.getJSONObject("idType"));
        beneficiaryCopy.put("idNumber", beneficiaryObj.getLong("idNumber"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        medicalAuthorization.put("specialAuthorization", true);

        JSONObject result = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        assertThat(result.getBigDecimal("chargeTotal").setScale(1, RoundingMode.HALF_UP))
                .isEqualTo(new BigDecimal(0).setScale(1, RoundingMode.HALF_UP));
        assertThat(result.getJSONObject("batch").getLong("id")).isNotNull();
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
    }

    @Test
    public void testIdAuthorizationWithOrganizationContractAppliesBeneficiaryBatchUnsuccessfullyWithFailingAmounts() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initBatches();

        JSONObject medicalAuthorization = new JSONObject();

        JSONObject beneficiaryCopy = new JSONObject();
        beneficiaryCopy.put("idType", beneficiaryObj.getJSONObject("idType"));
        beneficiaryCopy.put("idNumber", beneficiaryObj.getLong("idNumber"));

        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiaryCopy);
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        items.put(item1);
        items.put(item2);

        medicalAuthorization.put("medicalAuthorizationItems", items);

        TestUtils.createObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                status().isOk(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_PARTIALLY_APPROVED.getId());
        assertThat(result.getJSONObject("batch").getLong("id")).isPositive();
    }

    @Test
    public void testQRPreAuthorizationWithOrganizationContractIsApprovedAndSettlementAndPractitionerBudgetsAreSuccessful() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject beneficiary = new JSONObject();
        beneficiary.put("idNumber", beneficiaryObj.getLong("idNumber"));
        JSONObject idTypeJson = new JSONObject();
        idTypeJson.put("id", beneficiaryObj.getJSONObject("idType").getLong("id"));
        beneficiary.put("idType", idTypeJson);
        beneficiary.put("id", beneficiaryObj.getLong("id"));

        JSONObject petitioner = new JSONObject();
        petitioner.put("idNumber", practitionerObj.getLong("idNumber"));
        JSONObject petitionerIdTypeJson = new JSONObject();
        petitionerIdTypeJson.put("id", practitionerObj.getJSONObject("idType").getLong("id"));
        petitioner.put("idType", petitionerIdTypeJson);
        petitioner.put("id", practitionerObj.getLong("id"));

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        JSONArray items = new JSONArray();
        items.put(item1);
        items.put(item2);

        JSONObject container = new JSONObject();
        container.put("beneficiary", beneficiary);
        container.put("petitioner", petitioner);
        container.put("preMedicalAuthorizationItems", items);

        MockHttpServletResponse preMedAuthResponse = TestUtils
                .postAndGetResponse(mvc, ENDPOINT_PRE_AUTHORIZATIONS, container.toString(), adminJwtAccessToken);

        String code = preMedAuthResponse.getHeader("code");

        JSONObject preMedAuthQr = new JSONObject();
        JSONObject preMedAuthObj = new JSONObject();
        preMedAuthObj.put(PRE_MEDICAL_AUTHORIZATION_CODE_KEY, code);
        preMedAuthQr.put("preMedicalAuthorization", preMedAuthObj);
        preMedAuthQr.put(TIMESTAMP, LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        preMedAuthQr.put(ENCRYPTED_QR_KEY, UUID.randomUUID().toString());
        preMedAuthQr.put(QR_TYPE, PRE_MEDICAL_AUTHORIZATION_TYPE);
        String encryptedData = encryptData(preMedAuthQr.toString(), ENCRYPTION_KEY, ENCRYPTION_IV);

        JSONObject medicalAuthorization = new JSONObject();
        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("encryptedQr", encryptedData);
        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/qr-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(result.getJSONObject("preMedicalAuthorization").getString("code")).isEqualTo(code);
        assertItems(result);

        JSONObject practitionerCurrentItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATION_ITEMS, "?page=1&size=50"),
                practitionerJwtAccessToken);
        JSONArray practitionerItems = practitionerCurrentItems.getJSONObject(EMBEDDED).getJSONArray("authorizationItems");

        JSONObject fullItem1 = findItem(practitionerItems, contractItem1Obj.getJSONObject("nomenclator"));
        JSONObject fullItem2 = findItem(practitionerItems, contractItem2Obj.getJSONObject("nomenclator"));

        assertApprovedItemValues(fullItem1, fullItem2);
        assertSettlement(fullItem1, fullItem2);
        assertPractitionerBudget(fullItem1, fullItem2);
    }

    @Test
    public void testIdPreAuthorizationWithOrganizationContractIsApprovedAndSettlementAndPractitionerBudgetsAreSuccessful() throws Exception {
        initBeneficiaries();
        initOrganizationContract();
        associateContractToPractitioner();
        initMedicalCoverages();

        JSONObject beneficiary = new JSONObject();
        beneficiary.put("idNumber", beneficiaryObj.getLong("idNumber"));
        JSONObject idTypeJson = new JSONObject();
        idTypeJson.put("id", beneficiaryObj.getJSONObject("idType").getLong("id"));
        beneficiary.put("idType", idTypeJson);
        beneficiary.put("id", beneficiaryObj.getLong("id"));

        JSONObject petitioner = new JSONObject();
        petitioner.put("idNumber", practitionerObj.getLong("idNumber"));
        JSONObject petitionerIdTypeJson = new JSONObject();
        petitionerIdTypeJson.put("id", practitionerObj.getJSONObject("idType").getLong("id"));
        petitioner.put("idType", petitionerIdTypeJson);
        petitioner.put("id", practitionerObj.getLong("id"));

        JSONObject item1 = new JSONObject();
        item1.put("quantity", 1);
        item1.put("nomenclator", nomenclator1Obj);

        JSONObject item2 = new JSONObject();
        item2.put("quantity", 1);
        item2.put("nomenclator", nomenclator2Obj);

        JSONArray items = new JSONArray();
        items.put(item1);
        items.put(item2);

        JSONObject container = new JSONObject();
        container.put("beneficiary", beneficiary);
        container.put("petitioner", petitioner);
        container.put("preMedicalAuthorizationItems", items);

        MockHttpServletResponse preMedAuthResponse = TestUtils
                .postAndGetResponse(mvc, ENDPOINT_PRE_AUTHORIZATIONS, container.toString(), adminJwtAccessToken);

        String code = preMedAuthResponse.getHeader("code");

        JSONObject preMedAuthObj = new JSONObject();
        preMedAuthObj.put("code", code);

        JSONObject medicalAuthorization = new JSONObject();
        medicalAuthorization.put("practitioner", practitionerObj);
        medicalAuthorization.put("beneficiary", beneficiary);
        medicalAuthorization.put("preMedicalAuthorization", preMedAuthObj);
        medicalAuthorization.put("medicalAuthorizationItems", items);

        JSONObject minorObjectResult = TestUtils.postAndGetObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, "/id-authorization"),
                medicalAuthorization.toString(),
                medicalCenterJwtAccessToken);

        JSONObject result = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATIONS, SLASH, minorObjectResult.getLong("id")),
                medicalCenterJwtAccessToken);

        assertResult(result);
        assertThat(result.getJSONObject("authorizationType").getLong("id")).isEqualTo(AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION.getId());
        assertThat(result.getJSONObject("status").getLong("id")).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(result.getJSONObject("preMedicalAuthorization").getString("code")).isEqualTo(code);
        assertItems(result);

        JSONObject practitionerCurrentItems = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_AUTHORIZATION_ITEMS, "?page=1&size=50"),
                practitionerJwtAccessToken);
        JSONArray practitionerItems = practitionerCurrentItems.getJSONObject(EMBEDDED).getJSONArray("authorizationItems");

        JSONObject fullItem1 = findItem(practitionerItems, contractItem1Obj.getJSONObject("nomenclator"));
        JSONObject fullItem2 = findItem(practitionerItems, contractItem2Obj.getJSONObject("nomenclator"));

        assertApprovedItemValues(fullItem1, fullItem2);
        assertSettlement(fullItem1, fullItem2);
        assertPractitionerBudget(fullItem1, fullItem2);
    }

    private void assertResult(JSONObject result) {
        assertThat(result.getString("createdAt")).isNotEmpty();
        assertThat(result.getJSONObject("beneficiary").getLong("id")).isEqualTo(beneficiaryObj.getLong("id"));
        assertThat(result.getJSONObject("practitioner").getLong("id")).isEqualTo(practitionerObj.getLong("id"));
        assertThat(result.getJSONObject("petitioner").getLong("id")).isEqualTo(practitionerObj.getLong("id"));
        assertThat(result.getJSONObject("medicalCenter").getLong("id")).isEqualTo(medicalCenterObj.getLong("id"));
        assertThat(result.getJSONObject("contract").getLong("id")).isEqualTo(contractObj.getLong("id"));
        assertThat(result.getJSONObject("city").getLong("id")).isEqualTo(medicalCenterObj.getJSONObject("address")
                .getJSONObject("city").getLong("id"));
        assertThat(result.getBoolean("audited")).isFalse();
        assertThat(result.get("authorizationCondition")).isEqualTo(null);
        assertThat(result.getBoolean("audited")).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(result.getJSONObject(LINKS).getJSONObject(RESOURCE_RECEIPT)).isNotNull();
    }

    private void assertItems(JSONObject medicalAuthorization) {
        JSONArray items = medicalAuthorization.getJSONObject(EMBEDDED).getJSONArray("authorizationItems");
        assertThat(items).hasSize(2);
        items.forEach(item -> assertItem((JSONObject) item));
        assertItemCharges(items, medicalAuthorization.getBigDecimal("chargeTotal")
                .setScale(0, RoundingMode.DOWN));
    }

    private void assertItem(JSONObject item) {
        assertThat(item.getBoolean("settled")).isTrue();
        assertThat(item.getString("createdAt")).isNotBlank();
        assertThat(item.getInt("quantity")).isNotNull();
        assertThat(item.getJSONObject("nomenclator")).isNotNull();
        assertThat(item.getBigDecimal("chargeUnitPrice")).isNotNull();
        assertThat(item.getBigDecimal("chargeSubtotal")).isNotNull();
        assertThat(item.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(item.getJSONObject(LINKS).getJSONObject(RESOURCE_AUDIT_LOGS)).isNotNull();
    }

    private void assertItemCharges(JSONArray items, BigDecimal chargeTotal) {
        JSONObject item1 = findItem(items, contractItem1Obj.getJSONObject("nomenclator"));
        JSONObject item2 = findItem(items, contractItem2Obj.getJSONObject("nomenclator"));

        BigDecimal chargeSubtotal1 = calculateCharges(item1, medicalCoverageItem1Obj, contractItem1Obj);
        BigDecimal chargeSubtotal2 = calculateCharges(item2, medicalCoverageItem2Obj, contractItem2Obj);

        assertThat(item1.getBigDecimal("chargeSubtotal").setScale(0, RoundingMode.DOWN))
                .isEqualTo(chargeSubtotal1);
        assertThat(item2.getBigDecimal("chargeSubtotal").setScale(0, RoundingMode.DOWN))
                .isEqualTo(chargeSubtotal2);
        assertThat(chargeSubtotal1.add(chargeSubtotal2)).isEqualTo(chargeTotal);
    }

    private void assertApprovedItemValues(JSONObject item1, JSONObject item2) {
        assertThat(item1.getBigDecimal("subtotal").setScale(2, RoundingMode.HALF_UP))
                .isEqualTo(contractItem1Obj.getBigDecimal("value")
                        .multiply(new BigDecimal(item1.getLong("quantity"))).setScale(2, RoundingMode.HALF_UP));
        assertThat(item2.getBigDecimal("subtotal").setScale(2, RoundingMode.HALF_UP))
                .isEqualTo(contractItem2Obj.getBigDecimal("value")
                        .multiply(new BigDecimal(item2.getLong("quantity"))).setScale(2, RoundingMode.HALF_UP));
    }

    private void assertSettlement(JSONObject item1, JSONObject item2) throws Exception {
        JSONObject openSettlementEmbedded = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_SETTLEMENTS, "?page=1&size=50"),
                adminJwtAccessToken);

        JSONObject openSettlement = openSettlementEmbedded.getJSONObject(EMBEDDED).getJSONArray("settlements").getJSONObject(0);

        BigDecimal itemSubtotals;
        if (beneficiaryObj.getJSONObject("paymentMethod").getLong("id") == VOLUNTARY.getId())
            itemSubtotals = item1.getBigDecimal("subtotal")
                    .subtract(item1.getBigDecimal("chargeSubtotal"))
                    .add(item2.getBigDecimal("subtotal")
                            .subtract(item2.getBigDecimal("chargeSubtotal")))
                    .setScale(2, RoundingMode.HALF_UP);
        else
            itemSubtotals = item1.getBigDecimal("subtotal")
                    .add(item2.getBigDecimal("subtotal"))
                    .setScale(2, RoundingMode.HALF_UP);

        assertThat(openSettlement.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP))
                .isEqualTo(itemSubtotals);
        assertThat(openSettlement.getJSONObject("status").getLong("id")).isEqualTo(OPEN_SETTLEMENT.getId());
        assertThat(openSettlement.get("closedAt")).isEqualTo(null);
        assertThat(openSettlement.getString("openedAt")).isNotNull();
        assertThat(openSettlement.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(openSettlement.getJSONObject(LINKS).getJSONObject(RESOURCE_SETTLEMENT_ITEMS)).isNotNull();
        assertThat(openSettlement.getJSONObject(LINKS).getJSONObject(RESOURCE_RECEIPT)).isNotNull();
        assertThat(openSettlement.getJSONObject("practitioner").getLong("id")).isEqualTo(practitionerObj.getLong("id"));
        assertThat(openSettlement.getLong("id")).isNotNull();

        JSONObject closingObject = new JSONObject();
        closingObject.put("operation", SettlementOperation.CLOSE);
        JSONObject closedSettlement = TestUtils.putAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_SETTLEMENTS, SLASH, openSettlement.getLong("id")),
                closingObject.toString(),
                adminJwtAccessToken);

        assertThat(closedSettlement.getJSONObject("status").getLong("id")).isEqualTo(CLOSED_SETTLEMENT.getId());
        assertThat(closedSettlement.getString("closedAt")).isNotEmpty();
    }

    private void assertBeneficiaryBudget(JSONObject item1, JSONObject item2) throws Exception {
        JSONObject beneficiaryBudget = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_BENEFICIARY_BUDGETS, "?page=1&size=10&beneficiaryId=", beneficiaryObj.getLong("id")),
                adminJwtAccessToken);
        JSONObject budget = beneficiaryBudget.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_BUDGETS).getJSONObject(0);
        assertThat(budget.getString("createdAt")).isNotEmpty();
        assertThat(budget.getJSONObject("beneficiary").getLong("id")).isEqualTo(beneficiaryObj.getLong("id"));
        assertThat(budget.getJSONObject("company").getLong("id")).isEqualTo(beneficiaryObj.getJSONObject("company").getLong("id"));
        assertThat(budget.getLong("id")).isNotNull();
        assertThat((budget.get("closedAt"))).isEqualTo(null);
        assertThat(budget.getJSONObject("status").getLong("id")).isEqualTo(NOT_PAYED.getId());
        assertThat(budget.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP))
                .isEqualTo(item1.getBigDecimal("chargeSubtotal").add(item2.getBigDecimal("chargeSubtotal")).setScale(2, RoundingMode.HALF_UP));
        assertThat(budget.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(budget.getJSONObject(LINKS).getJSONObject(RESOURCE_RECEIPT)).isNotNull();
        assertThat(budget.getJSONObject(LINKS).getJSONObject(RESOURCE_BUDGET_ITEMS)).isNotNull();
    }

    private void assertPractitionerBudget(JSONObject item1, JSONObject item2) throws Exception {
        JSONObject practitionerBudget = TestUtils.getAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_PRACTITIONER_BUDGETS, "?page=1&size=10&practitionerId=", practitionerObj.getLong("id")),
                medicalCenterJwtAccessToken);
        JSONObject budget = practitionerBudget.getJSONObject(EMBEDDED).getJSONArray(RESOURCE_BUDGETS).getJSONObject(0);
        assertThat(budget.getString("createdAt")).isNotEmpty();
        assertThat(budget.getJSONObject("practitioner").getLong("id")).isEqualTo(practitionerObj.getLong("id"));
        assertThat(budget.getJSONObject("medicalCenter").getLong("id")).isEqualTo(medicalCenterObj.getLong("id"));
        assertThat(budget.getLong("id")).isNotNull();
        assertThat((budget.get("closedAt"))).isEqualTo(null);
        assertThat(budget.getJSONObject("status").getLong("id")).isEqualTo(NOT_PAYED.getId());
        assertThat(budget.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP))
                .isEqualTo(item1.getBigDecimal("chargeSubtotal").add(item2.getBigDecimal("chargeSubtotal")).setScale(2, RoundingMode.HALF_UP));
        assertThat(budget.getJSONObject(LINKS).getJSONObject(SELF)).isNotNull();
        assertThat(budget.getJSONObject(LINKS).getJSONObject(RESOURCE_RECEIPT)).isNotNull();
        assertThat(budget.getJSONObject(LINKS).getJSONObject(RESOURCE_BUDGET_ITEMS)).isNotNull();

        JSONObject payedBudget = TestUtils.putAndReturnObject(this.mvc,
                StringUtils.join(ENDPOINT_PRACTITIONER_BUDGETS, SLASH, budget.getLong("id")),
                medicalCenterJwtAccessToken);

        assertThat(payedBudget.getString("closedAt")).isNotEmpty();
        assertThat(payedBudget.getJSONObject("status").getLong("id")).isEqualTo(PAYED.getId());
    }

    private JSONObject findItem(JSONArray items, JSONObject nomenclator) {
        for (Object item : items) {
            JSONObject i = (JSONObject) item;
            if (i.getJSONObject("nomenclator").getLong("id") == nomenclator.getLong("id"))
                return i;
        }
        return new JSONObject();
    }

    public BigDecimal calculateCharges(JSONObject itemObj, JSONObject medicalCoverageItemObj, JSONObject contractItemObj) {
        long chargeTypeId = medicalCoverageItemObj.getJSONObject("chargeType").getLong("id");
        if (chargeTypeId == ChargeTypeReference.PERCENTAGE.getId()) {
            return contractItemObj.getBigDecimal("value")
                    .multiply(medicalCoverageItemObj.getBigDecimal("chargeValue"))
                    .divide(new BigDecimal(100), 0, RoundingMode.DOWN)
                    .multiply(new BigDecimal(itemObj.getLong("quantity"))).setScale(0, RoundingMode.DOWN);
        }
        if (chargeTypeId == ChargeTypeReference.FIXED_VALUE.getId()) {
            return medicalCoverageItemObj.getBigDecimal("chargeValue")
                    .multiply(new BigDecimal(itemObj.getLong("quantity"))).setScale(0, RoundingMode.HALF_DOWN);
        }
        return new BigDecimal(0);
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

    private void initRegions() throws Exception {
        long region1 = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_REGIONS,
                utils.readFileToJsonObject(SCHEMA_REGION_1).toString(), adminJwtAccessToken);
        region1Obj = new JSONObject();
        region1Obj.put("id", region1);
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

    private void initInsurancePlans() throws Exception {
        long insurancePlanId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_INSURANCE_PLANS,
                utils.readFileToJsonObject(SCHEMA_INSURANCE_PLAN_1).toString(),
                adminJwtAccessToken);
        insurancePlanObj = new JSONObject();
        insurancePlanObj.put("id", insurancePlanId);
        long specialInsurancePlanId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_INSURANCE_PLANS,
                utils.readFileToJsonObject(SCHEMA_INSURANCE_PLAN_2).toString(),
                adminJwtAccessToken);
        specialInsurancePlanObj = new JSONObject();
        specialInsurancePlanObj.put("id", specialInsurancePlanId);
    }

    private void initOrganizationContract() throws Exception {
        JSONObject contract = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contract.put("dateFrom", LocalDate.now().toString());
        contract.put("dateTo", LocalDate.now().plusMonths(6).toString());
        contract.put("organization", organizationObj);

        long contractId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_ORGANIZATION_CONTRACT,
                contract.toString(),
                adminJwtAccessToken);
        contractObj = new JSONObject();
        contractObj.put("id", contractId);

        JSONObject fixedContractItem1 = new JSONObject();
        fixedContractItem1.put("nomenclator", nomenclator1Obj);
        fixedContractItem1.put("value", new BigDecimal("321.47").setScale(2, RoundingMode.HALF_UP));
        fixedContractItem1.put("refundable", false);

        JSONObject fixedContractItem2 = new JSONObject();
        fixedContractItem2.put("nomenclator", nomenclator2Obj);
        fixedContractItem2.put("value", new BigDecimal("412.98").setScale(2, RoundingMode.HALF_UP));
        fixedContractItem2.put("refundable", false);

        String fixedContractItemEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_FIXED_CONTRACT_ITEMS);

        contractItem1Obj = TestUtils.createAndGetObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem1.toString(),
                adminJwtAccessToken);

        contractItem2Obj = TestUtils.createAndGetObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem2.toString(),
                adminJwtAccessToken);
    }

    private void initPractitionerContract() throws Exception {
        JSONObject contract = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contract.put("dateFrom", LocalDate.now().toString());
        contract.put("dateTo", LocalDate.now().plusMonths(6).toString());
        contract.put("practitioner", practitionerObj);

        long contractId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_PRACTITIONER_CONTRACT,
                contract.toString(),
                adminJwtAccessToken);
        contractObj = new JSONObject();
        contractObj.put("id", contractId);

        JSONObject fixedContractItem1 = new JSONObject();
        fixedContractItem1.put("nomenclator", nomenclator1Obj);
        fixedContractItem1.put("refundable", false);
        fixedContractItem1.put("value", new BigDecimal("123.49").setScale(2, RoundingMode.HALF_UP));

        JSONObject fixedContractItem2 = new JSONObject();
        fixedContractItem2.put("nomenclator", nomenclator2Obj);
        fixedContractItem2.put("refundable", false);
        fixedContractItem2.put("value", new BigDecimal("333.35").setScale(2, RoundingMode.HALF_UP));

        String fixedContractItemEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_FIXED_CONTRACT_ITEMS);

        contractItem1Obj = TestUtils.createAndGetObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem1.toString(),
                adminJwtAccessToken);

        contractItem2Obj = TestUtils.createAndGetObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem2.toString(),
                adminJwtAccessToken);
    }

    private void initMedicalCenterContract() throws Exception {
        JSONObject contract = utils.readFileToJsonObject(SCHEMA_CONTRACT_1);
        contract.put("dateFrom", LocalDate.now().toString());
        contract.put("dateTo", LocalDate.now().plusMonths(6).toString());
        contract.put("medicalCenter", medicalCenterObj);

        long contractId = TestUtils.createAndReturnLocationId(
                this.mvc,
                ENDPOINT_MEDICAL_CENTER_CONTRACT,
                contract.toString(),
                adminJwtAccessToken);
        contractObj = new JSONObject();
        contractObj.put("id", contractId);

        JSONObject fixedContractItem1 = new JSONObject();
        fixedContractItem1.put("nomenclator", nomenclator1Obj);
        fixedContractItem1.put("refundable", false);
        fixedContractItem1.put("value", new BigDecimal("434.58").setScale(2, RoundingMode.HALF_UP));

        JSONObject fixedContractItem2 = new JSONObject();
        fixedContractItem2.put("nomenclator", nomenclator2Obj);
        fixedContractItem2.put("refundable", false);
        fixedContractItem2.put("value", new BigDecimal("279.25").setScale(2, RoundingMode.HALF_UP));

        String fixedContractItemEndpoint = StringUtils.join(ENDPOINT_CONTRACTS, SLASH, contractId, ENDPOINT_FIXED_CONTRACT_ITEMS);

        contractItem1Obj = TestUtils.createAndGetObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem1.toString(),
                adminJwtAccessToken);

        contractItem2Obj = TestUtils.createAndGetObject(this.mvc,
                fixedContractItemEndpoint,
                fixedContractItem2.toString(),
                adminJwtAccessToken);
    }

    private void initBeneficiaries() throws Exception {
        JSONObject beneficiaryJsonObject = utils.readFileToJsonObject(IntegrationTestConstants.SCHEMA_BENEFICIARY);
        JSONArray insurancePlans = new JSONArray();
        JSONObject beneficiaryInsurancePlan = new JSONObject();
        beneficiaryInsurancePlan.put("insurancePlan", insurancePlanObj);
        insurancePlans.put(beneficiaryInsurancePlan);
        JSONObject beneficiarySpecialInsurancePlan = new JSONObject();
        beneficiarySpecialInsurancePlan.put("insurancePlan", specialInsurancePlanObj);
        insurancePlans.put(beneficiarySpecialInsurancePlan);
        beneficiaryJsonObject.put("beneficiaryInsurancePlans", insurancePlans);
        beneficiaryObj = TestUtils.createAndGetObject(
                this.mvc,
                ENDPOINT_BENEFICIARIES,
                beneficiaryJsonObject.toString(),
                adminJwtAccessToken);
    }

    private void initPractitioners() throws Exception {
        long practitionerCategoryId = TestUtils.createAndReturnLocationId(this.mvc, ENDPOINT_PRACTITIONER_CATEGORIES,
                utils.readFileToJsonObject(SCHEMA_PRACTITIONER_CATEGORY_1).toString(),
                adminJwtAccessToken);
        JSONObject practitionerCategory = new JSONObject();
        practitionerCategory.put("id", practitionerCategoryId);
        JSONObject practitioner = utils.readFileToJsonObject(SCHEMA_PRACTITIONER);
        practitioner.put("practitionerCategory", practitionerCategory);
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

    private void associateContractToPractitioner() throws Exception {
        TestUtils.putObject(mvc, StringUtils.join(ENDPOINT_PRACTITIONERS,
                SLASH,
                practitionerObj.getLong("id"),
                ENDPOINT_CONTRACTS,
                SLASH,
                contractObj.getLong("id")),
                adminJwtAccessToken);
    }

    private void initMedicalCoverages() throws Exception {
        JSONObject medicalCoverage = utils.readFileToJsonObject(SCHEMA_MEDICAL_COVERAGE);
        medicalCoverage.put("region", region1Obj);
        JSONObject medicalCoverageItem1 = utils.readFileToJsonObject(SCHEMA_MEDICAL_COVERAGE_ITEM_1);
        medicalCoverageItem1.put("nomenclator", nomenclator1Obj);
        JSONObject medicalCoverageItem2 = utils.readFileToJsonObject(SCHEMA_MEDICAL_COVERAGE_ITEM_2);
        medicalCoverageItem2.put("nomenclator", nomenclator2Obj);

        long medicalCoverageId = TestUtils.createAndReturnLocationId(this.mvc,
                StringUtils.join(ENDPOINT_INSURANCE_PLANS, SLASH, insurancePlanObj.getInt("id"), ENDPOINT_MEDICAL_COVERAGES),
                medicalCoverage.toString(),
                adminJwtAccessToken);

        String medicalCoverageEndpoint = StringUtils.join(ENDPOINT_MEDICAL_COVERAGES, SLASH, medicalCoverageId, ENDPOINT_MEDICAL_COVERAGE_ITEMS);

        medicalCoverageItem1Obj = TestUtils.createAndGetObject(this.mvc,
                medicalCoverageEndpoint,
                medicalCoverageItem1.toString(),
                adminJwtAccessToken);

        medicalCoverageItem2Obj = TestUtils.createAndGetObject(this.mvc,
                medicalCoverageEndpoint,
                medicalCoverageItem2.toString(),
                adminJwtAccessToken);
    }

    private void initLimitedMedicalCoverages() throws Exception {
        JSONObject medicalCoverage = utils.readFileToJsonObject(SCHEMA_MEDICAL_COVERAGE);
        medicalCoverage.put("region", region1Obj);
        JSONObject medicalCoverageItem1 = utils.readFileToJsonObject(SCHEMA_MEDICAL_COVERAGE_ITEM_1);
        medicalCoverageItem1.put("nomenclator", nomenclator1Obj);
        medicalCoverageItem1.put("gender", "FEMENINO");
        medicalCoverageItem1.put("ageFrom", 0);
        medicalCoverageItem1.put("ageTo", 1);
        JSONObject medicalCoverageItem2 = utils.readFileToJsonObject(SCHEMA_MEDICAL_COVERAGE_ITEM_2);
        medicalCoverageItem2.put("nomenclator", nomenclator2Obj);

        long medicalCoverageId = TestUtils.createAndReturnLocationId(this.mvc,
                StringUtils.join(ENDPOINT_INSURANCE_PLANS, SLASH, insurancePlanObj.getInt("id"), ENDPOINT_MEDICAL_COVERAGES),
                medicalCoverage.toString(),
                adminJwtAccessToken);

        String medicalCoverageEndpoint = StringUtils.join(ENDPOINT_MEDICAL_COVERAGES, SLASH, medicalCoverageId, ENDPOINT_MEDICAL_COVERAGE_ITEMS);

        medicalCoverageItem1Obj = TestUtils.createAndGetObject(this.mvc,
                medicalCoverageEndpoint,
                medicalCoverageItem1.toString(),
                adminJwtAccessToken);

        medicalCoverageItem2Obj = TestUtils.createAndGetObject(this.mvc,
                medicalCoverageEndpoint,
                medicalCoverageItem2.toString(),
                adminJwtAccessToken);
    }

    private void initAuditRules() throws Exception {
        JSONObject audit = new JSONObject();
        audit.put("id", RestrictionTypeReference.AUDIT.getId());
        JSONObject rule1 = utils.readFileToJsonObject(SCHEMA_RULE_1);
        rule1.put("restrictionType", audit);
        JSONObject rule2 = utils.readFileToJsonObject(SCHEMA_RULE_2);
        rule2.put("restrictionType", audit);
        TestUtils.createObject(this.mvc,
                ENDPOINT_RULE_CONFIGURATIONS,
                rule1.toString(),
                adminJwtAccessToken);
        TestUtils.createObject(this.mvc,
                ENDPOINT_RULE_CONFIGURATIONS,
                rule2.toString(),
                adminJwtAccessToken);
    }

    private void initAuditTray() throws Exception {
        JSONObject auditTray = new JSONObject();
        auditTray.put("name", "test");
        auditTray.put("purpose", "test");
        auditTray.put("color", "#000000");
        auditTray.put("region", region1Obj);

        JSONArray auditorArray = new JSONArray();

        JSONObject auditor = new JSONObject();
        auditor.put("sub", UUID.randomUUID().toString());
        auditor.put("username", "test");
        auditor.put("displayName", "test");

        auditorArray.put(auditor);

        JSONArray nomenclatorArray = new JSONArray();
        nomenclatorArray.put(nomenclator1Obj);
        nomenclatorArray.put(nomenclator2Obj);

        auditTray.put("auditors", auditorArray);
        auditTray.put("nomenclators", nomenclatorArray);

        TestUtils.createObject(this.mvc,
                ENDPOINT_AUDIT_TRAYS,
                auditTray.toString(),
                adminJwtAccessToken);
    }

    private void initRejectionRules() throws Exception {
        JSONObject rejection = new JSONObject();
        rejection.put("id", RestrictionTypeReference.REJECTION.getId());
        JSONObject rule1 = utils.readFileToJsonObject(SCHEMA_RULE_1);
        rule1.put("restrictionType", rejection);
        JSONObject rule2 = utils.readFileToJsonObject(SCHEMA_RULE_2);
        rule2.put("restrictionType", rejection);
        TestUtils.createObject(this.mvc,
                ENDPOINT_RULE_CONFIGURATIONS,
                rule1.toString(),
                adminJwtAccessToken);
        TestUtils.createObject(this.mvc,
                ENDPOINT_RULE_CONFIGURATIONS,
                rule2.toString(),
                adminJwtAccessToken);
    }

    private void initBatches() throws Exception {
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
        batchItems.put(batchItem2);
        batch.put("batchItems", batchItems);

        TestUtils.createObject(this.mvc,
                ENDPOINT_BATCHES, batch.toString(),
                adminJwtAccessToken);
    }

}
