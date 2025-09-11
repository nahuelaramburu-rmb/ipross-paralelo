package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.exception.InvalidUserStateException;

import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.model.Login;
import com.capacidad.identityservice.model.LoginEvent;
import com.capacidad.identityservice.model.projection.LoginViewDTO;
import com.capacidad.identityservice.repository.LoginRepository;
import com.capacidad.identityservice.service.LoginService;
import  com.capacidad.identityservice.misc.securityutils.SecurityUtils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


/*
* gestiona los intentos de login de los usuarios:
*
*   Validar si un usuario ha excedido el número máximo de intentos fallidos.
    Registrar intentos de login exitosos o fallidos.
    Resetear los intentos fallidos después de un login exitoso.
    Almacenar los intentos de login en la base de datos, usando un batch para los éxitos.

    * Se basa en varios conceptos de seguridad y multi-tenant (cada login puede asociarse a un "tenant").

    *   en resumén:
    *   Valida y limita los intentos de login fallidos.
        Registra tanto logins exitosos como fallidos.
        Optimiza la inserción de logins exitosos en batch.
        Resetea intentos fallidos tras un login exitoso.
        Soporta multi-tenant y obtiene información de IP/User-Agent.
    * *
* */


@Log4j2
@Service
public class LoginServiceImpl implements LoginService {

    // máximo de intentos fallidos permitidos.
    private static final int MAX_ATTEMPTS = 5;

    // tiempo que debe esperar un usuario bloqueado (en minutos) antes de intentar de nuevo.
    private static final int AWAIT_MINUTES = 10;

    // cantidad de logins exitosos que se guardarán en batch en la base de datos
    private static final int SUCCESS_LOGIN_INSERT_BATCH_SIZE = 50;

    // cola concurrente para almacenar logins exitosos temporalmente antes de hacer el batch insert.
    private static final ConcurrentLinkedQueue<Login> LOGIN_CONCURRENT_LIST = new ConcurrentLinkedQueue<>();


    private final LoginRepository loginRepository;

    @Autowired
    public LoginServiceImpl(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }


    // valida los intentos de ingreso fallidos del user
    private List<LoginViewDTO> validateAttempts(String principal, HttpServletRequest request) {

        //Obtiene todos los intentos fallidos del usuario o de la IP actual (loginInvalidAttempts).
        // principal -> username
        List<LoginViewDTO> loginInvalidAttempts = loginRepository.findAllByPrincipalOrIpAddressAndFailureEvent(principal, SecurityUtils.getRequestIPAddress(request));

        //Si el usuario ha excedido MAX_ATTEMPTS
        if (loginInvalidAttempts.size() >= MAX_ATTEMPTS) {

            // obtiene el ultimo intento de ingreso fallido del user
            LoginViewDTO lastFailedLogin = loginInvalidAttempts.get(loginInvalidAttempts.size() - 1);

            //si el último intento fallido fue hace menos de AWAIT_MINUTES, lanza una excepción InvalidUserStateException bloqueando el login.
            if (Duration.between(lastFailedLogin.getCreatedAt(), LocalDateTime.now()).toMinutes() < AWAIT_MINUTES) {
                throw new InvalidUserStateException("login.maxAttempts");

            }
        }

        //Retorna la lista de intentos fallidos para su uso posterior.
        return loginInvalidAttempts;
    }


    //Elimina de la base de datos los intentos de login fallidos que ya no son necesarios, normalmente después de un login exitoso.
    @Override
    public void resetLoginAttempts(List<Login> loginList) {
        loginRepository.deleteAll(loginList);
    }



    @Override
    public void registerLoginAttempt(Object principal, LoginEvent event, HttpServletRequest request) {

        //Convierte el objeto principal (puede ser un CustomUserDetails, un User o un string) a su nombre de usuario mediante resolvePrincipal.
        String principalName = resolvePrincipal(principal);

        //Valida los intentos fallidos con validateAttempts.
        List<LoginViewDTO> validatedLoginAttempts = validateAttempts(principalName, request);

        //Construye un objeto Login con información del login (buildLoginObject).
        Login login = buildLoginObject(principalName, principal.getClass().getSimpleName(), event, request);

        //Si el login fue exitoso (LoginEvent.SUCCESS):
        if (event.equals(LoginEvent.SUCCESS)) {

            //Se agrega a la cola LOGIN_CONCURRENT_LIST.
            LOGIN_CONCURRENT_LIST.add(login);

            //Si la cola llega a 50 elementos, se hace un insert batch a la base de datos y se limpia la cola.
            if (LOGIN_CONCURRENT_LIST.size() == SUCCESS_LOGIN_INSERT_BATCH_SIZE) {
                loginRepository.saveAll(LOGIN_CONCURRENT_LIST);
                LOGIN_CONCURRENT_LIST.clear(); //Si existían intentos fallidos, se eliminan de la base de datos.
            }

            // limpia los intentos de ingreso fallido del user , ya que tuvo 1 ingreso exitoso
            if (!validatedLoginAttempts.isEmpty()) {
                List<Login> loginList = validatedLoginAttempts.stream()
                        .map(LoginViewDTO::buildLogin)
                        .filter(l -> StringUtils.equals(l.getPrincipal(), principalName))
                        .collect(Collectors.toList());
                resetLoginAttempts(loginList);
            }
        } else
            //Si el login fue fallido, se guarda directamente en la base de datos.
            loginRepository.save(login);
    }


    // Convierte distintos tipos de objetos de usuario en un nombre de usuario String.
    private String resolvePrincipal(Object principal) {
        if (principal instanceof CustomUserDetails)
            return ((CustomUserDetails) principal).getUsername();
        if (principal instanceof User)
            return ((User) principal).getUsername();
        return principal.toString();
    }


    //Crea un objeto Login con toda la información relevante:
    //Usuario (principal)
    //Clase del usuario (principalClass)
    //Evento de login (SUCCESS o FAILURE)
    //Tenant actual (multi-tenant)
    //User-Agent e IP del request
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
