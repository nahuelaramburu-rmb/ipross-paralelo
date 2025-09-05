package com.capacidad.identityservice.exception;

import com.capacidad.utils.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.capacidad.identityservice.misc.Utils.processExceptionSimpleName;
import static com.capacidad.identityservice.misc.constant.ApplicationConstants.DOT;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String LOG_ERROR_MESSAGE_TEMPLATE = "({}) - exception: {} - detail: {}";
    private static final String SQL_STATE_UNIQUE_CONSTRAINT = "23505";
    private static final String SQL_STATE_NOT_NULL_CONSTRAINT = "23502";
    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleDatabaseException(HttpServletRequest request, ConstraintViolationException ex) {
        log.error(LOG_ERROR_MESSAGE_TEMPLATE, ex.getCause().getMessage(), ex.getClass(), ex.getMessage());
        if (StringUtils.equals(ex.getSQLState(), SQL_STATE_UNIQUE_CONSTRAINT)) {
            String errorKey = "database.uniqueConstraint";
            String fields = parseAndTranslateConstraintErrorFields(ex.getCause().getMessage(), "(?s)(?<=Detail: Key \\().*?(?=\\))");
            log.error("Fields in conflict: {}", fields);
            return handleObjectAlreadyExistsException(request, new ObjectAlreadyExistsException(errorKey, fields));
        }
        if (StringUtils.equals(ex.getSQLState(), SQL_STATE_NOT_NULL_CONSTRAINT)) {
            String fields = parseAndTranslateConstraintErrorFields(ex.getCause().getMessage(), "(?<=null value in column \")(.*?)(?=\" violates not-null constraint)");
            log.error("Fields in conflict: {}", fields);
            return handleObjectNotValidException(request, new ObjectNotValidException("database.notNullConstraint"));
        }
        return handleObjectNotValidException(request, new ObjectNotValidException("database.statementError"));
    }

    private String parseAndTranslateConstraintErrorFields(String errorMessage, String pattern) {
        Pattern fieldPattern = Pattern.compile(pattern);
        Matcher matcher = fieldPattern.matcher(errorMessage);
        if (matcher.find()) {
            return getLocaleMessage(matcher.group(), null, LocaleContextHolder.getLocale())
                    .orElse(matcher.group());
        }
        return null;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        HttpServletRequest req = ((HttpServletRequest) ((ServletWebRequest) request).getNativeRequest());
        List<FieldError> errorList = ex.getBindingResult().getFieldErrors();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (FieldError fieldError : errorList) {
            sb.append(fieldError.getField());
            sb.append(" - ");
            sb.append(fieldError.getDefaultMessage());
            if (i + 1 != errorList.size())
                sb.append(" | ");
            i++;
        }
        String message = sb.toString();
        log.error(LOG_ERROR_MESSAGE_TEMPLATE, message, ex.getClass(), ex.getMessage());
        return buildErrorResponse(ObjectNotValidException.class.getSimpleName(), "", message, status, req.getRequestURI());
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<Object> handleObjectNotFoundException(HttpServletRequest request, ObjectNotFoundException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.NOT_FOUND, ex.getArgs());
    }

    @ExceptionHandler(ObjectNotValidException.class)
    public ResponseEntity<Object> handleObjectNotValidException(HttpServletRequest request, ObjectNotValidException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.BAD_REQUEST, ex.getArgs());
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    public ResponseEntity<Object> handleObjectAlreadyExistsException(HttpServletRequest request, ObjectAlreadyExistsException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.CONFLICT, ex.getArgs());
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<Object> handleInsufficientAuthenticationException(HttpServletRequest request, InsufficientAuthenticationException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.FORBIDDEN, null);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(HttpServletRequest request, InvalidTokenException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.UNAUTHORIZED, ex.getArgs());
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<Object> handleExpiredTokenException(HttpServletRequest request, ExpiredTokenException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.UNAUTHORIZED, ex.getArgs());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(HttpServletRequest request, BadCredentialsException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.UNAUTHORIZED, null);
    }

    @ExceptionHandler(InvalidUserStateException.class)
    public ResponseEntity<Object> handleInvalidUserStateException(HttpServletRequest request, InvalidUserStateException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.UNAUTHORIZED, ex.getArgs());
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<Object> handleInvalidSessionException(HttpServletRequest request, InvalidSessionException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.UNAUTHORIZED, ex.getArgs());
    }

    @ExceptionHandler(TokenSigningException.class)
    public ResponseEntity<Object> handleTokenSigningException(HttpServletRequest request, TokenSigningException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getArgs());
    }

    @ExceptionHandler(UnspecifiedTenantException.class)
    public ResponseEntity<Object> handleUnspecifiedTenantException(HttpServletRequest request, UnspecifiedTenantException ex) {
        return buildErrorResponse(ex.getClass().getSimpleName(), ex.getMessage(), "", HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(HttpServletRequest request, AuthenticationException ex) {
        String message = "Authentication Failed";
        log.error(LOG_ERROR_MESSAGE_TEMPLATE, message, ex.getClass(), ex.getMessage());
        return buildErrorResponse(ex.getClass().getSimpleName(), message, "", HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    private ResponseEntity<Object> buildGenericErrorResponse(HttpServletRequest request, Exception ex, HttpStatus httpStatus, String[] args) {
        String errorCode = StringUtils.deleteWhitespace(StringUtils.join(ex.getClass().getSimpleName(), DOT, ex.getMessage()));
        String enMessage = getLocaleMessage(errorCode, args, Locale.US).orElse(ex.getMessage());
        String i18nMessage = getLocaleMessage(errorCode, args, LocaleContextHolder.getLocale()).orElse(enMessage);
        log.error(LOG_ERROR_MESSAGE_TEMPLATE, enMessage, ex.getClass(), ex.getMessage());
        return buildErrorResponse(ex.getClass().getSimpleName(), i18nMessage, ex.getMessage(), httpStatus, request.getRequestURI());
    }

    private ResponseEntity<Object> buildErrorResponse(String type, String message, String code, HttpStatus httpStatus, String requestUri) {
        ApiError apiError = new ApiError(httpStatus.value(), message, processExceptionSimpleName(type), requestUri, code);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(apiError.getJsonObject(), headers, httpStatus);
    }

    public Optional<String> getLocaleMessage(String message, String[] args, Locale locale) {
        try {
            return Optional.of(messageSource.getMessage(message, args, locale));
        } catch (NoSuchMessageException e) {
            log.debug("Locale message not found: {}", e.getMessage());
        }
        return Optional.empty();
    }

}
