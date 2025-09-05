package com.capacidad.validationapi.exception;

import com.capacidad.utils.exception.*;
import com.capacidad.validationapi.misc.LocaleHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String LOG_ERROR_MESSAGE_TEMPLATE = "({}) - exception: {} - detail: {}";
    private static final String SQL_STATE_UNIQUE_CONSTRAINT = "23505";
    private static final String SQL_STATE_NOT_NULL_CONSTRAINT = "23502";
    private static final String SQL_STATE_FK_REFERENCE_CONSTRAINT = "23503";
    private final LocaleHandler localeHandler;

    public GlobalExceptionHandler(LocaleHandler localeHandler) {
        this.localeHandler = localeHandler;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(HttpServletRequest request, DataIntegrityViolationException ex) {
        SQLException sqlException = (SQLException) ex.getRootCause();
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(ex.getMessage(), sqlException, "dataIntegrityViolation");
        return handleConstraintViolationException(request, constraintViolationException);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException ex) {
        log.debug(LOG_ERROR_MESSAGE_TEMPLATE, ex.getCause().getMessage(), ex.getClass(), ex.getMessage());
        if (StringUtils.equals(ex.getSQLState(), SQL_STATE_UNIQUE_CONSTRAINT)) {
            String errorKey = "database.uniqueConstraint";
            String removedFields = removeExtraFields(ex.getCause().getMessage());
            String fields = parseAndTranslateConstraintErrorFields(removedFields, "(?s)(?<=Detail: Key \\().*?(?=\\))");
            log.debug("Fields in conflict: {}", removedFields);
            return handleObjectAlreadyExistsException(request, new ObjectAlreadyExistsException(errorKey, fields));
        }
        if (StringUtils.equals(ex.getSQLState(), SQL_STATE_NOT_NULL_CONSTRAINT)) {
            String fields = parseAndTranslateConstraintErrorFields(ex.getCause().getMessage(), "(?<=null value in column \")(.*?)(?=\" violates not-null constraint)");
            log.debug("Fields in conflict: {}", fields);
            return handleObjectNotValidException(request, new ObjectNotValidException("database.notNullConstraint", fields));
        }
        if (StringUtils.equals(ex.getSQLState(), SQL_STATE_FK_REFERENCE_CONSTRAINT)) {
            if (StringUtils.contains(ex.getCause().getMessage(), "is not present in table"))
                return handleObjectNotValidException(request, new ObjectNotValidException("database.fkExistenceConstraint"));
            return handleObjectNotValidException(request, new ObjectNotValidException("database.fkReferenceConstraint"));
        }
        return handleObjectNotValidException(request, new ObjectNotValidException("database.statementError"));
    }

    private String parseAndTranslateConstraintErrorFields(String errorMessage, String pattern) {
        Pattern fieldPattern = Pattern.compile(pattern);
        Matcher matcher = fieldPattern.matcher(errorMessage);
        if (matcher.find()) {
            String replaced = matcher.group().replace(", ", DASH);
            return localeHandler.getLocaleMessage(replaced, LocaleContextHolder.getLocale())
                    .orElse(matcher.group());
        }
        return null;
    }

    private String removeExtraFields(String message) {
        Pattern removeFields = Pattern.compile("((,?\\s)|(,?))(tenant_id|deletion_token|deleted)((,?\\s)|(,?))");
        Matcher removeMatcher = removeFields.matcher(message);
        if (removeMatcher.find())
            message = removeMatcher.replaceAll("");
        return message;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        HttpServletRequest req = ((HttpServletRequest) ((ServletWebRequest) request).getNativeRequest());
        String message = "Malformed Request JSON";
        log.debug(LOG_ERROR_MESSAGE_TEMPLATE, message, ex.getClass(), ex.getMessage());
        ObjectNotValidException exception = new ObjectNotValidException("malformedRequestJSON");
        return buildGenericErrorResponse(req, exception, HttpStatus.BAD_REQUEST, exception.getArgs());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException ex) {
        String message = "Malformed Request Arguments";
        log.debug(LOG_ERROR_MESSAGE_TEMPLATE, message, ex.getClass(), ex.getMessage());
        ObjectNotValidException exception = new ObjectNotValidException("malformedRequestArguments");
        return buildGenericErrorResponse(request, exception, HttpStatus.BAD_REQUEST, exception.getArgs());
    }

    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public ResponseEntity<Object> handleUnsatisfiedServletRequestParameterException(HttpServletRequest request, UnsatisfiedServletRequestParameterException ex) {
        String message = "Unsatisfied Request Parameters";
        log.debug(LOG_ERROR_MESSAGE_TEMPLATE, message, ex.getClass(), ex.getMessage());
        ObjectNotValidException exception = new ObjectNotValidException("unsatisfiedRequestParameters");
        return buildGenericErrorResponse(request, exception, HttpStatus.BAD_REQUEST, exception.getArgs());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        HttpServletRequest req = ((HttpServletRequest) ((ServletWebRequest) request).getNativeRequest());
        List<FieldError> errorList = ex.getBindingResult().getFieldErrors();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (FieldError fieldError : errorList) {
            sb.append(resolveMessage(fieldError.getField(), fieldError.getField(), null));
            sb.append(COLON);
            sb.append(WHITESPACE);
            sb.append(fieldError.getDefaultMessage());
            if (i + 1 != errorList.size())
                sb.append(", ");
            i++;
        }
        String message = sb.toString();
        log.debug(LOG_ERROR_MESSAGE_TEMPLATE, ex.getClass(), ex.getMessage(), message);
        return buildErrorResponse(ObjectNotValidException.class.getSimpleName(), message, "", status, req.getRequestURI());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(HttpServletRequest request, InvalidTokenException ex) {
        return buildGenericErrorResponse(request, ex, HttpStatus.UNAUTHORIZED, ex.getArgs());
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

    private ResponseEntity<Object> buildGenericErrorResponse(HttpServletRequest request, Exception ex, HttpStatus httpStatus, String[] args) {
        String errorMessage = resolveErrorMessage(ex, args);
        String requestURI = Optional.ofNullable(request).map(HttpServletRequest::getRequestURI).orElse("Unresolved Request URI");
        log.debug(LOG_ERROR_MESSAGE_TEMPLATE, ex.getClass(), ex.getMessage(), errorMessage);
        return buildErrorResponse(ex.getClass().getSimpleName(), errorMessage, ex.getMessage(), httpStatus, requestURI);
    }

    public String resolveErrorMessage(Exception ex, String[] args) {
        String errorCode = StringUtils.join(ex.getClass().getSimpleName(), DOT, ex.getMessage());
        return resolveMessage(errorCode, ex.getMessage(), args);
    }

    private String resolveMessage(String key, String defaultMessage, String[] args) {
        String enMessage = localeHandler.getLocaleMessage(key, Locale.US, args).orElse(defaultMessage);
        return localeHandler.getLocaleMessage(key, LocaleContextHolder.getLocale(), args).orElse(enMessage);
    }

    private ResponseEntity<Object> buildErrorResponse(String type, String message, String code, HttpStatus httpStatus, String requestUri) {
        ApiError apiError = new ApiError(httpStatus.value(), message, processExceptionSimpleName(type), requestUri, code);
        return new ResponseEntity<>(apiError.getJsonObject(), httpStatus);
    }

    private String processExceptionSimpleName(String exceptionSimpleName) {
        return StringUtils.remove(exceptionSimpleName, "Exception");
    }

}
