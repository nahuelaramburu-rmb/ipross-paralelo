package com.capacidad.validationapi.config;

import com.capacidad.validationapi.config.filter.JWTAuthenticationFilter;
import com.capacidad.validationapi.misc.ApplicationProperties;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.capacidad.utils.Constants.JWT_CLAIM_TENANT;
import static com.capacidad.utils.Constants.JWT_CLAIM_USERNAME;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.HEADER_AUTHORIZATION;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketConfigTest {

    @Mock
    private Message<?> message;

    @Mock
    private StompHeaderAccessor stompHeaderAccessor;

    @Mock
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private ApplicationProperties applicationProperties;

    /*@Mock
    private TokenVerifier tokenVerifier;*/

    @Spy
    @InjectMocks
    private WebSocketConfig webSocketConfig;


    @Test
    public void testOnPreSendReturnsMessageWhenNoAuthenticationIsRequired() {
        doReturn(stompHeaderAccessor).when(webSocketConfig).getAccessor(message);
        when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.ACK);

        webSocketConfig.onPreSend(message);

        verify(jwtAuthenticationFilter, never()).parseAndValidateJwt(anyString());
    }

    @Test
    public void testOnPreSendReturnsMessageWhenAccessorIsNull() {
        doReturn(null).when(webSocketConfig).getAccessor(message);

        webSocketConfig.onPreSend(message);

        verify(jwtAuthenticationFilter, never()).parseAndValidateJwt(anyString());
    }

    @Test
    public void testOnPreSendAuthenticateSocketSuccessfullyWithoutTenantHeaderWhenCommandIsConnect() {
        String authHeader = "Authorization Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDViNWM2YS0yNGI1LTQ4M2UtYjZkMC03NDdiOWE0YTI5MDUiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.R-mnfKKWffosZNEpn0LdnIxpQ75YUXi7_Y_aySqvQ9k";
        var authNativeHeaders = new ArrayList<String>();
        authNativeHeaders.add(authHeader);

        var parsedJwt = new HashMap<String, Object>();
        parsedJwt.put(JWT_CLAIM_TENANT, UUID.randomUUID().toString());
        parsedJwt.put(JWT_CLAIM_USERNAME, "usertest");

        doReturn(stompHeaderAccessor).when(webSocketConfig).getAccessor(message);
        when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.CONNECT);
        when(stompHeaderAccessor.getNativeHeader(HEADER_AUTHORIZATION)).thenReturn(authNativeHeaders);
        when(jwtAuthenticationFilter.parseAndValidateJwt(authHeader)).thenReturn(parsedJwt);
        when(jwtAuthenticationFilter.getAuthorities(parsedJwt)).thenReturn(Collections.emptyList());

        webSocketConfig.onPreSend(message);

        verify(stompHeaderAccessor, times(1)).setUser(any(Authentication.class));
        //verify(tokenVerifier, times(1)).verify(anyString());
    }

    @Test
    public void testOnPreSendAuthenticateSocketSuccessfullyWithoutTenantHeaderWhenCommandIsSend() {
        String authHeader = "Authorization Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDViNWM2YS0yNGI1LTQ4M2UtYjZkMC03NDdiOWE0YTI5MDUiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.R-mnfKKWffosZNEpn0LdnIxpQ75YUXi7_Y_aySqvQ9k";
        var authNativeHeaders = new ArrayList<String>();
        authNativeHeaders.add(authHeader);

        var parsedJwt = new HashMap<String, Object>();
        parsedJwt.put(JWT_CLAIM_TENANT, UUID.randomUUID().toString());
        parsedJwt.put(JWT_CLAIM_USERNAME, "usertest");

        doReturn(stompHeaderAccessor).when(webSocketConfig).getAccessor(message);
        when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.SEND);
        when(stompHeaderAccessor.getNativeHeader(HEADER_AUTHORIZATION)).thenReturn(authNativeHeaders);
        when(jwtAuthenticationFilter.parseAndValidateJwt(authHeader)).thenReturn(parsedJwt);
        when(jwtAuthenticationFilter.getAuthorities(parsedJwt)).thenReturn(Collections.emptyList());

        webSocketConfig.onPreSend(message);

        verify(stompHeaderAccessor, times(1)).setUser(any(Authentication.class));
        //verify(tokenVerifier, times(1)).verify(anyString());
    }

    @Test
    public void testOnPreSendAuthenticateSocketSuccessfullyWithoutTenantHeaderWhenCommandIsSubscribe() {
        String authHeader = "Authorization Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDViNWM2YS0yNGI1LTQ4M2UtYjZkMC03NDdiOWE0YTI5MDUiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.R-mnfKKWffosZNEpn0LdnIxpQ75YUXi7_Y_aySqvQ9k";
        var authNativeHeaders = new ArrayList<String>();
        authNativeHeaders.add(authHeader);

        var parsedJwt = new HashMap<String, Object>();
        parsedJwt.put(JWT_CLAIM_TENANT, UUID.randomUUID().toString());
        parsedJwt.put(JWT_CLAIM_USERNAME, "usertest");

        doReturn(stompHeaderAccessor).when(webSocketConfig).getAccessor(message);
        when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.SUBSCRIBE);
        when(stompHeaderAccessor.getNativeHeader(HEADER_AUTHORIZATION)).thenReturn(authNativeHeaders);
        when(jwtAuthenticationFilter.parseAndValidateJwt(authHeader)).thenReturn(parsedJwt);
        when(jwtAuthenticationFilter.getAuthorities(parsedJwt)).thenReturn(Collections.emptyList());

        webSocketConfig.onPreSend(message);

        verify(stompHeaderAccessor, times(1)).setUser(any(Authentication.class));
        //verify(tokenVerifier, times(1)).verify(anyString());
    }

    @Test
    public void testOnPreSendAuthenticateSocketFailsWhenCommandIsSubscribeAndTenantIsInvalid() {
        String authHeader = "Authorization Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDViNWM2YS0yNGI1LTQ4M2UtYjZkMC03NDdiOWE0YTI5MDUiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.R-mnfKKWffosZNEpn0LdnIxpQ75YUXi7_Y_aySqvQ9k";
        var authNativeHeaders = new ArrayList<String>();
        authNativeHeaders.add(authHeader);

        var parsedJwt = new HashMap<String, Object>();
        parsedJwt.put(JWT_CLAIM_TENANT, UUID.randomUUID().toString());
        parsedJwt.put(JWT_CLAIM_USERNAME, "usertest");

        String activeProfile = "dev";
        String destination = StringUtils.join(activeProfile, DOT, UUID.randomUUID().toString());

        doReturn(stompHeaderAccessor).when(webSocketConfig).getAccessor(message);
        when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.SUBSCRIBE);
        when(stompHeaderAccessor.getNativeHeader(HEADER_AUTHORIZATION)).thenReturn(authNativeHeaders);
        when(jwtAuthenticationFilter.parseAndValidateJwt(authHeader)).thenReturn(parsedJwt);
        when(jwtAuthenticationFilter.getAuthorities(parsedJwt)).thenReturn(Collections.emptyList());
        when(stompHeaderAccessor.getDestination()).thenReturn(destination);

        webSocketConfig.onPreSend(message);

        verify(stompHeaderAccessor, times(1)).setUser(null);
        //verify(tokenVerifier, times(1)).verify(anyString());
    }

    @Test
    public void testOnPreSendAuthenticateSocketFailsWhenCommandIsSubscribeAndTenantIsValid() {
        String authHeader = "Authorization Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDViNWM2YS0yNGI1LTQ4M2UtYjZkMC03NDdiOWE0YTI5MDUiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.R-mnfKKWffosZNEpn0LdnIxpQ75YUXi7_Y_aySqvQ9k";
        var authNativeHeaders = new ArrayList<String>();
        authNativeHeaders.add(authHeader);

        UUID tenantId = UUID.randomUUID();
        var parsedJwt = new HashMap<String, Object>();
        parsedJwt.put(JWT_CLAIM_TENANT, tenantId.toString());
        parsedJwt.put(JWT_CLAIM_USERNAME, "usertest");

        String activeProfile = "dev";
        String destination = StringUtils.join(activeProfile, DOT, tenantId.toString());

        doReturn(stompHeaderAccessor).when(webSocketConfig).getAccessor(message);
        when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.SUBSCRIBE);
        when(stompHeaderAccessor.getNativeHeader(HEADER_AUTHORIZATION)).thenReturn(authNativeHeaders);
        when(jwtAuthenticationFilter.parseAndValidateJwt(authHeader)).thenReturn(parsedJwt);
        when(jwtAuthenticationFilter.getAuthorities(parsedJwt)).thenReturn(Collections.emptyList());
        when(stompHeaderAccessor.getDestination()).thenReturn(destination);
        when(applicationProperties.getActiveProfile()).thenReturn(activeProfile);

        webSocketConfig.onPreSend(message);

        verify(stompHeaderAccessor, times(1)).setUser(any(Authentication.class));
        //verify(tokenVerifier, times(1)).verify(anyString());
        verify(stompHeaderAccessor, never()).setUser(null);
    }

}
