package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.exception.InvalidUserStateException;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.model.Login;
import com.capacidad.identityservice.model.LoginEvent;
import com.capacidad.identityservice.model.projection.LoginViewDTO;
import com.capacidad.identityservice.repository.LoginRepository;
import com.capacidad.identityservice.service.LoginService;
import com.capacidad.utils.SecurityUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Log4j2
@Service
public class LoginServiceImpl implements LoginService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int AWAIT_MINUTES = 10;
    private static final int SUCCESS_LOGIN_INSERT_BATCH_SIZE = 50;
    private static final ConcurrentLinkedQueue<Login> LOGIN_CONCURRENT_LIST = new ConcurrentLinkedQueue<>();
    private final LoginRepository loginRepository;

    @Autowired
    public LoginServiceImpl(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    private List<LoginViewDTO> validateAttempts(String principal, HttpServletRequest request) {
        List<LoginViewDTO> loginInvalidAttempts = loginRepository.findAllByPrincipalOrIpAddressAndFailureEvent(principal, SecurityUtils.getRequestIPAddress(request));
        if (loginInvalidAttempts.size() >= MAX_ATTEMPTS) {
            LoginViewDTO lastFailedLogin = loginInvalidAttempts.get(loginInvalidAttempts.size() - 1);
            if (Duration.between(lastFailedLogin.getCreatedAt(), LocalDateTime.now()).toMinutes() < AWAIT_MINUTES)
                throw new InvalidUserStateException("login.maxAttempts");
        }
        return loginInvalidAttempts;
    }

    @Override
    public void resetLoginAttempts(List<Login> loginList) {
        loginRepository.deleteAll(loginList);
    }

    @Override
    public void registerLoginAttempt(Object principal, LoginEvent event, HttpServletRequest request) {
        String principalName = resolvePrincipal(principal);
        List<LoginViewDTO> validatedLoginAttempts = validateAttempts(principalName, request);
        Login login = buildLoginObject(principalName, principal.getClass().getSimpleName(), event, request);
        if (event.equals(LoginEvent.SUCCESS)) {
            LOGIN_CONCURRENT_LIST.add(login);
            if (LOGIN_CONCURRENT_LIST.size() == SUCCESS_LOGIN_INSERT_BATCH_SIZE) {
                loginRepository.saveAll(LOGIN_CONCURRENT_LIST);
                LOGIN_CONCURRENT_LIST.clear();
            }
            if (!validatedLoginAttempts.isEmpty()) {
                List<Login> loginList = validatedLoginAttempts.stream()
                        .map(LoginViewDTO::buildLogin)
                        .filter(l -> StringUtils.equals(l.getPrincipal(), principalName))
                        .collect(Collectors.toList());
                resetLoginAttempts(loginList);
            }
        } else
            loginRepository.save(login);
    }

    private String resolvePrincipal(Object principal) {
        if (principal instanceof CustomUserDetails)
            return ((CustomUserDetails) principal).getUsername();
        if (principal instanceof User)
            return ((User) principal).getUsername();
        return principal.toString();
    }

    private Login buildLoginObject(String principal, String principalClass, LoginEvent event, HttpServletRequest request) {
        Login login = new Login();
        login.setLoginEvent(event);
        login.setPrincipal(principal);
        login.setPrincipalClass(principalClass);
        login.setTenantContext(TenantContext.getTenant() != null ? TenantContext.getTenant().getTenantId().toString() : null);
        login.setAgent(request.getHeader("User-Agent"));
        login.setIpAddress(SecurityUtils.getRequestIPAddress(request));
        return login;
    }

}
