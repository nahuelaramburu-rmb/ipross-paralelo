package com.capacidad.validationapi;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.capacidad.validationapi.misc.ApplicationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

import static com.capacidad.utils.Constants.*;
import static com.capacidad.validationapi.config.filter.JWTAuthenticationFilter.JWT_CLAIM_CLIENT_ID;

@Component
public class AuthUtils {

    private final ApplicationProperties applicationProperties;
    private final String tenantId = UUID.randomUUID().toString();

    @Autowired
    public AuthUtils(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public String obtainAccessToken(String role, String scope, UUID resourceId) throws Exception {
        Algorithm algorithm = getSigningAlthorithm();
        return JWT.create()
                .withIssuer("https://vem-test.capacidad.com.ar")
                .withClaim(JWT_CLAIM_USERNAME, "test_user")
                .withClaim(JWT_CLAIM_SCOPE, scope)
                .withClaim(JWT_CLAIM_ROLE, role)
                .withClaim(JWT_CLAIM_TENANT, StringUtils.join("test_tenant", ":", tenantId))
                .withClaim(JWT_CLAIM_GROUP, applicationProperties.getActiveProfile())
                .withClaim(JWT_CLAIM_RESOURCE_ID, resourceId != null ? resourceId.toString() : "")
                .withClaim(JWT_CLAIM_CLIENT_ID, "test_client_id")
                .withSubject(UUID.randomUUID().toString())
                .withKeyId("test_kid")
                .withAudience(applicationProperties.getApiIdentifier())
                .withExpiresAt(new Date(System.currentTimeMillis() + (long) 3600 * 1000L))
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    private Algorithm getSigningAlthorithm() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return Algorithm.RSA256(publicKey, privateKey);
    }

}
