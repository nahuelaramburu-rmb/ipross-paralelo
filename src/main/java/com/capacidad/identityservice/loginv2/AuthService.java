package com.capacidad.identityservice.loginv2;

import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.repository.ApplicationUserRepository;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthService {

    // se usa para buscar usuarios y sus contextos (tenant, permisos, etc.) en la base de datos.
    @Autowired
    private ApplicationUserContextService userContextService;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private LoginService loginService; // registra los intentos de login de cada user


    public ApplicationUser loadUserByUsername(String username, String password , HttpServletRequest request) {

        // representa a los usuarios de este sistema
        var user = applicationUserRepository.findByUsername(username);


        // todo , verificar si la password en db esta encriptada,
        if (user.isEmpty() || !Objects.equals(user.get().getPassword(), password)) {

            loginService.registerLoginAttempt(user, LoginEvent.FAILURE , request);

            throw new UsernameNotFoundException("credenciales inválidas");
        }


        loginService.registerLoginAttempt(user , LoginEvent.SUCCESS , request);

        return user.get();
    }
}
