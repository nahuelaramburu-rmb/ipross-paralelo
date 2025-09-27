package com.capacidad.identityservice.misc.constant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static com.capacidad.utils.Constants.ROLE_PREFIX;

public final class SecurityConstants {

    public static final String TENANT_ID_HEADER = "X-TenantID";
    public static final String TENANT_ID = "tenant_id";
    public static final String TENANT_NAME = "tenant_name";

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String JWT_CLAIM_CLIENT_ID = "client_id";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_EMAIL_VERIFIED = "email_verified";
    public static final String JWT_CLAIM_SUBROLE = "subrole";

    // roles
    public static final String BENEFICIARY = "BENEFICIARY";
    public static final String PRACTITIONER = "PRACTITIONER";
    public static final String ADMIN = "ADMIN";
    public static final String CLIENT = "CLIENT";
    public static final String MEDICAL_CENTER = "MEDICAL_CENTER";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String AUDITOR = "AUDITOR";
    public static final String FUNDER = "FUNDER";

    // le asigna authorities a cada rol
    private static final String ROLE_BENEFICIARY = StringUtils.join(ROLE_PREFIX, BENEFICIARY);
    public static final SimpleGrantedAuthority ROLE_BENEFICIARY_AUTHORITY = new SimpleGrantedAuthority(ROLE_BENEFICIARY);

    private static final String ROLE_PRACTITIONER = StringUtils.join(ROLE_PREFIX, PRACTITIONER);
    public static final SimpleGrantedAuthority ROLE_PRACTITIONER_AUTHORITY = new SimpleGrantedAuthority(ROLE_PRACTITIONER);

    private static final String ROLE_ADMIN = StringUtils.join(ROLE_PREFIX, ADMIN);
    public static final SimpleGrantedAuthority ROLE_ADMIN_AUTHORITY = new SimpleGrantedAuthority(ROLE_ADMIN);

    private static final String ROLE_CLIENT = StringUtils.join(ROLE_PREFIX, CLIENT);
    public static final SimpleGrantedAuthority ROLE_CLIENT_AUTHORITY = new SimpleGrantedAuthority(ROLE_CLIENT);

    private static final String ROLE_MEDICAL_CENTER = StringUtils.join(ROLE_PREFIX, MEDICAL_CENTER);
    public static final SimpleGrantedAuthority ROLE_MEDICAL_CENTER_AUTHORITY = new SimpleGrantedAuthority(ROLE_MEDICAL_CENTER);

    private static final String ROLE_ORGANIZATION = StringUtils.join(ROLE_PREFIX, ORGANIZATION);
    public static final SimpleGrantedAuthority ROLE_ORGANIZATION_AUTHORITY = new SimpleGrantedAuthority(ROLE_ORGANIZATION);

    private static final String ROLE_FUNDER = StringUtils.join(ROLE_PREFIX, FUNDER);
    public static final SimpleGrantedAuthority ROLE_FUNDER_AUTHORITY = new SimpleGrantedAuthority(ROLE_FUNDER);

    private SecurityConstants() {
    }
}