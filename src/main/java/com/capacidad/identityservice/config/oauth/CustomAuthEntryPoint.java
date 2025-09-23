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


/*
* es un manejador de errores de autenticación personalizado para Spring Security.
Se ejecuta cuando un usuario intenta acceder a un recurso protegido sin estar autenticado o con credenciales inválidas.
*
*Implementa AuthenticationEntryPoint: interfaz de Spring Security que define qué hacer cuando la autenticación falla.
*
*
* Intercepta los errores de autenticación de Spring Security.

    Devuelve una respuesta JSON estructurada en vez de la página HTML por defecto.
    Traduce el mensaje de error según el locale (i18n).
    Deja logs de advertencia cuando alguien falla en autenticarse
*
*
* cada vez que alguien no esté autenticado o mande un token inválido -> entra aquí y se devuelve un JSON con el detalle.
*
* */

@Log4j2
@Configuration
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    //se usa para obtener mensajes internacionalizados (i18n) de error según el locale.
    private final GlobalExceptionHandler exceptionHandler;

    //serializa objetos Java a JSON
    private final ObjectMapper objectMapper = new ObjectMapper();


    public CustomAuthEntryPoint(GlobalExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }


    // Este metodo se ejecuta cuando ocurre un error de autenticación
    // request: la petición que causó el error.
    // response: la respuesta que se enviará al cliente.
    // authException: la excepción que lanzó Spring Security.
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // los mensajes de error se devuelven en español
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        // construye un codigo de error
        // Usa el nombre de la excepción + el mensaje de la excepcion
        // este código se usará para buscar un mensaje traducido.
        String errorCode = StringUtils.join(
                authException.getClass().getSimpleName(),
                DOT,
                authException.getMessage()
        ).replace(WHITESPACE, UNDERSCORE);

        // Busca mensaje internacionalizado (i18n)
        //Si encuentra traducción -> usa esa
        //Si no ->  usa el mensaje de la excepción (authException.getMessage()).
        Optional<String> i18n = exceptionHandler.getLocaleMessage(
                errorCode,
                null,
                LocaleContextHolder.getLocale()
        );

        // se arma el cuerpo de la respuesta json
        // timestamp: momento en milisegundos.
        //status: 401.
        //error: texto estándar de HttpStatus.UNAUTHORIZED.
        //message: mensaje traducido o el original.
        //type: tipo simplificado de la excepción.
        //path: indica el endpoint fallido (fijo en ENDPOINT_OAUTH).
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toEpochMilli());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put("message", i18n.orElse(authException.getMessage()));
        body.put("type", processExceptionSimpleName(authException.getClass().getSimpleName()));
        body.put("path", ENDPOINT_OAUTH);

        // configura la respuesta http
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)); //Escribe el JSON en el response

        // serializa el objeto java a json
        objectMapper.writeValue(response.getWriter(), body);

        // registra en logs la información del error.
        log.warn("Authentication failed: {}", body);
    }
}
