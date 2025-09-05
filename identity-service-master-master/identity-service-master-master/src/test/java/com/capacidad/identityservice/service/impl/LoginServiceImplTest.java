package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.exception.InvalidUserStateException;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.model.LoginEvent;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.model.projection.LoginViewDTO;
import com.capacidad.identityservice.repository.LoginRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoginServiceImplTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private LoginRepository loginRepository;
    @InjectMocks
    private LoginServiceImpl loginService;


    @Test(expected = InvalidUserStateException.class)
    public void testRegisterLoginAttemptThrowsExceptionWhenFailureLoginAndMaxAttemptsReached() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("username");
        user.setPassword("password");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("test"));

        CustomUserDetails customUserDetails = new CustomUserDetails(user.getUsername(), user.getPassword(), authorities);
        customUserDetails.setApplicationUser(user);

        LoginViewDTO login1 = new LoginViewDTO(5L, LocalDateTime.now(), "principal");
        List<LoginViewDTO> loginList = new ArrayList<>();
        loginList.add(new LoginViewDTO(1L, LocalDateTime.now(), "principal"));
        loginList.add(new LoginViewDTO(2L, LocalDateTime.now(), "principal"));
        loginList.add(new LoginViewDTO(3L, LocalDateTime.now(), "principal"));
        loginList.add(new LoginViewDTO(4L, LocalDateTime.now(), "principal"));
        loginList.add(login1);

        when(loginRepository.findAllByPrincipalOrIpAddressAndFailureEvent(any(), any())).thenReturn(loginList);

        loginService.registerLoginAttempt(customUserDetails, LoginEvent.FAILURE, request);
    }

    @Test
    public void testRegisterLoginAttemptDoNotFailWhenSuccessLoginAndExpiredMaxAttempts() {
        TenantContext.setTenant(null);

        ApplicationUser user = new ApplicationUser();
        user.setUsername("username");
        user.setPassword("password");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("test"));

        CustomUserDetails customUserDetails = new CustomUserDetails(user.getUsername(), user.getPassword(), authorities);
        customUserDetails.setApplicationUser(user);

        LoginViewDTO login1 = new LoginViewDTO(5L, LocalDateTime.now().minusMinutes(20), "username");
        List<LoginViewDTO> loginList = new ArrayList<>();
        loginList.add(new LoginViewDTO(1L, LocalDateTime.now(), "username"));
        loginList.add(new LoginViewDTO(2L, LocalDateTime.now(), "username"));
        loginList.add(new LoginViewDTO(3L, LocalDateTime.now(), "username"));
        loginList.add(new LoginViewDTO(4L, LocalDateTime.now(), "username"));
        loginList.add(login1);

        when(loginRepository.findAllByPrincipalOrIpAddressAndFailureEvent(any(), any())).thenReturn(loginList);

        loginService.registerLoginAttempt(customUserDetails, LoginEvent.SUCCESS, request);

        verify(loginRepository, never()).save(any());
        verify(loginRepository, times(1)).deleteAll(anyCollection());
    }

    @Test
    public void testRegisterLoginAttemptDoNotFailWhenSuccessLoginAndNoMaxAttemptsReached() {
        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);

        List<LoginViewDTO> loginList = new ArrayList<>();
        loginList.add(new LoginViewDTO(1L, LocalDateTime.now(), "principal"));

        when(loginRepository.findAllByPrincipalOrIpAddressAndFailureEvent(any(), any())).thenReturn(loginList);

        loginService.registerLoginAttempt("principal", LoginEvent.SUCCESS, request);

        verify(loginRepository, never()).save(any());
        verify(loginRepository, times(1)).deleteAll(anyCollection());
    }

    @Test
    public void testRegisterLoginAttemptDoNotFailWhenFailureLoginAndNoMaxAttemptsReached() {
        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);

        List<LoginViewDTO> loginList = new ArrayList<>();
        loginList.add(new LoginViewDTO(1L, LocalDateTime.now(), "principal"));

        when(loginRepository.findAllByPrincipalOrIpAddressAndFailureEvent(any(), any())).thenReturn(loginList);

        loginService.registerLoginAttempt("principal", LoginEvent.FAILURE, request);

        verify(loginRepository, times(1)).save(any());
        verify(loginRepository, never()).deleteAll(anyCollection());
    }

    @Test
    public void testRegisterLoginAttemptDoNotFailWhenSuccessAndEmptyAttempts() {
        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);

        when(loginRepository.findAllByPrincipalOrIpAddressAndFailureEvent(any(), any())).thenReturn(Collections.emptyList());

        loginService.registerLoginAttempt("principal", LoginEvent.SUCCESS, request);

        verify(loginRepository, never()).save(any());
        verify(loginRepository, never()).deleteAll(anyCollection());
    }

}
