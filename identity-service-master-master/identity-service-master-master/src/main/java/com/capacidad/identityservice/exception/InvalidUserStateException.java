package com.capacidad.identityservice.exception;

import org.springframework.security.core.AuthenticationException;


public class InvalidUserStateException extends AuthenticationException {
    private final String[] args;

    public InvalidUserStateException(String message, String... args) {
        super(message);
        this.args = args;
    }

    public InvalidUserStateException(String message) {
        super(message);
        this.args = null;
    }

    public String[] getArgs() {
        return this.args;
    }
}
