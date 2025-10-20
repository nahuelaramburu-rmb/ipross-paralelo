package com.capacidad.validationapi;

import org.apache.commons.lang3.StringUtils;

public final class IntegrationTestConstants {

    public static final String LOCATION_HEADER = "Location";
    private static final String FILES_FOLDER = "integration_json_files";

    public static final String SCHEMA_BENEFICIARY_CATEGORY = StringUtils.join(FILES_FOLDER, "/beneficiary/beneficiary_category.json");
    public static final String SCHEMA_BENEFICIARY = StringUtils.join(FILES_FOLDER, "/beneficiary/beneficiary.json");
    public static final String SCHEMA_RELATED_BENEFICIARY = StringUtils.join(FILES_FOLDER, "/beneficiary/beneficiary_related.json");
    public static final String SCHEMA_EXPIRATION = StringUtils.join(FILES_FOLDER, "/beneficiary/expiration.json");

    public static final String SCHEMA_INSURANCE_PLAN_1 = FILES_FOLDER + "/insurance_plan/insurance_plan_1.json";
    public static final String SCHEMA_INSURANCE_PLAN_2 = FILES_FOLDER + "/insurance_plan/insurance_plan_2.json";

    public static final String SCHEMA_MEDICAL_COVERAGE = StringUtils.join(FILES_FOLDER, "/medical-coverage/medical_coverage.json");
    public static final String SCHEMA_MEDICAL_COVERAGE_ITEM_1 = StringUtils.join(FILES_FOLDER, "/medical-coverage/medical_coverage_item_1.json");
    public static final String SCHEMA_MEDICAL_COVERAGE_ITEM_2 = StringUtils.join(FILES_FOLDER, "/medical-coverage/medical_coverage_item_2.json");

    public static final String SCHEMA_NOMENCLATOR_1 = StringUtils.join(FILES_FOLDER, "/nomenclator/nomenclator_1.json");
    public static final String SCHEMA_NOMENCLATOR_2 = StringUtils.join(FILES_FOLDER, "/nomenclator/nomenclator_2.json");

    public static final String SCHEMA_MEDICAL_PRACTICE_1 = StringUtils.join(FILES_FOLDER, "/nomenclator/medical_practice_1.json");
    public static final String SCHEMA_MEDICAL_PRACTICE_2 = StringUtils.join(FILES_FOLDER, "/nomenclator/medical_practice_2.json");

    public static final String
            SCHEMA_PRACTITIONER = StringUtils.join(FILES_FOLDER, "/practitioner/practitioner.json");
    public static final String SCHEMA_PRACTITIONER_CATEGORY_1 = StringUtils.join(FILES_FOLDER, "/practitioner/practitioner_category_1.json");
    public static final String SCHEMA_PRACTITIONER_CATEGORY_2 = StringUtils.join(FILES_FOLDER, "/practitioner/practitioner_category_2.json");

    public static final String SCHEMA_ORGANIZATION_1 = StringUtils.join(FILES_FOLDER, "/organization/organization_1.json");
    public static final String SCHEMA_ORGANIZATION_2 = StringUtils.join(FILES_FOLDER, "/organization/organization_2.json");
    public static final String SCHEMA_ORGANIZATION_3 = StringUtils.join(FILES_FOLDER, "/organization/organization_3.json");

    public static final String SCHEMA_MEDICAL_CENTER_1 = StringUtils.join(FILES_FOLDER, "/medical_center/medical_center_1.json");
    public static final String SCHEMA_MEDICAL_CENTER_2 = StringUtils.join(FILES_FOLDER, "/medical_center/medical_center_2.json");

    public static final String SCHEMA_CONTRACT_1 = StringUtils.join(FILES_FOLDER, "/contract/contract_1.json");
    public static final String SCHEMA_CONTRACT_2 = StringUtils.join(FILES_FOLDER, "/contract/contract_2.json");

    public static final String SCHEMA_REGION_1 = StringUtils.join(FILES_FOLDER, "/region/region_1.json");
    public static final String SCHEMA_REGION_2 = StringUtils.join(FILES_FOLDER, "/region/region_2.json");

    public static final String SCHEMA_RULE_1 = StringUtils.join(FILES_FOLDER, "/rule/rule_1.json");
    public static final String SCHEMA_RULE_2 = StringUtils.join(FILES_FOLDER, "/rule/rule_2.json");
    public static final String SCHEMA_RULE_3 = StringUtils.join(FILES_FOLDER, "/rule/rule_3.json");
    public static final String SCHEMA_RULE_4 = StringUtils.join(FILES_FOLDER, "/rule/rule_4.json");

    public static final String SCHEMA_COMPANY_1 = StringUtils.join(FILES_FOLDER, "/company/company_1.json");


    private IntegrationTestConstants() {
    }

}
