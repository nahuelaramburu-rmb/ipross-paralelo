package com.capacidad.validationapi.misc.constant;

public final class ControllerEndpoints {

    public static final String ALL_PARAMS = "/**";

    public static final String ENDPOINT_ACTUATOR = "/actuator";
    public static final String ENDPOINT_AUDITORS = "/auditors";
    public static final String ENDPOINT_ADJUSTMENTS = "/adjustments";
    public static final String ENDPOINT_AUDIT_TRAYS = "/audit-trays";
    public static final String ENDPOINT_AUDIT_HISTORY = "/audit-history";
    public static final String ENDPOINT_AUTH = "/auth";
    public static final String ENDPOINT_KEY = "/key";
    public static final String ENDPOINT_AUTHORIZATIONS = "/authorizations";
    public static final String ENDPOINT_AUTHORIZATION_ITEMS = "/authorization-items";
    public static final String ENDPOINT_BATCHES = "/batches";
    public static final String ENDPOINT_BATCH_ITEMS = "/batch-items";
    public static final String ENDPOINT_BENEFICIARIES = "/beneficiaries";
    public static final String ENDPOINT_DASHBOARDS = "/dashboards";
    public static final String ENDPOINT_CATEGORIES = "/categories";
    public static final String ENDPOINT_BENEFICIARY_CATEGORIES = ENDPOINT_BENEFICIARIES + ENDPOINT_CATEGORIES;
    public static final String ENDPOINT_COMPANIES = "/companies";
    public static final String ENDPOINT_CONTRACTS = "/contracts";
    public static final String ENDPOINT_CONTRACT_ITEMS = "/contract-items";
    public static final String ENDPOINT_FIXED_CONTRACT_ITEMS = ENDPOINT_CONTRACT_ITEMS + "/fixed";
    public static final String ENDPOINT_EXPIRATIONS = "/expirations";
    public static final String ENDPOINT_GENERAL = "/general";
    public static final String ENDPOINT_HEALTH = "/actuator/health";
    public static final String ENDPOINT_INSURANCE_PLANS = "/insurance-plans";
    public static final String ENDPOINT_BENEFICIARY_INSURANCE_PLANS = ENDPOINT_BENEFICIARIES + ENDPOINT_INSURANCE_PLANS;
    public static final String ENDPOINT_MAXIMUM_ADJUSTMENTS = ENDPOINT_ADJUSTMENTS + "/maximum";
    public static final String ENDPOINT_MEDICAL_COVERAGES = "/medical-coverages";
    public static final String ENDPOINT_MEDICAL_COVERAGE_ITEMS = "/medical-coverage-items";
    public static final String ENDPOINT_MEDICAL_CENTERS = "/medical-centers";
    public static final String ENDPOINT_MEDICAL_REGISTRATIONS = "/medical-registrations";
    public static final String ENDPOINT_MONETARY_ADJUSTMENTS = ENDPOINT_ADJUSTMENTS + "/monetary";
    public static final String ENDPOINT_NOMENCLATORS = "/nomenclators";
    public static final String ENDPOINT_NOMENCLATOR_GROUPS = "/nomenclator-groups";
    public static final String ENDPOINT_PRACTITIONERS = "/practitioners";
    public static final String ENDPOINT_PRACTITIONER_CATEGORIES = ENDPOINT_PRACTITIONERS + ENDPOINT_CATEGORIES;
    public static final String ENDPOINT_REGIONS = "/regions";
    public static final String ENDPOINT_RULE_CONFIGURATIONS = "/rule-configurations";
    public static final String ENDPOINT_ORGANIZATIONS = "/organizations";
    public static final String ENDPOINT_PROPERTIES = "/properties";
    public static final String ENDPOINT_SETTLEMENTS = "/settlements";
    public static final String ENDPOINT_STORAGE = "/storage";
    public static final String ENDPOINT_USAGE_RATE_ADJUSTMENTS = ENDPOINT_ADJUSTMENTS + "/usage-rate";
    public static final String ENDPOINT_VERIFICATION = "/verification";
    public static final String ENDPOINT_PRE_AUTHORIZATIONS = "/pre-authorizations";
    public static final String ENDPOINT_PRESCRIPTIONS = "/prescriptions";
    public static final String ENDPOINT_PRESCRIPTION_RESTRICTIONS = "/prescription-restrictions";
    public static final String ENDPOINT_MEDICINES = "/medicines";
    public static final String ENDPOINT_NOTIFICATION_PUBLICATIONS = "/notification-publications";

    public static final String ENDPOINT_PROCEDURES = "/procedures";
    public static final String ENDPOINT_CUD_PROCEDURES = ENDPOINT_PROCEDURES + "/cud";
    public static final String ENDPOINT_CERTIFICATE_PROCEDURES = ENDPOINT_PROCEDURES + "/certificate";
    public static final String ENDPOINT_DISABILITY_PROCEDURES = ENDPOINT_PROCEDURES + "/disability";
    public static final String ENDPOINT_UNKNOWN_AUTHORIZATION_PROCEDURES = ENDPOINT_PROCEDURES + "/unknown-authorization";

    public static final String ENDPOINT_BUDGETS = "/budgets";
    public static final String ENDPOINT_BENEFICIARY_BUDGETS = ENDPOINT_BUDGETS + ENDPOINT_BENEFICIARIES;
    public static final String ENDPOINT_PRACTITIONER_BUDGETS = ENDPOINT_BUDGETS + ENDPOINT_PRACTITIONERS;

    public static final String ENDPOINT_ORGANIZATION_CONTRACT = ENDPOINT_CONTRACTS + "/organization";
    public static final String ENDPOINT_PRACTITIONER_CONTRACT = ENDPOINT_CONTRACTS + "/practitioner";
    public static final String ENDPOINT_MEDICAL_CENTER_CONTRACT = ENDPOINT_CONTRACTS + "/medical-center";

    public static final String ENDPOINT_WEBSOCKET_AUDIT = "/audit";
    public static final String ENDPOINT_WEBSOCKET_AUDITORS = "/auditors";
    public static final String ENDPOINT_WEBSOCKET_AUDIT_TRAYS = "/audit-trays";
    public static final String ENDPOINT_WEBSOCKET_NOTIFICATIONS = "/notifications";

    public static final String ENDPOINT_CALENDAR = "/calendar";
    public static final String ENDPOINT_TRADE_UNIONS = "/trade-unions";
    public static final String ENDPOINT_TRADE_UNION_COVERAGE_ITEMS = "/trade-union-coverage-items";

    private ControllerEndpoints() {
    }

}
