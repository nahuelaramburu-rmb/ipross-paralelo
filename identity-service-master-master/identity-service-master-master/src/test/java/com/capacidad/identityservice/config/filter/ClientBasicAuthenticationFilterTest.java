package com.capacidad.identityservice.config.filter;

import com.capacidad.identityservice.config.security.ClientAuthenticationToken;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.TenantService;
import com.capacidad.utils.exception.ApiError;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.ENDPOINT_LOGIN;
import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.ENDPOINT_OAUTH;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientBasicAuthenticationFilterTest {

    @Mock
    private TenantService tenantService;
    @Mock
    private ClientDetailsService clientDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private GlobalExceptionHandler exceptionHandler;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ClientDetails clientDetails;
    @Mock
    private PrintWriter responseWriter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain chain;
    @InjectMocks
    private ClientBasicAuthenticationFilter clientBasicAuthenticationFilter;

    @Test
    public void testFilterBypassWhenEndpointIsLoginAndCredentialsAreValid() throws IOException, ServletException {
        String basicAuth = "Basic dGVzdDp0ZXN0";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(basicAuth);
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_LOGIN));
        when(request.getContextPath()).thenReturn("/v1");
        clientBasicAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(clientDetailsService, never()).loadClientByClientId(anyString());
    }

    @Test
    public void testFilterBypassWhenEndpointIsOauthAndCredentialsAreValid() throws IOException, ServletException {
        String basicAuth = "Basic dGVzdDp0ZXN0";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(basicAuth);
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_OAUTH, "/token"));
        when(request.getContextPath()).thenReturn("/v1");
        clientBasicAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(clientDetailsService, never()).loadClientByClientId(anyString());
    }

    @Test
    public void testFilterBypassWhenBasicAuthCredentialsAreNull() throws IOException, ServletException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        clientBasicAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(clientDetailsService, never()).loadClientByClientId(anyString());
    }

    @Test
    public void testDoFilterInternalThrowsUsernameNotFoundExceptionWhenNoSuchClient() throws IOException, ServletException {
        //Base64 test:test;
        String basicAuth = "Basic dGVzdDp0ZXN0";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(basicAuth);

        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED.value(), "", "Authorization", "/path", "");
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(apiError.getJsonObject(), HttpStatus.UNAUTHORIZED);

        when(clientDetailsService.loadClientByClientId("test")).thenThrow(new NoSuchClientException("no such client"));
        when(exceptionHandler.handleAuthenticationException(any(HttpServletRequest.class), any(AuthenticationException.class))).thenReturn(responseEntity);
        when(response.getWriter()).thenReturn(responseWriter);

        clientBasicAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(exceptionHandler, times(1)).handleAuthenticationException(any(HttpServletRequest.class), any(AuthenticationException.class));
        verify(response, times(1)).setStatus(apiError.getStatus());
        verify(responseWriter, times(1)).write(apiError.getJsonObject());
    }

    @Test
    public void testDoFilterInternalThrowsObjectNotFoundExceptionWhenNoSuchTenant() throws IOException, ServletException, ObjectNotFoundException {
        //Base64 test:test;
        String basicAuth = "Basic dGVzdDp0ZXN0";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(basicAuth);

        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), "", "ObjectNotFound", "/path", "");
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(apiError.getJsonObject(), HttpStatus.NOT_FOUND);

        UUID tenantId = UUID.randomUUID();
        Map<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put(TENANT_ID, tenantId.toString());

        ObjectNotFoundException objectNotFoundException = new ObjectNotFoundException("tenant not found");

        when(clientDetailsService.loadClientByClientId("test")).thenReturn(clientDetails);
        when(clientDetails.getAdditionalInformation()).thenReturn(additionalInformation);
        when(tenantService.findTenantById(tenantId)).thenThrow(objectNotFoundException);
        when(exceptionHandler.handleObjectNotFoundException(request, objectNotFoundException)).thenReturn(responseEntity);
        when(response.getWriter()).thenReturn(responseWriter);

        clientBasicAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(exceptionHandler, times(1)).handleObjectNotFoundException(request, objectNotFoundException);
        verify(response, times(1)).setStatus(apiError.getStatus());
        verify(responseWriter, times(1)).write(apiError.getJsonObject());
    }

    @Test
    public void testDoFilterInternalThrowsBadCredentialsExceptionWhenInvalidClientCredentials() throws IOException, ServletException, ObjectNotFoundException {
        //Base64 test:test;
        String basicAuth = "Basic dGVzdDp0ZXN0";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(basicAuth);

        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED.value(), "", "Authentication", "/path", "");
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(apiError.getJsonObject(), HttpStatus.UNAUTHORIZED);

        UUID tenantId = UUID.randomUUID();
        Map<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put(TENANT_ID, tenantId.toString());

        when(clientDetailsService.loadClientByClientId("test")).thenReturn(clientDetails);
        when(clientDetails.getAdditionalInformation()).thenReturn(additionalInformation);
        when(tenantService.findTenantById(tenantId)).thenReturn(new Tenant());
        when(exceptionHandler.handleAuthenticationException(any(HttpServletRequest.class), any(AuthenticationException.class))).thenReturn(responseEntity);
        when(response.getWriter()).thenReturn(responseWriter);

        clientBasicAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(exceptionHandler, times(1)).handleAuthenticationException(any(HttpServletRequest.class), any(AuthenticationException.class));
        verify(response, times(1)).setStatus(apiError.getStatus());
        verify(responseWriter, times(1)).write(apiError.getJsonObject());
    }

    @Test
    public void testDoFilterInternalReturnsValidAuthentication() throws IOException, ServletException, ObjectNotFoundException {
        //Base64 test:test;
        String basicAuth = "Basic dGVzdDp0ZXN0";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(basicAuth);

        SecurityContextHolder.setContext(securityContext);

        UUID tenantId = UUID.randomUUID();
        Map<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put(TENANT_ID, tenantId.toString());

        Set<String> scope = new HashSet<>();
        scope.add("read:beneficiaries");
        scope.add("write:beneficiaries");

        when(clientDetailsService.loadClientByClientId("test")).thenReturn(clientDetails);
        when(clientDetails.getClientSecret()).thenReturn("test");
        when(clientDetails.getAdditionalInformation()).thenReturn(additionalInformation);
        when(clientDetails.getScope()).thenReturn(scope);
        when(clientDetails.getClientId()).thenReturn("test");
        when(tenantService.findTenantById(tenantId)).thenReturn(new Tenant());
        when(passwordEncoder.matches("test", "test")).thenReturn(true);

        doNothing().when(chain).doFilter(request, response);

        clientBasicAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(securityContext, times(1)).setAuthentication(any(ClientAuthenticationToken.class));
    }

}
