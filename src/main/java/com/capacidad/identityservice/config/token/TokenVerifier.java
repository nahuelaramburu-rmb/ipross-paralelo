package com.capacidad.identityservice.config.token;

import com.capacidad.utils.exception.InvalidTokenException;

public interface TokenVerifier {

    void verify(String jwt) throws InvalidTokenException;

    void validate(String jwt) throws InvalidTokenException;

}
