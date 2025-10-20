package com.capacidad.validationapi.misc.constant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class SecurityConstants {
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String ROLE_PREFIX = "ROLE_";

    public static final String ADMIN = "ADMIN";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String BENEFICIARY = "BENEFICIARY";
    public static final String FUNDER = "FUNDER";
    public static final String MEDICAL_CENTER = "MEDICAL_CENTER";
    public static final String PRACTITIONER = "PRACTITIONER";
    public static final String AUDITOR = "AUDITOR";
    public static final String CLIENT = "CLIENT";

    private static final String ROLE_ADMIN = StringUtils.join(ROLE_PREFIX, ADMIN);
    public static final SimpleGrantedAuthority ROLE_ADMIN_INSTANCE = new SimpleGrantedAuthority(ROLE_ADMIN);
    private static final String ROLE_CLIENT = StringUtils.join(ROLE_PREFIX, CLIENT);
    public static final SimpleGrantedAuthority ROLE_CLIENT_INSTANCE = new SimpleGrantedAuthority(ROLE_CLIENT);
    private static final String ROLE_ORGANIZATION = StringUtils.join(ROLE_PREFIX, ORGANIZATION);
    public static final SimpleGrantedAuthority ROLE_ORGANIZATION_INSTANCE = new SimpleGrantedAuthority(ROLE_ORGANIZATION);
    private static final String ROLE_BENEFICIARY = StringUtils.join(ROLE_PREFIX, BENEFICIARY);
    public static final SimpleGrantedAuthority ROLE_BENEFICIARY_INSTANCE = new SimpleGrantedAuthority(ROLE_BENEFICIARY);
    private static final String ROLE_FUNDER = StringUtils.join(ROLE_PREFIX, FUNDER);
    public static final SimpleGrantedAuthority ROLE_FUNDER_INSTANCE = new SimpleGrantedAuthority(ROLE_FUNDER);
    private static final String ROLE_MEDICAL_CENTER = StringUtils.join(ROLE_PREFIX, MEDICAL_CENTER);
    public static final SimpleGrantedAuthority ROLE_MEDICAL_CENTER_INSTANCE = new SimpleGrantedAuthority(ROLE_MEDICAL_CENTER);
    private static final String ROLE_PRACTITIONER = StringUtils.join(ROLE_PREFIX, PRACTITIONER);
    public static final SimpleGrantedAuthority ROLE_PRACTITIONER_INSTANCE = new SimpleGrantedAuthority(ROLE_PRACTITIONER);
    private static final String ROLE_AUDITOR = StringUtils.join(ROLE_PREFIX, AUDITOR);
    public static final SimpleGrantedAuthority ROLE_AUDITOR_INSTANCE = new SimpleGrantedAuthority(ROLE_AUDITOR);

    private SecurityConstants() {
    }
}
