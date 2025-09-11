package com.capacidad.identityservice.exception;

import org.springframework.security.core.AuthenticationException;

public class UnspecifiedTenantException extends AuthenticationException {
    public UnspecifiedTenantException(String msg) {
        super(msg);
    }
}
