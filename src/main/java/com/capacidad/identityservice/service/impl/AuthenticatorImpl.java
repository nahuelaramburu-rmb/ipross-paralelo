package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.service.Authenticator;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.HmacHashFunction;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.EQUAL;

@Component
public class AuthenticatorImpl implements Authenticator {

    private final GoogleAuthenticator authenticator;
    private final ApplicationProperties applicationProperties;
    private final Base32 codec32;

    @Autowired
    public AuthenticatorImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        GoogleAuthenticatorConfig googleAuthenticatorConfig = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setHmacHashFunction(HmacHashFunction.HmacSHA1)
                .setKeyRepresentation(KeyRepresentation.BASE32)
                .setCodeDigits(6)
                .setTimeStepSizeInMillis(TimeUnit.MINUTES.toMillis(applicationProperties.getVerificationWindow()))
                .setWindowSize(10)
                .build();
        this.authenticator = new GoogleAuthenticator(googleAuthenticatorConfig);
        this.codec32 = new Base32();
    }

    @Override
    public boolean validateOtp(int otp, String salt) {
        return authenticator.authorize(getBase32Key(salt), otp, Instant.now().toEpochMilli());
    }

    @Override
    public Integer generateOtp(String salt) {
        return authenticator.getTotpPassword(getBase32Key(salt), Instant.now().toEpochMilli());
    }

    //
    private String getBase32Key(String salt) {
        String stringKey = StringUtils.join(applicationProperties.getOtpKey(), salt);
        return codec32.encodeAsString(stringKey.getBytes()).replace(EQUAL, "");
    }

}
