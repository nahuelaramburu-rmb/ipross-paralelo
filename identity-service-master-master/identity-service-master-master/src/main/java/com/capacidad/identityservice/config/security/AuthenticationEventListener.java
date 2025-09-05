package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.model.LoginEvent;
import com.capacidad.identityservice.service.LoginService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationEventListener {

    private final LoginService loginService;

    public AuthenticationEventListener(LoginService loginService) {
        this.loginService = loginService;
    }

    @EventListener
    public void authenticationFailed(AuthenticationFailureBadCredentialsEvent event) {
        HttpServletRequest request = getCurrentRequest();
        Authentication authentication = event.getAuthentication();
        loginService.registerLoginAttempt(authentication.getPrincipal(), LoginEvent.FAILURE, request);
    }

    @EventListener
    public void authenticationSuccess(AuthenticationSuccessEvent event) {
        HttpServletRequest request = getCurrentRequest();
        Authentication authentication = event.getAuthentication();
        loginService.registerLoginAttempt(authentication.getPrincipal(), LoginEvent.SUCCESS, request);
    }

    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

}
