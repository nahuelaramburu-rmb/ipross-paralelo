package com.capacidad.validationapi.module.totp;

public interface Authenticator {

    boolean authorize(String secret, int verificationCode, long time);

    int getTotpPassword(String secret, long time);

}
