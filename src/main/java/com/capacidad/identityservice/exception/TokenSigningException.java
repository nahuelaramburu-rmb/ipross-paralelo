package com.capacidad.identityservice.exception;

public class TokenSigningException extends RuntimeException {
    private final String[] args;

    public TokenSigningException(String message, String... args) {
        super(message);
        this.args = args;
    }

    public TokenSigningException(String message) {
        super(message);
        this.args = null;
    }

    public String[] getArgs() {
        return this.args;
    }
}
