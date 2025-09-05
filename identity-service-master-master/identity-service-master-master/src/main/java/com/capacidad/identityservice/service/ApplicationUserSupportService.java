package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.base.BaseEntity;

import java.io.Serializable;
import java.util.Map;

public interface ApplicationUserSupportService {

    <T extends BaseEntity<I>, I extends Serializable> T getEntityReference(Class<T> clazz, Long primaryKey);

    boolean passwordMatches(CharSequence rawPassword, String encodedPassword);

    String encodePassword(CharSequence rawPassword);

    long getVerificationRetryProp();

    String getActiveProfileProp();

    Integer generateOtpCode(String salt);

    boolean isOtpValid(int otp, String salt);

    String prepareTemplate(Map<String, String> values, String templateName);

    void clearTokensFromStore(String username);

}
