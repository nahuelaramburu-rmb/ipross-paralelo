package com.capacidad.identityservice.config.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.exception.TokenSigningException;
import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.model.PermissionGroup;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.PermissionGroupService;
import com.capacidad.identityservice.service.TenantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COLON;
import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.*;
import static com.capacidad.utils.Constants.*;

@Slf4j
@Component
public class TokenBuilderImpl implements TokenBuilder {

    private JsonNode keys;
    private final ApplicationUserContextService userContextService;
    private final ClientDetailsService clientDetailsService;
    private final ApplicationProperties applicationProperties;
    private final TenantService tenantService;
    private final Map<String, Algorithm> algorithmMap = new HashMap<>();
    private final Map<Integer, RSAKey> rsaKeyMap = new HashMap<>();
    private final PermissionGroupService permissionGroupService;

    @Autowired
    public TokenBuilderImpl(Utils utils,
                            ApplicationUserContextService userContextService,
                            ClientDetailsService clientDetailsService,
                            ApplicationProperties applicationProperties,
                            TenantService tenantService,
                            PermissionGroupService permissionGroupService) {
        Optional<JsonNode> jwks = utils.readResourceToJsonNode("keys.jwks.json");
        jwks.ifPresent(jsonNode -> this.keys = jsonNode.get("keys"));
        this.userContextService = userContextService;
        this.clientDetailsService = clientDetailsService;
        this.applicationProperties = applicationProperties;
        this.tenantService = tenantService;
        this.permissionGroupService = permissionGroupService;
    }

    @Override
    public OAuth2AccessToken buildAccessToken(OAuth2Authentication oAuth2Authentication) {
        var oAuth2Request = oAuth2Authentication.getOAuth2Request();
        var clientDetails = clientDetailsService.loadClientByClientId(oAuth2Request.getClientId());
        int validitySeconds = clientDetails.getAccessTokenValiditySeconds();
        Date expirationTimestamp = getExpirationTimestamp(validitySeconds);
        var token = new DefaultOAuth2AccessToken(buildAccessToken(oAuth2Authentication, clientDetails, expirationTimestamp));
        if (validitySeconds > 0)
            token.setExpiration(expirationTimestamp);
        token.setScope(clientDetails.getScope());
        return token;
    }

    private String buildAccessToken(OAuth2Authentication oAuth2Authentication, ClientDetails clientDetails, Date expirationTimestamp) {
        var signingRSAKey = getSigningKey();
        if (oAuth2Authentication.isClientOnly())
            return buildClientAccessToken(signingRSAKey, clientDetails, expirationTimestamp);
        var customUserDetails = (CustomUserDetails) oAuth2Authentication.getPrincipal();
        var user = customUserDetails.getApplicationUser();
        ApplicationUserContext context = tenantService.validateTenant(user, clientDetails,
                oAuth2Authentication.getOAuth2Request().getRequestParameters().get(TENANT_ID_HEADER));

        return buildUserAccessToken(signingRSAKey, user, context, clientDetails, expirationTimestamp);
    }

    private String buildClientAccessToken(RSAKey signingRSAKey, ClientDetails clientDetails, Date expirationTimestamp) {
        var scopes = String.join(COMA, clientDetails.getScope());
        return JWT.create()
                .withIssuer(Utils.buildJwtIssuer(
                        applicationProperties.getJwtIssuer(), applicationProperties.getActiveProfile()
                        )
                )
                .withSubject(clientDetails.getClientId())
                .withClaim(JWT_CLAIM_USERNAME, CLIENT.toLowerCase())
                .withClaim(JWT_CLAIM_CLIENT_ID, clientDetails.getClientId())
                .withClaim(JWT_CLAIM_SCOPE, scopes)
                .withClaim(JWT_CLAIM_TENANT, Utils.buildTenantResponse(clientDetails))
                .withClaim(JWT_CLAIM_ROLE, CLIENT.toLowerCase())
                .withClaim(JWT_CLAIM_GROUP, applicationProperties.getActiveProfile().toLowerCase())
                .withKeyId(signingRSAKey.getKeyID())
                .withAudience(String.join(COMA, clientDetails.getResourceIds()))
                .withExpiresAt(expirationTimestamp)
                .withIssuedAt(new Date())
                .sign(algorithmMap.get(signingRSAKey.getKeyID()));
    }

    private String buildUserAccessToken(RSAKey signingRSAKey, ApplicationUser user, ApplicationUserContext context, ClientDetails clientDetails, Date expirationTimestamp) {
        var aud = String.join(COMA, clientDetails.getResourceIds());
        var currentTenant = TenantContext.getTenant();
        userContextService.checkOperationalState(user, context, clientDetails);
        Set<PermissionGroup> permissionGroups = permissionGroupService.findAllBasedOnContextAttributes(context);
        String newScope = permissionGroups.stream().flatMap(p -> p.getResourceOperations().entrySet().stream().map(e -> e.getValue().stream().map(operation -> StringUtils.join(StringUtils.lowerCase(operation.toString()), COLON, e.getKey()))
                .collect(Collectors.joining(COMA))))
                .collect(Collectors.joining(COMA));
        Long suggestionId = context.getPermissionSuggestion() != null ? context.getPermissionSuggestion().getId() : null;
        String resourceId = user.getResourceId() != null ? user.getResourceId().toString() : "";
        return JWT.create()
                .withIssuer(Utils.buildJwtIssuer(
                        applicationProperties.getJwtIssuer(), applicationProperties.getActiveProfile()
                        )
                )
                .withClaim(JWT_CLAIM_USERNAME, user.getUsername())
                .withClaim(JWT_CLAIM_SCOPE, newScope)
                .withClaim(JWT_CLAIM_ROLE, context.getRole().getName().toLowerCase())
                .withClaim(JWT_CLAIM_TENANT, Utils.buildTenantResponse(currentTenant))
                .withClaim(JWT_CLAIM_GROUP, applicationProperties.getActiveProfile().toLowerCase())
                .withClaim(JWT_CLAIM_EMAIL, user.getEmail())
                .withClaim(JWT_CLAIM_RESOURCE_ID, resourceId)
                .withClaim(JWT_CLAIM_EMAIL_VERIFIED, user.getEmailVerified())
                .withClaim(JWT_CLAIM_CLIENT_ID, clientDetails.getClientId())
                .withClaim(JWT_CLAIM_SUBROLE, suggestionId)
                .withSubject(user.getSub().toString())
                .withKeyId(signingRSAKey.getKeyID())
                .withAudience(aud)
                .withExpiresAt(expirationTimestamp)
                .withIssuedAt(new Date())
                .sign(algorithmMap.get(signingRSAKey.getKeyID()));
    }

    private Algorithm getSigningAlgorithm(RSAKey signingRSAKey) {
        try {
            return Algorithm.RSA256(signingRSAKey.toRSAPublicKey(), signingRSAKey.toRSAPrivateKey());
        } catch (JOSEException e) {
            log.error("Error converting RSAKeys to Public/Private Keys: {}", e.getMessage());
            throw new TokenSigningException("tokenBuilder.signingAlgorithm");
        }
    }

    private RSAKey getSigningKey() {
        var randomKey = Math.toIntExact(Math.round(Math.random()));
        if (rsaKeyMap.containsKey(randomKey))
            return rsaKeyMap.get(randomKey);
        var selectedKey = keys.get(randomKey).toString();
        try {
            var rsaKey = RSAKey.parse(selectedKey);
            rsaKeyMap.put(randomKey, rsaKey);
            if (!algorithmMap.containsKey(rsaKey.getKeyID()))
                algorithmMap.put(rsaKey.getKeyID(), getSigningAlgorithm(rsaKey));
            return rsaKey;
        } catch (ParseException e) {
            log.error("Error parsing Jwks to RSAKey: {}", e.getMessage());
            throw new TokenSigningException("tokenBuilder.signingKey");
        }
    }

    private Date getExpirationTimestamp(int validitySeconds) {
        return new Date(System.currentTimeMillis() + (long) validitySeconds * 1000L);
    }

}
