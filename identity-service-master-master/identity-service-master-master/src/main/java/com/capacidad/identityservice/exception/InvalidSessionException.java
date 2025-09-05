package com.capacidad.identityservice.exception;


import org.springframework.security.core.AuthenticationException;

public class InvalidSessionException extends AuthenticationException {
    private final String[] args;

    public InvalidSessionException(String message, String... args) {
        super(message);
        this.args = args;
    }

    public InvalidSessionException(String message) {
        super(message);
        this.args = null;
    }

    public String[] getArgs() {
        return this.args;
    }
}
