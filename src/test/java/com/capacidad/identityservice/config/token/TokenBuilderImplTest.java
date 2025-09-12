package com.capacidad.identityservice.config.token;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.PermissionGroupService;
import com.capacidad.identityservice.service.TenantService;
import com.capacidad.utils.TokenUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import jakarta.persistence.EntityManager;
import java.util.*;

import static com.capacidad.identityservice.misc.constant.SecurityConstants.*;
import static com.capacidad.utils.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenBuilderImplTest {

    @Mock
    private Utils utils;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ApplicationUserContextService userContextService;
    @Mock
    private ClientDetailsService clientDetailsService;
    @Mock
    private ClientDetails clientDetails;
    @Mock
    private OAuth2Request oAuth2Request;
    @Mock
    private OAuth2Authentication oAuth2Authentication;
    @Mock
    private TenantService tenantService;
    @Mock
    private PermissionGroupService permissionGroupService;
    @Mock
    private ApplicationProperties applicationProperties;
    private TokenBuilderImpl tokenBuilder;

    @Before
    public void init() {
        Utils realUtils = new Utils(new ObjectMapper(), entityManager);
        Optional<JsonNode> jwksNode = realUtils.readResourceToJsonNode("keys.jwks.json");
        when(utils.readResourceToJsonNode("keys.jwks.json")).thenReturn(jwksNode);
        tokenBuilder = new TokenBuilderImpl(utils, userContextService, clientDetailsService, applicationProperties, tenantService, permissionGroupService);
    }

    @Test
    public void testBuildAccessTokenReturnsValidTokenWithResourceFromAuthenticationWithNoExpiration() {
        CustomUserDetails customUserDetails = new CustomUserDetails("user", "pass", Collections.emptyList());

        ApplicationUser user = new ApplicationUser();
        user.setUsername("username_test");
        user.setGroup(Group.DEV);
        user.setSub(UUID.randomUUID());
        user.setResourceId(UUID.randomUUID());

        customUserDetails.setApplicationUser(user);

        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setId(2L);
        Role role = new Role();
        role.setId(1L);
        role.setName(BENEFICIARY);

        userContext.setRole(role);

        Tenant tenant = new Tenant();
        tenant.setName("test");
        tenant.setTenantId(UUID.randomUUID());

        userContext.setTenant(tenant);

        Set<PermissionGroup> permissionGroups = new HashSet<>();
        PermissionGroup permissionGroup = new PermissionGroup();
        permissionGroup.setResourceOperations(new HashMap<>() {{
            put("resource_1", Arrays.asList(Operation.READ, Operation.CREATE));
            put("resource_2", Arrays.asList(Operation.UPDATE, Operation.DELETE));
        }});
        permissionGroups.add(permissionGroup);

        user.getContextSet().add(userContext);

        Set<String> resources = new HashSet<>();
        resources.add("apiA");
        resources.add("apiB");

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(TENANT_ID_HEADER, tenant.getName());
        TenantContext.setTenant(tenant);

        String testClientId = "test_client_id";

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getClientId()).thenReturn(testClientId);
        when(oAuth2Request.getRequestParameters()).thenReturn(requestParams);
        when(clientDetailsService.loadClientByClientId(testClientId)).thenReturn(clientDetails);
        when(clientDetails.getAccessTokenValiditySeconds()).thenReturn(0);
        when(clientDetails.getResourceIds()).thenReturn(resources);
        when(clientDetails.getClientId()).thenReturn(testClientId);
        when(oAuth2Authentication.isClientOnly()).thenReturn(false);
        when(oAuth2Authentication.getPrincipal()).thenReturn(customUserDetails);
        when(tenantService.validateTenant(user, clientDetails, tenant.getName())).thenReturn(userContext);
        when(applicationProperties.getActiveProfile()).thenReturn("dev");
        doNothing().when(userContextService).checkOperationalState(customUserDetails.getApplicationUser(), userContext, clientDetails);
        when(permissionGroupService.findAllBasedOnContextAttributes(userContext)).thenReturn(permissionGroups);

        OAuth2AccessToken oAuth2AccessToken = tokenBuilder.buildAccessToken(oAuth2Authentication);

        Map<String, Object> parsedToken = TokenUtils.parseJwt(oAuth2AccessToken.getValue());

        assertThat(parsedToken).containsEntry(JWT_CLAIM_USERNAME, user.getUsername());
        assertThat(parsedToken).containsEntry(JWT_CLAIM_TENANT, tenant.getTenantId().toString());
        assertThat(parsedToken).containsEntry(JWT_CLAIM_ROLE, StringUtils.join(ROLE_PREFIX, role.getName().toUpperCase()));
        assertThat((List<String>) parsedToken.get(JWT_CLAIM_SCOPE)).contains("read:resource_1", "create:resource_1",
                "update:resource_2", "delete:resource_2");
        String aud = TokenUtils.getJwtAudience(oAuth2AccessToken.getValue()).orElse("");
        assertThat(aud).contains("apiA").contains("apiB");
        String clientId = TokenUtils.getClaim(oAuth2AccessToken.getValue(), JWT_CLAIM_CLIENT_ID).orElse(null);
        assertThat(clientId).isEqualTo("test_client_id");
        String resourceId = TokenUtils.getJwtResourceIdClaim(oAuth2AccessToken.getValue()).orElse(null);
        assertThat(resourceId).isEqualTo(user.getResourceId().toString());
        String sub = TokenUtils.getJwtSubject(oAuth2AccessToken.getValue()).orElse(null);
        assertThat(sub).isEqualTo(user.getSub().toString());
        assertThat(oAuth2AccessToken.getExpiresIn()).isZero();
    }

    @Test
    public void testBuildAccessTokenReturnsValidTokenWithoutResourceAndRoleScopeFromAuthenticationWithExpiration() {
        CustomUserDetails customUserDetails = new CustomUserDetails("user", "pass", Collections.emptyList());

        ApplicationUser user = new ApplicationUser();
        user.setUsername("username_test");
        user.setGroup(Group.DEV);
        user.setSub(UUID.randomUUID());
        user.setResourceId(null);

        customUserDetails.setApplicationUser(user);

        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setId(1L);
        Role role = new Role();
        role.setId(1L);
        role.setName(ADMIN);

        userContext.setRole(role);

        Tenant tenant = new Tenant();
        tenant.setName("test");
        tenant.setTenantId(UUID.randomUUID());

        userContext.setTenant(tenant);

        Set<PermissionGroup> permissionGroups = new HashSet<>();
        PermissionGroup permissionGroup = new PermissionGroup();
        permissionGroup.setResourceOperations(new HashMap<>() {{
            put("resource_1", Arrays.asList(Operation.READ, Operation.CREATE));
            put("resource_2", Arrays.asList(Operation.UPDATE, Operation.DELETE));
        }});
        permissionGroups.add(permissionGroup);

        user.getContextSet().add(userContext);

        Set<String> resources = new HashSet<>();
        resources.add("apiA");
        resources.add("apiB");

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(TENANT_ID_HEADER, tenant.getName());
        TenantContext.setTenant(tenant);

        String testClientId = "test_client_id";

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getClientId()).thenReturn(testClientId);
        when(oAuth2Request.getRequestParameters()).thenReturn(requestParams);
        when(clientDetailsService.loadClientByClientId(testClientId)).thenReturn(clientDetails);
        when(clientDetails.getAccessTokenValiditySeconds()).thenReturn(3600);
        when(clientDetails.getResourceIds()).thenReturn(resources);
        when(clientDetails.getClientId()).thenReturn(testClientId);
        when(oAuth2Authentication.isClientOnly()).thenReturn(false);
        when(oAuth2Authentication.getPrincipal()).thenReturn(customUserDetails);
        when(tenantService.validateTenant(user, clientDetails, tenant.getName())).thenReturn(userContext);
        when(applicationProperties.getActiveProfile()).thenReturn("dev");
        doNothing().when(userContextService).checkOperationalState(customUserDetails.getApplicationUser(), userContext, clientDetails);
        when(permissionGroupService.findAllBasedOnContextAttributes(userContext)).thenReturn(permissionGroups);

        OAuth2AccessToken oAuth2AccessToken = tokenBuilder.buildAccessToken(oAuth2Authentication);

        Map<String, Object> parsedToken = TokenUtils.parseJwt(oAuth2AccessToken.getValue());

        assertThat(parsedToken).containsEntry(JWT_CLAIM_USERNAME, user.getUsername());
        assertThat(parsedToken).containsEntry(JWT_CLAIM_TENANT, tenant.getTenantId().toString());
        assertThat(parsedToken).containsEntry(JWT_CLAIM_ROLE, StringUtils.join(ROLE_PREFIX, role.getName().toUpperCase()));
        assertThat((List<String>) parsedToken.get(JWT_CLAIM_SCOPE)).contains("read:resource_1", "create:resource_1",
                "update:resource_2", "delete:resource_2");
        String aud = TokenUtils.getJwtAudience(oAuth2AccessToken.getValue()).orElse(null);
        assertThat(aud).contains("apiA").contains("apiB");
        String clientId = TokenUtils.getClaim(oAuth2AccessToken.getValue(), JWT_CLAIM_CLIENT_ID).orElse(null);
        assertThat(clientId).isEqualTo("test_client_id");
        String resourceId = TokenUtils.getJwtResourceIdClaim(oAuth2AccessToken.getValue()).orElse(null);
        assertThat(resourceId).isNull();
        String sub = TokenUtils.getJwtSubject(oAuth2AccessToken.getValue()).orElse(null);
        assertThat(sub).isEqualTo(user.getSub().toString());
        assertThat(oAuth2AccessToken.getExpiresIn()).isPositive();
    }

    @Test
    public void testBuildAccessTokenReturnsValidTokenClientCredentialsFromAuthentication() {
        Set<String> resources = new HashSet<>();
        resources.add("apiA");
        resources.add("apiB");

        Set<String> scope = new HashSet<>();
        scope.add("create:users");
        scope.add("token:beneficiaries");

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        tenant.setName("test_tenant");

        Map<String, Object> clientAdditionalInformation = new HashMap<>();
        clientAdditionalInformation.put(JWT_CLAIM_GROUP, Group.DEV.toString().toLowerCase());
        clientAdditionalInformation.put(TENANT_ID, tenant.getTenantId().toString());
        clientAdditionalInformation.put(TENANT_NAME, tenant.getName());

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getClientId()).thenReturn("test_client_id");
        when(clientDetailsService.loadClientByClientId("test_client_id")).thenReturn(clientDetails);
        when(clientDetails.getAccessTokenValiditySeconds()).thenReturn(3600);
        when(clientDetails.getResourceIds()).thenReturn(resources);
        when(clientDetails.getClientId()).thenReturn("test_client_id");
        when(clientDetails.getScope()).thenReturn(scope);
        when(clientDetails.getAdditionalInformation()).thenReturn(clientAdditionalInformation);
        when(applicationProperties.getActiveProfile()).thenReturn("dev");
        when(oAuth2Authentication.isClientOnly()).thenReturn(true);

        OAuth2AccessToken oAuth2AccessToken = tokenBuilder.buildAccessToken(oAuth2Authentication);

        String sub = TokenUtils.getJwtSubject(oAuth2AccessToken.getValue()).orElse(null);
        String tenantId = TokenUtils.getJwtTenantClaim(oAuth2AccessToken.getValue()).orElse(null);
        String group = TokenUtils.getJwtGroupClaim(oAuth2AccessToken.getValue()).orElse(null);
        List<String> scopeList = TokenUtils.getJwtScope(oAuth2AccessToken.getValue());
        String role = TokenUtils.getJwtRoleClaim(oAuth2AccessToken.getValue()).orElse(null);

        assertThat(sub).isEqualTo("test_client_id");
        assertThat(tenantId.split(":")[1]).isEqualTo(tenant.getTenantId().toString());
        assertThat(scopeList.size()).isEqualTo(2);
        assertThat(scopeList).contains("create:users", "token:beneficiaries");
        assertThat(role).contains(CLIENT.toLowerCase());
        assertThat(group).contains(Group.DEV.toString().toLowerCase());
    }

}
