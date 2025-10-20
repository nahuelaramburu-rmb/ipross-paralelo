package com.capacidad.identityservice.config.token;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.utils.TokenUtils;
import com.capacidad.utils.exception.ExpiredTokenException;
import com.capacidad.utils.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;

@Slf4j
@Component
public class TokenVerifierImpl implements TokenVerifier {

    private final JwkProvider jwkProvider;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public TokenVerifierImpl(ApplicationProperties applicationProperties) {
        this.jwkProvider = new UrlJwkProvider(Utils.getFileURL("keys.jwks.json"));
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void verify(String jwt) {
        try {
            String keyId = TokenUtils.getJwtKid(jwt)
                    .orElseThrow(() -> new InvalidTokenException("tokenVerifier.invalidHeaders"));
            Jwk jwk = this.jwkProvider.get(keyId);
            RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(Utils.buildJwtIssuer(
                            applicationProperties.getJwtIssuer(), applicationProperties.getActiveProfile()
                            )
                    )
                    .build();
            verifier.verify(jwt);
        } catch (JWTVerificationException | JwkException e) {
            log.error("Invalid verification for token: {} - ERROR: {}", jwt, e.getMessage());
            if (StringUtils.contains(e.getMessage(), "expired"))
                throw new ExpiredTokenException("tokenVerifier.expiredToken");
            throw new InvalidTokenException("tokenVerifier.generalError");
        }
    }

    @Override
    public void validate(String jwt) {
        TokenUtils.validate(jwt, applicationProperties.getApiIdentifier(), applicationProperties.getActiveProfile());
    }

}
