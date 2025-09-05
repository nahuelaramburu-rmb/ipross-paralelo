package com.capacidad.validationapi.misc;

import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COLON;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;

public final class SecurityUtils {

    private SecurityUtils() {

    }

    public static boolean isMedicalCenter() {
        return authoritiesContains(ROLE_MEDICAL_CENTER_INSTANCE);
    }

    public static boolean isOrganization() {
        return authoritiesContains(ROLE_ORGANIZATION_INSTANCE);
    }

    public static boolean isAdmin() {
        return authoritiesContains(ROLE_ADMIN_INSTANCE);
    }

    public static boolean isFunder() {
        return authoritiesContains(ROLE_FUNDER_INSTANCE);
    }

    public static boolean isPractitioner() {
        return authoritiesContains(ROLE_PRACTITIONER_INSTANCE);
    }

    public static boolean isBeneficiary() {
        return authoritiesContains(ROLE_BENEFICIARY_INSTANCE);
    }

    public static boolean isAuditor() {
        return authoritiesContains(ROLE_AUDITOR_INSTANCE);
    }

    public static boolean isClient() {
        return authoritiesContains(ROLE_CLIENT_INSTANCE);
    }

    public static boolean authoritiesContains(SimpleGrantedAuthority authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().contains(authority);
    }

    public static String buildScope(String operation, String resource) {
        return String.join(COLON, operation.toLowerCase(), resource.toLowerCase());
    }

    public static boolean isHighRankingAuthority() {
        return isAdmin() || isFunder() || isAuditor() || isClient();
    }

    public static Optional<UUID> getAuthenticatedAuthorityResourceId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JWTAuthenticationToken)
            return Optional.ofNullable(((JWTAuthenticationToken) authentication).getResourceId());
        return Optional.empty();
    }

    public static Optional<UUID> getAuthenticatedAuthoritySub() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JWTAuthenticationToken)
            return Optional.ofNullable(((JWTAuthenticationToken) authentication).getSub());
        return Optional.empty();
    }

    public static Optional<String> getAuthenticatedAuthorityPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null)
            return Optional.ofNullable(authentication.getPrincipal().toString());
        return Optional.empty();
    }

    public static Optional<String> getAuthenticatedAuthorityClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JWTAuthenticationToken)
            return Optional.of(((JWTAuthenticationToken) authentication).getClientId());
        return Optional.empty();
    }

}
