package com.capacidad.identityservice.service;

public interface Authenticator {

    boolean validateOtp(int otp, String salt);

    Integer generateOtp(String salt);

}
