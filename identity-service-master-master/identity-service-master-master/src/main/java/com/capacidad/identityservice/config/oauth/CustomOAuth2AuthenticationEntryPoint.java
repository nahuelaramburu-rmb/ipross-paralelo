package com.capacidad.identityservice.config.oauth;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;

import java.util.Locale;

public class CustomOAuth2AuthenticationEntryPoint extends OAuth2AuthenticationEntryPoint {

    private final ExceptionTranslatorConfig translatorConfig;

    public CustomOAuth2AuthenticationEntryPoint(ExceptionTranslatorConfig translatorConfig) {
        this.translatorConfig = translatorConfig;
    }

    @Override
    protected ResponseEntity<?> enhanceResponse(ResponseEntity<?> response, Exception exception) {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        ResponseEntity<?> responseEntity = super.enhanceResponse(response, exception);
        Object body = responseEntity.getBody();
        if (!(body instanceof OAuth2Exception))
            return responseEntity;
        translatorConfig.resolveExceptionMessage(exception, (OAuth2Exception) body);
        return responseEntity;
    }
}
