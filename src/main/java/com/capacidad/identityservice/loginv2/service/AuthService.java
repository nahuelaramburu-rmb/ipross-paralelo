package com.capacidad.identityservice.loginv2.service;

import com.capacidad.identityservice.loginv2.model.AuthUserInfo;
import com.capacidad.identityservice.loginv2.model.RegisterRequestDTO;
import com.capacidad.identityservice.loginv2.repository.AuthUserInfoRepository;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.repository.ApplicationUserRepository;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    // se usa para buscar usuarios y sus contextos (tenant, permisos, etc.) en la base de datos.
    @Autowired
    private ApplicationUserContextService userContextService;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private LoginService loginService; // registra los intentos de login de cada user

    @Autowired
    private AuthUserInfoRepository authUserInfoRepository;


    public ApplicationUser loadUserByUsername(String username, String password, HttpServletRequest request) {

        // representa a los usuarios de este sistema
        var user = applicationUserRepository.findByUsername(username);


        // todo , verificar si la password en db esta encriptada,
        if (user.isEmpty() || !Objects.equals(user.get().getPassword(), password)) {

            loginService.registerLoginAttempt(user, LoginEvent.FAILURE, request);

            throw new UsernameNotFoundException("credenciales inválidas");
        }


        loginService.registerLoginAttempt(user, LoginEvent.SUCCESS, request);

        return user.get();
    }

    // todo , implementar jwt en el login de user
    public ApplicationUser loadUserByEmail(String username, String password, HttpServletRequest request) {

        // representa a los usuarios de este sistema
        var user = applicationUserRepository.findByEmail(username);


        // todo , verificar si la password en db esta encriptada,
        if (user.isEmpty() || !Objects.equals(user.get().getPassword(), password)) {

            loginService.registerLoginAttempt(user, LoginEvent.FAILURE, request);

            throw new UsernameNotFoundException("credenciales inválidas");
        }


        loginService.registerLoginAttempt(user, LoginEvent.SUCCESS, request);

        return user.get();
    }


    // todo , ver si puedo usar los metodos en applicationuserservice ,
    //  para llevar a cabo la logica del registro de user, ver el metodo "create"
    //
    public void register(RegisterRequestDTO request) {

//        Group group = Group.DEV;
//
//        ApplicationUser user = new ApplicationUser(
//                request.getNombre(),
//                request.getEmail(),
//                request.getPassword(),
//                group
//        );
//        var state = new State();
//        state.setId(StateReference.CONFIRMED.getId());
//        user.setState(state);

        AuthUserInfo authUserInfo = new AuthUserInfo(

                request.getNombre(),
                request.getNumero_afiliado(),
                request.getEmail(),
                request.getPassword(),
                request.getEstado(),
                request.getPlan()
        );


        authUserInfoRepository.save(authUserInfo);
    }



    /*
*
* {
  "id": "12345",
  "nombre": "Juan Pérez",
  "numero_afiliado": "123456789",
  "email": "juan.perez@email.com",
  "estado": "activo",
  "plan": "Plan Básico"
}
* */


    public AuthUserInfo loadUserInfo(String username, HttpServletRequest request) {


        Optional<AuthUserInfo> userInfo = authUserInfoRepository.findByNombre(username);


        if (userInfo.isEmpty()){

            throw new UsernameNotFoundException("user no encontrado");
        }

        return userInfo.get();

    }


    /*
    *
    * public ApplicationUser(String username, String email, String password , Group group ,State state) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.group = group;
        this.state = state;
    }
    * */
}

