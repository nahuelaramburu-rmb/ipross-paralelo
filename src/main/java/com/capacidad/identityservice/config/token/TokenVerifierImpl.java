package com.capacidad.identityservice.config.token;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.utils.exception.ExpiredTokenException;
import com.capacidad.utils.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenVerifierImpl implements TokenVerifier {

    private final JwtDecoder jwtDecoder;
    private final ApplicationProperties applicationProperties;

    public TokenVerifierImpl(ApplicationProperties applicationProperties) {

        this.applicationProperties = applicationProperties;

        this.jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(Utils.getFileURL("keys.jwks.json").toString())  // verificar filename !!
                .build();
    }


    @Override
    public void verify(String token) {
        try {
            Jwt decoded = jwtDecoder.decode(token);

            // validación adicional opcional: issuer exacto
            String expectedIssuer = Utils.buildJwtIssuer(
                    applicationProperties.getJwtIssuer(),
                    applicationProperties.getActiveProfile()
            );

            if (decoded.getIssuer() == null || !expectedIssuer.equals(decoded.getIssuer().toString())) {
                throw new InvalidTokenException("tokenVerifier.invalidIssuer");
            }

            // si llegamos acá, la firma y los checks básicos (exp/nbf) ya fueron validados

        } catch (JwtException e) {
            log.error("Token validation error for token [{}]: {}", token, e.getMessage());

            // intentar detectar expirado (mejor: basarse en tipo o mensajes)
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("expired")) {
                throw new ExpiredTokenException("tokenVerifier.expiredToken");
            }
            throw new InvalidTokenException("tokenVerifier.invalidToken");
        }
    }

    @Override
    public void validate(String jwt) {
        try {
            Jwt decoded = jwtDecoder.decode(jwt);

            // valida audience
            String apiId = applicationProperties.getApiIdentifier();
            if (apiId != null && !decoded.getAudience().contains(apiId)) {
                throw new InvalidTokenException("tokenVerifier.invalidAudience");
            }

            // otras validaciones , a implementar (scope, claim custom, etc.)


        } catch (JwtException e) {
            log.error("Token validate error for token [{}]: {}", jwt, e.getMessage());
            throw new InvalidTokenException("tokenVerifier.invalidToken");
        }
    }
}


