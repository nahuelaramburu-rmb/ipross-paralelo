package com.capacidad.validationapi.misc;

import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.config.security.WebSocketAuthentication;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.projection.InsurancePlanProjection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    @Mock
    private SecurityContext securityContext;

    @Test
    public void testGetAuthenticatedAuthorityResourceIdReturnsEmptyWhenAuthenticationInvalidInstance() {
        SecurityContextHolder.setContext(securityContext);

        WebSocketAuthentication authentication = new WebSocketAuthentication("", Collections.emptyList(), UUID.randomUUID(), UUID.randomUUID());

        when(securityContext.getAuthentication()).thenReturn(authentication);

        Optional<UUID> result = SecurityUtils.getAuthenticatedAuthorityResourceId();

        assertThat(result).isEmpty();
    }

    @Test
    public void testGetAuthenticatedAuthorityResourceIdReturnsResourceIdWhenAuthenticationValidInstance() {
        SecurityContextHolder.setContext(securityContext);

        UUID resourceId = UUID.randomUUID();

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), resourceId, "", UUID.randomUUID(), "");

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Optional<UUID> result = SecurityUtils.getAuthenticatedAuthorityResourceId();

        assertThat(result).contains(resourceId);
    }

    @Test
    public void testGetAuthenticatedAuthoritySubReturnsEmptyWhenAuthenticationInvalidInstance() {
        SecurityContextHolder.setContext(securityContext);

        WebSocketAuthentication authentication = new WebSocketAuthentication("", Collections.emptyList(), UUID.randomUUID(), UUID.randomUUID());

        when(securityContext.getAuthentication()).thenReturn(authentication);

        Optional<UUID> result = SecurityUtils.getAuthenticatedAuthoritySub();

        assertThat(result).isEmpty();
    }

    @Test
    public void testGetAuthenticatedAuthoritySubReturnsSubWhenAuthenticationValidInstance() {
        SecurityContextHolder.setContext(securityContext);

        UUID sub = UUID.randomUUID();

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), UUID.randomUUID(), "", sub, "");

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Optional<UUID> result = SecurityUtils.getAuthenticatedAuthoritySub();

        assertThat(result).contains(sub);
    }

    @Test
    public void testGetAuthenticatedAuthorityClientIdReturnsEmptyWhenAuthenticationInvalidInstance() {
        SecurityContextHolder.setContext(securityContext);

        WebSocketAuthentication authentication = new WebSocketAuthentication("", Collections.emptyList(), UUID.randomUUID(), UUID.randomUUID());

        when(securityContext.getAuthentication()).thenReturn(authentication);

        Optional<String> result = SecurityUtils.getAuthenticatedAuthorityClientId();

        assertThat(result).isEmpty();
    }

    @Test
    public void testGetAuthenticatedAuthorityClientIdReturnsClientIdWhenAuthenticationValidInstance() {
        SecurityContextHolder.setContext(securityContext);

        String clientId = "testClientId";

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken(clientId, Collections.emptyList(), UUID.randomUUID(), clientId, UUID.randomUUID(), "");

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Optional<String> result = SecurityUtils.getAuthenticatedAuthorityClientId();

        assertThat(result).contains(clientId);
    }

    @Test
    public void testProjectionToResourceMappingReturnsObjectWhenResourceImplemented() {
        InsurancePlanProjection projection = mock(InsurancePlanProjection.class);

        EntityModel<InsurancePlanProjection> result = Utils.projectionToResourceMapping(InsurancePlan.class, projection);

        assertThat(result).isNotNull();
    }

    @Test
    public void testProjectionToResourceMappingReturnsNullWhenResourceImplementedProjectionInvalidNullConstructor() {
        Beneficiary beneficiary = new Beneficiary();
        InsurancePlanProjection insurancePlanProjection = projectionFactory.createProjection(InsurancePlanProjection.class, beneficiary);

        EntityModel<InsurancePlanProjection> result = Utils.projectionToResourceMapping(beneficiary.getClass(), insurancePlanProjection);

        assertThat(result).isNull();
    }

    @Test
    public void testProjectionToResourceMappingReturnsNullWhenResourceImplementedProjectionValidButNullInstanceNull() {
        Beneficiary beneficiary = new Beneficiary();
        BeneficiaryProjection beneficiaryProjection = projectionFactory.createProjection(BeneficiaryProjection.class, beneficiary);

        EntityModel<InsurancePlanProjection> result = Utils.projectionToResourceMapping(beneficiary.getClass(), beneficiaryProjection);

        assertThat(result).isNull();
    }

}
