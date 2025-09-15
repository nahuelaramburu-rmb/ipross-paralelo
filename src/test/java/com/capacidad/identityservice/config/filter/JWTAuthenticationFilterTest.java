package com.capacidad.identityservice.config.filter;

import com.capacidad.identityservice.config.token.TokenVerifier;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.TenantService;
import com.capacidad.utils.exception.ApiError;
import com.capacidad.utils.exception.InvalidTokenException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.*;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID_HEADER;
import static com.capacidad.utils.Constants.AUTHORIZATION_BEARER_PREFIX;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
public class JWTAuthenticationFilterTest {

    @Mock
    private TenantService tenantService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private GlobalExceptionHandler exceptionHandler;
    @Mock
    private HttpServletResponse response;
    @Mock
    private PrintWriter responseWriter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain chain;
    @Mock
    private TokenVerifier tokenVerifier;
    @InjectMocks
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testFilterBypassWhenEndpointIsLoginAndBearerTokenIsValid() throws IOException, ServletException {
        String jwt = "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhODQxNTZhOC0zNTUwLTRmN2UtOTJmNS1kODZiZDM4ZGI4NDciLCJyb2xlIjoiYWRtaW4iLC" +
                "JlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3YxIiwiY2xpZW50X2lkIjoicHVibGljLWNsaWVudCIsImF1ZCI6InFrVWJJ" +
                "QUYyVTRITlVWVTdzeUZiY1FGT1RWU1h1RSIsInNjb3BlIjpbImFueTphbnkiXSwiZXhwIjoxNTU2MjkyNDkyLCJpYXQiOjE1NTYyODg4OTIsInRlbmFudCI6IiIsImVtYW" +
                "lsIjoiYWRtaW5kZXZAY2FwYWNpZGFkLmNvbS5hciIsInVzZXJuYW1lIjoiYWRtaW5kZXYiLCJncm91cCI6ImRldiJ9.YmI5sFHy0K19CQMpPG_fpucSFwIB1tu74ECOXsHydKY";
        when(request.getHeader(AUTHORIZATION)).thenReturn(jwt);
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_LOGIN));
        when(request.getContextPath()).thenReturn("/v1");
        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(request, never()).getHeader(TENANT_ID_HEADER);
    }

    @Test
    public void testFilterBypassWhenEndpointIsOauthAndBearerTokenIsValid() throws IOException, ServletException {
        String jwt = "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhODQxNTZhOC0zNTUwLTRmN2UtOTJmNS1kODZiZDM4ZGI4NDciLCJyb2xlIjoiYWRtaW4iLC" +
                "JlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3YxIiwiY2xpZW50X2lkIjoicHVibGljLWNsaWVudCIsImF1ZCI6InFrVWJJ" +
                "QUYyVTRITlVWVTdzeUZiY1FGT1RWU1h1RSIsInNjb3BlIjpbImFueTphbnkiXSwiZXhwIjoxNTU2MjkyNDkyLCJpYXQiOjE1NTYyODg4OTIsInRlbmFudCI6IiIsImVtYW" +
                "lsIjoiYWRtaW5kZXZAY2FwYWNpZGFkLmNvbS5hciIsInVzZXJuYW1lIjoiYWRtaW5kZXYiLCJncm91cCI6ImRldiJ9.YmI5sFHy0K19CQMpPG_fpucSFwIB1tu74ECOXsHydKY";
        when(request.getHeader(AUTHORIZATION)).thenReturn(jwt);
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_OAUTH, "/token"));
        when(request.getContextPath()).thenReturn("/v1");
        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(request, never()).getHeader(TENANT_ID_HEADER);
    }

    @Test
    public void testFilterBypassWhenBearerTokenIsNull() throws IOException, ServletException {
        when(request.getHeader(AUTHORIZATION)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(request, never()).getHeader(TENANT_ID_HEADER);
    }

    @Test
    public void testDoFilterInternalThrowsInvalidTokenExceptionWhenTokenAudIsInvalid() throws IOException, ServletException, InvalidTokenException {
        String jwt = "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhODQxNTZhOC0zNTUwLTRmN2UtOTJmNS1kODZiZDM4ZGI4NDciLCJyb2xlIjoiYWRtaW4iLC" +
                "JlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3YxIiwiY2xpZW50X2lkIjoicHVibGljLWNsaWVudCIsImF1ZCI6InFrVWJJ" +
                "QUYyVTRITlVWVTdzeUZiY1FGT1RWU1h1RSIsInNjb3BlIjpbImFueTphbnkiXSwiZXhwIjoxNTU2MjkyNDkyLCJpYXQiOjE1NTYyODg4OTIsInRlbmFudCI6IiIsImVtYW" +
                "lsIjoiYWRtaW5kZXZAY2FwYWNpZGFkLmNvbS5hciIsInVzZXJuYW1lIjoiYWRtaW5kZXYiLCJncm91cCI6ImRldiJ9.YmI5sFHy0K19CQMpPG_fpucSFwIB1tu74ECOXsHydKY";
        when(request.getHeader(AUTHORIZATION)).thenReturn(jwt);
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_USERS));
        when(request.getContextPath()).thenReturn("/v1");

        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED.value(), "", "Authentication", "/path", "");
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(apiError.getJsonObject(), HttpStatus.UNAUTHORIZED);

        InvalidTokenException invalidTokenException = new InvalidTokenException("invalid token aud");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(StringUtils.join(AUTHORIZATION_BEARER_PREFIX, " ", jwt));
        doThrow(invalidTokenException).when(tokenVerifier).validate(jwt);
        when(exceptionHandler.handleInvalidTokenException(request, invalidTokenException)).thenReturn(responseEntity);
        when(response.getWriter()).thenReturn(responseWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(exceptionHandler, times(1)).handleInvalidTokenException(request, invalidTokenException);
        verify(response, times(1)).setStatus(apiError.getStatus());
        verify(responseWriter, times(1)).write(apiError.getJsonObject());
    }

    @Test
    public void testDoFilterInternalThrowsObjectNotFoundExceptionWhenTenantDoesNotExists() throws IOException, ServletException, InvalidTokenException, ObjectNotFoundException {
        String jwt = "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhODQxNTZhOC0zNTUwLTRmN2UtOTJmNS1kODZiZDM4ZGI4NDciLCJyb2xlIjoiYWRtaW4iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwi" +
                "aXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3YxIiwiY2xpZW50X2lkIjoicHVibGljLWNsaWVudCIsImF1ZCI6InFrVWJJQUYyVTRITlVWVTdzeUZiY1FGT1RWU1h1RSIsInNjb3BlIjpbImFueTphbnkiXSwiZXhw" +
                "IjoxNTU2MjkyNDkyLCJpYXQiOjE1NTYyODg4OTIsInRlbmFudCI6InRlc3Q6ZjQyOWYyZjEtZTgxMS00NzY0LTk4ZmQtNWZmNzY2OGU4MDUxIiwiZW1haWwiOiJhZG1pbmRldkBjYXBhY2lkYWQuY29tLmFyIiwidXNlcm5" +
                "hbWUiOiJhZG1pbmRldiIsImdyb3VwIjoiZGV2In0.7jji3ZPtmKz0WxP6knEFz74Fp06q_Py_HEHxb8Hb7cw";
        when(request.getHeader(AUTHORIZATION)).thenReturn(jwt);
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_USERS));
        when(request.getContextPath()).thenReturn("/v1");

        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), "", "ObjectNotFound", "/path", "");
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(apiError.getJsonObject(), HttpStatus.NOT_FOUND);

        ObjectNotFoundException tenantNotFoundException = new ObjectNotFoundException("Tenant not found");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(StringUtils.join(AUTHORIZATION_BEARER_PREFIX, " ", jwt));
        when(tenantService.findTenantById(UUID.fromString("f429f2f1-e811-4764-98fd-5ff7668e8051"))).thenThrow(tenantNotFoundException);
        doNothing().when(tokenVerifier).validate(jwt);
        when(exceptionHandler.handleObjectNotFoundException(request, tenantNotFoundException)).thenReturn(responseEntity);
        when(response.getWriter()).thenReturn(responseWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(exceptionHandler, times(1)).handleObjectNotFoundException(request, tenantNotFoundException);
        verify(response, times(1)).setStatus(apiError.getStatus());
        verify(responseWriter, times(1)).write(apiError.getJsonObject());
    }

    @Test
    public void testDoFilterInternalSetSecurityContextWhenJwtIsValid() throws IOException, ServletException, InvalidTokenException, ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhODQxNTZhOC0zNTUwLTRmN2UtOTJmNS1kODZiZDM4ZGI4NDciLCJyb2xlIjoiYWRtaW4iLCJlbWFpbF92ZXJpZm" +
                "llZCI6dHJ1ZSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3YxIiwiY2xpZW50X2lkIjoicHVibGljLWNsaWVudCIsImF1ZCI6InFrVWJJQUYyVTRITlVWVTdzeUZiY1FGT1RWU1" +
                "h1RSIsInJlc291cmNlX2lkIjoiMWFmZGQ3OWQtNDU1Mi00YjAyLTk1ZTQtYTljNTc2M2NiNzU4Iiwic2NvcGUiOlsiYW55OmFueSJdLCJleHAiOjE1NTYyOTI0OTIsImlhdCI6MTU1NjI4" +
                "ODg5MiwidGVuYW50IjoidGVzdDpmNDI5ZjJmMS1lODExLTQ3NjQtOThmZC01ZmY3NjY4ZTgwNTEiLCJlbWFpbCI6ImFkbWluZGV2QGNhcGFjaWRhZC5jb20uYXIiLCJ1c2VybmFtZSI6I" +
                "mFkbWluZGV2IiwiZ3JvdXAiOiJkZXYifQ.9cLr4M0kD55T_s_KkfMBcrkHS4NmXDMJtq5WqJQRbCM";
        when(request.getHeader(AUTHORIZATION)).thenReturn(jwt);
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_USERS));
        when(request.getContextPath()).thenReturn("/v1");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(StringUtils.join(AUTHORIZATION_BEARER_PREFIX, " ", jwt));
        when(tenantService.findTenantById(UUID.fromString("f429f2f1-e811-4764-98fd-5ff7668e8051"))).thenReturn(new Tenant());
        doNothing().when(tokenVerifier).validate(jwt);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(securityContext, times(1)).setAuthentication(any(Authentication.class));
    }

}
