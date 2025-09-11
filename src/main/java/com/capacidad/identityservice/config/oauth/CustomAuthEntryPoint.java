package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.capacidad.identityservice.misc.Utils.processExceptionSimpleName;
import static com.capacidad.identityservice.misc.constant.ApplicationConstants.*;
import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.ENDPOINT_OAUTH;

@Log4j2
@Configuration
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final GlobalExceptionHandler exceptionHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public CustomAuthEntryPoint(GlobalExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));


        String errorCode = StringUtils.join(
                authException.getClass().getSimpleName(),
                DOT,
                authException.getMessage()
        ).replace(WHITESPACE, UNDERSCORE);

        Optional<String> i18n = exceptionHandler.getLocaleMessage(
                errorCode,
                null,
                LocaleContextHolder.getLocale()
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toEpochMilli());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put("message", i18n.orElse(authException.getMessage()));
        body.put("type", processExceptionSimpleName(authException.getClass().getSimpleName()));
        body.put("path", ENDPOINT_OAUTH);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body));

        objectMapper.writeValue(response.getOutputStream(), body);

        log.warn("Authentication failed: {}", body);
    }
}
