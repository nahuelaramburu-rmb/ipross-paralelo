package com.capacidad.identityservice.controller;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.dto.authdto.LoginRequestDTO;
import com.capacidad.identityservice.model.dto.authdto.RegisterRequest;
import com.capacidad.identityservice.misc.DTOtoEntityMapper;
import com.capacidad.identityservice.misc.constant.ControllerEndpoints;
import com.capacidad.identityservice.model.dto.authdto.RegisterResponseDTO;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.ApplicationUserService;
import com.capacidad.identityservice.service.impl.AuthenticationService;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// http://localhost:8080/identity-service/auth/login


// todo , implementar register , con ApplicationUser como modelo

// data a enviar para registrar un user

/*
*
* {
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "MySecurePass123",
  "emailVerified": false,
  "challengeType": "OTP",  // no implementado
  "group": "DEV",
  "profile": {
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+123456789", // opcional
    "address": "123 Main St"  // opcional
  },
  "state": {
    "id": 1
  },
  "contextSet": [
    {
      "tenant": {
        "id": 10
      },
      "role": {
        "id": 5
      },
      "permissionSuggestion": {
        "id": 2
      },
      "permissionStrategy": "ALLOW",
      "permissionGroups": [
        { "id": 100 },
        { "id": 101 }
      ]
    }
  ]
}

*
* Campos requeridos not null
* {
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "MySecurePass123",
  "group": "DEV",  // a asignar
  "profile": {
    "firstName": "John",
    "lastName": "Doe"
  },
  "state": {
    "id": 1 // a asignar
  },
  "contextSet": [
    {
      "tenant": {
        "id": 10 // a asignar
      },
      "role": {
        "id": 5 // a asignar
      },
      "permissionStrategy": "ALLOW" // a asignar
    }
  ]
}

*
* body del register request dto
* {
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "MySecurePass123",
  "profile": {
    "firstName": "John",
    "lastName": "Doe"
  },
*
*
*
*
* */


@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_AUTH)
public class AuthController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ApplicationUserContextService applicationUserContextService;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    DTOtoEntityMapper dtOtoEntityMapper;

    @Autowired
    private AuthenticationService authenticationService;


    // sirve para registrar users en app mobile,
    // no esta implementado para multitenancy
    @PostMapping("/register-beneficiary")
    public ResponseEntity<?> registerBeneficiary(@RequestBody RegisterRequest request) throws ObjectNotFoundException, ObjectNotValidException {

        ApplicationUser user = authenticationService.createBeneficiary(dtOtoEntityMapper.mapRegisterRequestToApplicationUser(request));

        RegisterResponseDTO registerResponseDTO = dtOtoEntityMapper.ApplicationUserToRegisterResponse(user);

        return ResponseEntity.ok(Map.of("message", registerResponseDTO));
    }


    // sirve para registrar users en app mobile,
    // no esta implementado para multitenancy
    @PostMapping("/register-practitioner")
    public ResponseEntity<?> registerPractitioner(@RequestBody RegisterRequest request) throws ObjectNotFoundException, ObjectNotValidException {

        ApplicationUser user = authenticationService.createPractitioner(dtOtoEntityMapper.mapRegisterRequestToApplicationUser(request));

        RegisterResponseDTO registerResponseDTO = dtOtoEntityMapper.ApplicationUserToRegisterResponse(user);

        return ResponseEntity.ok(Map.of("message", registerResponseDTO));
    }



    // se valida el user con email y password
    // todo , se debe implementar el login validator, que permite loggearse despues de x tiempo y depues de x veces
    // todo , debe retornar un jwt , con data del user , roles , para luego poder acceder a ciertos endpoints y ser autorizado
    @PostMapping(value = ControllerEndpoints.ENDPOINT_LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO , HttpServletRequest request) throws ObjectNotFoundException, ObjectNotValidException {

     //   var user = applicationUserService.login(loginRequestDTO);

        //RegisterResponseDTO loginResponseDTO = dtOtoEntityMapper.ApplicationUserToRegisterResponse(user);

        return ResponseEntity.ok(Map.of("message", authenticationService.login(loginRequestDTO)));

        // retornar el token de acceso
        //return ResponseEntity.ok(Map.of("message", "login exitoso"));
    }






}
