package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.exception.UnspecifiedTenantException;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TenantServiceImplTest {

    @Mock
    private RegisteredClient clientDetails;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @BeforeEach
    public void setup() {
        TenantContext.clearContext(); // Limpiar contexto antes de cada test
    }

    @Test
    public void testValidateTenantFailsWhenMultitenantAndNoHeaderSpecified() {
        ApplicationUser user = new ApplicationUser();

        Tenant tenant1 = new Tenant();
        tenant1.setName("tenant1");
        tenant1.setTenantId(UUID.randomUUID());

        Tenant tenant2 = new Tenant();
        tenant2.setName("tenant2");
        tenant2.setTenantId(UUID.randomUUID());

        ApplicationUserContext context1 = new ApplicationUserContext();
        context1.setTenant(tenant1);

        ApplicationUserContext context2 = new ApplicationUserContext();
        context2.setTenant(tenant2);

        user.getContextSet().add(context1);
        user.getContextSet().add(context2);

        UnspecifiedTenantException exception = (UnspecifiedTenantException) catchThrowable(() ->
                tenantService.validateTenant(user, clientDetails, ""));

        assertThat(exception).hasMessageContaining(Utils.buildTenantResponse(tenant1));
        assertThat(exception).hasMessageContaining(Utils.buildTenantResponse(tenant2));
    }

    @Test
    public void testValidateTenantFailsWhenMultitenantAndInvalidHeaderSpecified() {
        ApplicationUser user = new ApplicationUser();

        Tenant tenant1 = new Tenant();
        tenant1.setName("tenant1");
        tenant1.setTenantId(UUID.randomUUID());

        Tenant tenant2 = new Tenant();
        tenant2.setName("tenant2");
        tenant2.setTenantId(UUID.randomUUID());

        ApplicationUserContext context1 = new ApplicationUserContext();
        context1.setTenant(tenant1);

        ApplicationUserContext context2 = new ApplicationUserContext();
        context2.setTenant(tenant2);

        user.getContextSet().add(context1);
        user.getContextSet().add(context2);

        InsufficientAuthenticationException exception = (InsufficientAuthenticationException) catchThrowable(() ->
                tenantService.validateTenant(user, clientDetails, "invalidTenantId"));

        assertThat(exception).hasMessage("tenant.invalidTenantUser");
    }

    @Test
    public void testValidateTenantFailsWhenMultitenantAndInvalidClientTenant() {
        ApplicationUser user = new ApplicationUser();

        Tenant tenant1 = new Tenant();
        tenant1.setName("tenant1");
        tenant1.setTenantId(UUID.randomUUID());

        Tenant tenant2 = new Tenant();
        tenant2.setName("tenant2");
        tenant2.setTenantId(UUID.randomUUID());

        ApplicationUserContext context1 = new ApplicationUserContext();
        context1.setTenant(tenant1);

        ApplicationUserContext context2 = new ApplicationUserContext();
        context2.setTenant(tenant2);

        user.getContextSet().add(context1);
        user.getContextSet().add(context2);

        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put(TENANT_ID, "invalidTenant");

        when(clientDetails.getClientSettings().getSettings()).thenReturn(additionalInfo);

        InsufficientAuthenticationException exception = (InsufficientAuthenticationException) catchThrowable(() ->
                tenantService.validateTenant(user, clientDetails, tenant1.getTenantId().toString()));

        assertThat(exception).hasMessage("tenant.clientCannotOperate");
    }

    @Test
    public void testValidateTenantReturnsContextWhenMultitenantAndValidClientTenant() {
        ApplicationUser user = new ApplicationUser();

        Tenant tenant1 = new Tenant();
        tenant1.setName("tenant1");
        tenant1.setTenantId(UUID.randomUUID());

        Tenant tenant2 = new Tenant();
        tenant2.setName("tenant2");
        tenant2.setTenantId(UUID.randomUUID());

        ApplicationUserContext context1 = new ApplicationUserContext();
        context1.setTenant(tenant1);

        ApplicationUserContext context2 = new ApplicationUserContext();
        context2.setTenant(tenant2);

        user.getContextSet().add(context1);
        user.getContextSet().add(context2);

        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put(TENANT_ID, tenant1.getTenantId().toString());

        when(clientDetails.getClientSettings().getSettings()).thenReturn(additionalInfo);

        ApplicationUserContext result = tenantService.validateTenant(user, clientDetails, tenant1.getTenantId().toString());

        assertThat(result).isEqualTo(context1);
        assertThat(TenantContext.getTenant()).isEqualTo(tenant1);
    }

    @Test
    public void testValidateTenantReturnsContextWhenSingleTenantAndNotSpecifiedClientTenant() {
        ApplicationUser user = new ApplicationUser();

        Tenant tenant2 = new Tenant();
        tenant2.setName("tenant1");
        tenant2.setTenantId(UUID.randomUUID());

        ApplicationUserContext context2 = new ApplicationUserContext();
        context2.setTenant(tenant2);

        user.getContextSet().add(context2);

        ApplicationUserContext result = tenantService.validateTenant(user, clientDetails, "");

        assertThat(result).isEqualTo(context2);
        assertThat(TenantContext.getTenant()).isEqualTo(tenant2);
    }
}
