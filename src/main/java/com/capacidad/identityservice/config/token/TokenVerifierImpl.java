package com.capacidad.identityservice.config.token;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.utils.exception.ExpiredTokenException;
import com.capacidad.utils.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;


/*
* Esta clase es un validador de tokens JWT con Spring Security, que asegura que los tokens:

    estén bien firmados,
    no estén expirados,
    provengan del emisor correcto,
    y opcionalmente tengan la audiencia esperada.
*
*
* */


@Slf4j
@Component
public class TokenVerifierImpl implements TokenVerifier {

    private final JwtDecoder jwtDecoder;
    private final ApplicationProperties applicationProperties;

    public TokenVerifierImpl(ApplicationProperties applicationProperties) {

        this.applicationProperties = applicationProperties;

        // init jwtDecoder con NimbusJwtDecoder
        //NimbusJwtDecoder -> valida la firma del token JWT con las claves contenidas en keys.jwks.json.
        this.jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(Utils.getFileURL("keys.jwks.json").toString())  // verificar filename !!
                .build();
    }


    // verifica la firma, expiracion y emisor del token
    @Override
    public void verify(String token) {
        try {

            //Decodifica el token
            Jwt decoded = jwtDecoder.decode(token);

            //Construye el issuer esperado
            String expectedIssuer = Utils.buildJwtIssuer(
                    applicationProperties.getJwtIssuer(),
                    applicationProperties.getActiveProfile()
            );

            // compara el issuer esperado con el claim iss del token.
            if (decoded.getIssuer() == null || !expectedIssuer.equals(decoded.getIssuer().toString())) {
                throw new InvalidTokenException("tokenVerifier.invalidIssuer");
            }

            // si llegamos acá, la firma y los checks básicos (exp/nbf) ya fueron validados

        } catch (JwtException e) {
            // firma  incorrecta o  vencida
            log.error("Token validation error for token [{}]: {}", token, e.getMessage());

            // intenta detectar expirado (mejor: basarse en tipo o mensajes)
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("expired")) {
                throw new ExpiredTokenException("tokenVerifier.expiredToken");  // token expirado
            }
            throw new InvalidTokenException("tokenVerifier.invalidToken"); //
        }
    }


    // Comprueba firma + audiencia
    @Override
    public void validate(String jwt) {
        try {
            Jwt decoded = jwtDecoder.decode(jwt);

            //Valida la audiencia (aud):
            String apiId = applicationProperties.getApiIdentifier();

            //Comprueba que el claim aud del JWT contenga ese identificador.
            if (apiId != null && !decoded.getAudience().contains(apiId)) {

                //Si no lo contiene
                throw new InvalidTokenException("tokenVerifier.invalidAudience");
            }

            // otras validaciones , a implementar (scope, claim custom, etc.)


        } catch (JwtException e) {
            // problema al decodificar el token
            log.error("Token validate error for token [{}]: {}", jwt, e.getMessage());
            throw new InvalidTokenException("tokenVerifier.invalidToken");
        }
    }
}


