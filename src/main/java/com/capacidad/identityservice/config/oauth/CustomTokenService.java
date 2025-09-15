package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomTokenService implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final CustomUserDetailsService customUserDetailsService;
    private final RedisOAuth2AuthorizationService authorizationService;

    @Override
    public void customize(JwtEncodingContext context) {
        String username = context.getPrincipal().getName();

        // Refrescar datos del usuario
        UserDetails refreshed = customUserDetailsService.loadUserByUsername(username);
        CustomUserDetails customUser = (CustomUserDetails) refreshed;

        context.getClaims().claim("user_id", customUser.getApplicationUser().getId());
        context.getClaims().claim("email", customUser.getApplicationUser().getEmail());
        context.getClaims().claim("tenant_id", customUser.getTenantId());

        log.debug("JWT personalizado para usuario {}", username);
    }

    public OAuth2Authorization findAuthorization(String tokenValue) {
        return authorizationService.findByToken(tokenValue, null);
    }

    public void revokeAuthorization(String tokenValue) {
        OAuth2Authorization auth = authorizationService.findByToken(tokenValue, null);
        if (auth != null) {
            authorizationService.remove(auth);
            log.info("Token revocado {}", tokenValue);
        }
    }
}
