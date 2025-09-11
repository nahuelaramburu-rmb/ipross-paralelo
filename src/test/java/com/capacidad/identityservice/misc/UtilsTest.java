package com.capacidad.identityservice.misc;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.*;
import static com.capacidad.identityservice.misc.constant.ScopeConstants.READ;
import static com.capacidad.identityservice.misc.constant.ScopeConstants.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private PrintWriter printWriter;
    @Mock
    private HttpServletResponse response;

    @Test
    public void testSendErrorDoNotWritesOnResponseWhenApiErrorIsNull() throws IOException {
        Utils.sendError(response, new ResponseEntity<>(null, null, HttpStatus.OK));

        verify(response, never()).getWriter();
    }

    @Test
    public void testSendErrorWritesOnResponseWhenApiErrorIsNotNull() throws IOException {
        when(response.getWriter()).thenReturn(printWriter);

        Utils.sendError(response, new ResponseEntity<>("response", null, HttpStatus.OK));

        verify(printWriter, times(1)).write("response");
    }

    @Test
    public void testIsLoginOrLogoutEndpointReturnsFalseOnAnyEndpoint() {
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_USERS));
        when(request.getContextPath()).thenReturn("/v1");

        boolean result = Utils.isLoginOrLogoutEndpoint(request);

        assertThat(result).isFalse();
    }

    @Test
    public void testIsLoginOrLogoutEndpointReturnTrueOnLoginEndpoint() {
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_LOGIN));
        when(request.getContextPath()).thenReturn("/v1");

        boolean result = Utils.isLoginOrLogoutEndpoint(request);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsLoginOrLogoutEndpointReturnTrueOnLogoutEndpoint() {
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_LOGOUT));
        when(request.getContextPath()).thenReturn("/v1");

        boolean result = Utils.isLoginOrLogoutEndpoint(request);

        assertThat(result).isTrue();
    }

    //

    @Test
    public void testIsOAuthEndpointReturnsFalseOnAnyEndpoint() {
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", ENDPOINT_USERS));
        when(request.getContextPath()).thenReturn("/v1");

        boolean result = Utils.isOAuthEndpoint(request);

        assertThat(result).isFalse();
    }

    @Test
    public void testIsOAuthEndpointReturnTrueOnOauthAuthorizeEndpoint() {
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", "/oauth/authorize"));
        when(request.getContextPath()).thenReturn("/v1");

        boolean result = Utils.isOAuthEndpoint(request);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsOAuthEndpointReturnTrueOnOauthTokenEndpoint() {
        when(request.getRequestURI()).thenReturn(StringUtils.join("/v1", "/oauth/token"));
        when(request.getContextPath()).thenReturn("/v1");

        boolean result = Utils.isOAuthEndpoint(request);

        assertThat(result).isTrue();
    }

    @Test
    public void testBuildScopeReturnsValidResult() {
        String result = Utils.buildScope(READ, USERS);
        assertThat(result).isEqualTo("read:users");
    }

}
