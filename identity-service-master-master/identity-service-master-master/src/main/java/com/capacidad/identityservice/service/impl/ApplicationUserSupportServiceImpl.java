package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.base.BaseEntity;
import com.capacidad.identityservice.service.ApplicationUserSupportService;
import com.capacidad.identityservice.service.Authenticator;
import com.capacidad.identityservice.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
public class ApplicationUserSupportServiceImpl implements ApplicationUserSupportService {

    private final PasswordEncoder passwordEncoder;
    private final Utils utils;
    private final Authenticator authenticator;
    private final ApplicationProperties applicationProperties;
    private final TemplateService templateService;
    private final ClientRegistrationService clientRegistrationService;
    private final TokenStore tokenStore;

    @Autowired
    public ApplicationUserSupportServiceImpl(PasswordEncoder passwordEncoder,
                                             Utils utils,
                                             Authenticator authenticator,
                                             ApplicationProperties applicationProperties,
                                             TemplateService templateService,
                                             @Qualifier("jdbcClientDetailsService") JdbcClientDetailsService clientDetailsService,
                                             TokenStore tokenStore) {
        this.passwordEncoder = passwordEncoder;
        this.utils = utils;
        this.authenticator = authenticator;
        this.applicationProperties = applicationProperties;
        this.templateService = templateService;
        this.clientRegistrationService = clientDetailsService;
        this.tokenStore = tokenStore;
    }

    public <T extends BaseEntity<I>, I extends Serializable> T getEntityReference(Class<T> clazz, Long primaryKey) {
        return utils.getEntityReference(clazz, primaryKey);
    }

    public boolean passwordMatches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String encodePassword(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public long getVerificationRetryProp() {
        return applicationProperties.getVerificationRetry();
    }

    @Override
    public String getActiveProfileProp() {
        return applicationProperties.getActiveProfile();
    }

    @Override
    public Integer generateOtpCode(String salt) {
        return authenticator.generateOtp(salt);
    }

    @Override
    public boolean isOtpValid(int otp, String salt) {
        return authenticator.validateOtp(otp, salt);
    }

    @Override
    public String prepareTemplate(Map<String, String> values, String templateName) {
        return templateService.prepareTemplate(values, templateName);
    }

    @Override
    public void clearTokensFromStore(String username) {
        for (ClientDetails client : clientRegistrationService.listClientDetails()) {
            for (final OAuth2AccessToken token : tokenStore.findTokensByClientIdAndUserName(client.getClientId(), username)) {
                tokenStore.removeRefreshToken(token.getRefreshToken());
                tokenStore.removeAccessToken(token);
            }
        }
    }

}
