package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.config.token.TokenBuilder;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomTokenService {

    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final TokenBuilder tokenBuilder;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Crea un access token y un refresh token nuevos (equivalente a  createAccessToken).
     */
    public OAuth2Authorization createAccessToken(Authentication authentication, RegisteredClient registeredClient) {
        // construye autorización
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(authentication.getName())
                .authorizationGrantType(AuthorizationGrantType.PASSWORD) // o REFRESH_TOKEN / CLIENT_CREDENTIALS según tu flujo
                .attribute(Principal.class.getName(), authentication);

        // genera access token
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(authentication)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorization(builder.build())
                .build();

        OAuth2AccessToken accessToken = (OAuth2AccessToken) tokenGenerator.generate(tokenContext);
        builder.token(accessToken);

        // genera refresh token
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(UUID.randomUUID().toString(),
                Instant.now(),
                Instant.now().plus(registeredClient.getTokenSettings().getRefreshTokenTimeToLive()));

        builder.refreshToken(refreshToken);

        OAuth2Authorization authorization = builder.build();
        authorizationService.save(authorization);

        return authorization;
    }

    /**
     * Refresca el access token (equivalente a tu refreshAccessToken).
     */
    public OAuth2Authorization refreshAccessToken(String refreshTokenValue, RegisteredClient registeredClient) {
        OAuth2Authorization authorization = authorizationService.findByToken(refreshTokenValue, OAuth2TokenType.REFRESH_TOKEN);
        if (authorization == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_grant"), "Invalid refresh token");
        }

        Authentication currentUserAuth = authorization.getAttribute(Principal.class.getName());

        // recargar datos del usuario
        UserDetails refreshedUserDetails = customUserDetailsService.loadUserByUsername(currentUserAuth.getName());
        if (refreshedUserDetails instanceof CustomUserDetails cud) {
            cud.setApplicationUser(((CustomUserDetails) refreshedUserDetails).getApplicationUser());
        }

        // crear un nuevo access token
        return createAccessToken(currentUserAuth, registeredClient);
    }
}

