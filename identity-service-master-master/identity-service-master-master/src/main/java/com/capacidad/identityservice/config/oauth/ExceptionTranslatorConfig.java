package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;

import static com.capacidad.identityservice.misc.Utils.processExceptionSimpleName;
import static com.capacidad.identityservice.misc.constant.ApplicationConstants.*;
import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.ENDPOINT_OAUTH;

@Log4j2
@Configuration
public class ExceptionTranslatorConfig {

    private final GlobalExceptionHandler exceptionHandler;

    @Autowired
    public ExceptionTranslatorConfig(GlobalExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Bean
    public WebResponseExceptionTranslator<OAuth2Exception> oauth2ResponseExceptionTranslator() {
        return new DefaultWebResponseExceptionTranslator() {
            @Override
            public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {

                ResponseEntity<OAuth2Exception> responseEntity = super.translate(e);
                OAuth2Exception body = responseEntity.getBody();
                HttpStatus statusCode = responseEntity.getStatusCode();

                if (body == null)
                    return responseEntity;

                resolveExceptionMessage(e, body);

                HttpHeaders headers = new HttpHeaders();
                headers.setAll(responseEntity.getHeaders().toSingleValueMap());
                return new ResponseEntity<>(body, headers, statusCode);
            }
        };
    }

    public void resolveExceptionMessage(Exception e, OAuth2Exception body) {
        String errorCode = StringUtils.join(e.getClass().getSimpleName(), DOT, e.getMessage()).replace(WHITESPACE, UNDERSCORE);
        Optional<String> i18n = exceptionHandler.getLocaleMessage(errorCode, getExceptionArgs(body.getCause()), LocaleContextHolder.getLocale());

        body.addAdditionalInformation("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        body.addAdditionalInformation("code", i18n.isPresent() ? e.getMessage() : "");
        body.addAdditionalInformation("status", String.valueOf(body.getHttpErrorCode()));
        body.addAdditionalInformation("message", i18n.orElse(body.getMessage()));
        body.addAdditionalInformation("type", processExceptionSimpleName(e.getClass().getSimpleName()));
        body.addAdditionalInformation("path", ENDPOINT_OAUTH);
    }

    private String[] getExceptionArgs(Throwable cause) {
        String[] args = null;
        if (cause != null) {
            try {
                Method method = cause.getClass().getMethod("getArgs");
                args = (String[]) method.invoke(cause);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.debug(e.getMessage());
            }
        }
        return args;
    }

}
