package com.capacidad.validationapi.module.totp;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.HmacHashFunction;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AuthenticatorImpl implements Authenticator {

    private final GoogleAuthenticator googleAuthenticator;

    public AuthenticatorImpl() {
        GoogleAuthenticatorConfig googleAuthenticatorConfig = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setHmacHashFunction(HmacHashFunction.HmacSHA1)
                .setKeyRepresentation(KeyRepresentation.BASE32)
                .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(120))
                .setWindowSize(3)
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(googleAuthenticatorConfig);
    }

    @Override
    public boolean authorize(String secret, int verificationCode, long time) {
        return googleAuthenticator.authorize(secret, verificationCode, time);
    }

    @Override
    public int getTotpPassword(String secret, long time) {
        return googleAuthenticator.getTotpPassword(secret, time);
    }
}
