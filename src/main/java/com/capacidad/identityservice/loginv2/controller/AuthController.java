package com.capacidad.identityservice.loginv2.controller;

import com.capacidad.identityservice.loginv2.service.AuthService;
import com.capacidad.identityservice.loginv2.model.LoginRequestDTO;
import com.capacidad.identityservice.loginv2.model.RegisterRequestDTO;
import com.capacidad.identityservice.misc.constant.ControllerEndpoints;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// http://localhost:8080/identity-service/auth/login

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_AUTH)
public class AuthController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ApplicationUserContextService applicationUserContextService;

    @Autowired
    private AuthService authService;


    // todo , verificar como se logean los user , con username, nro afiliado o email
    @PostMapping(value = ControllerEndpoints.ENDPOINT_LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO , HttpServletRequest request) throws ObjectNotFoundException, ObjectNotValidException {

        var user = authService.loadUserByEmail(loginRequestDTO.getUsername() ,loginRequestDTO.getPassword() , request );


        return ResponseEntity.ok(user.getUsername());

        // retornar el token de acceso
        //return ResponseEntity.ok(Map.of("message", "login exitoso"));
    }


    @GetMapping("/getuserinfo/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username, HttpServletRequest request) throws ObjectNotFoundException, ObjectNotValidException {

        var user = authService.loadUserInfo(username,request);


        return ResponseEntity.ok(user);

        // retornar el token de acceso
        //return ResponseEntity.ok(Map.of("message", "login exitoso"));
    }



    // el register no se expone en la app , quizas se crean usuarios desde un panel web ...??
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) throws ObjectNotFoundException {

        authService.register(request);

        //   return ResponseEntity.ok(Map.of("message", userService.register(request)));

        return ResponseEntity.ok(Map.of("message", "register"));
    }
}
