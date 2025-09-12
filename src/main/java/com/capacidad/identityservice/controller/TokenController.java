package com.capacidad.identityservice.controller;

import com.capacidad.identityservice.config.token.TokenVerifierImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.ENDPOINT_OAUTH;


/*
* Este controlador expone un endpoint para que otros servicios o clientes puedan verificar si un JWT es válido.

Si el token es válido, devuelve 204 No Content (token aceptado).

Si el token es inválido o expirado, devuelve un error (excepciones en TokenVerifierImpl).
*
*
* */


@RestController
@RequestMapping(value = ENDPOINT_OAUTH)
public class TokenController {

    private final TokenVerifierImpl tokenVerifier;

    @Autowired
    public TokenController(TokenVerifierImpl tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @PostMapping(value = "/verify_token", params = {"token"})
    public ResponseEntity<Object> verifyToken(@RequestParam String token) {
        tokenVerifier.verify(token);
        return ResponseEntity.noContent().build();
    }

}
