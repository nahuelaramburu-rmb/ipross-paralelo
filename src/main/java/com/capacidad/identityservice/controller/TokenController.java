package com.capacidad.identityservice.controller;

import com.capacidad.identityservice.config.token.TokenVerifierImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.ENDPOINT_OAUTH;

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
