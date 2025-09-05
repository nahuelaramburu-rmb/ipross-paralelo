package com.capacidad.validationapi.module.importprocessor.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.exception.GlobalExceptionHandler;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.importprocessor.service.ImportErrorHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Log4j2
public class ImportErrorHandlerImpl implements ImportErrorHandler {

    private final LocaleHandler localeHandler;
    private final GlobalExceptionHandler exceptionHandler;

    public ImportErrorHandlerImpl(LocaleHandler localeHandler,
                                  GlobalExceptionHandler exceptionHandler) {
        this.localeHandler = localeHandler;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public String handleImportError(Exception e) {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        String message = localeHandler.getLocaleMessage("import.genericErrorMessage").orElse("Error");
        if (e instanceof ObjectNotValidException) {
            ObjectNotValidException exception = (ObjectNotValidException) e;
            message = exceptionHandler.resolveErrorMessage(exception, exception.getArgs());
        }
        if (e instanceof ObjectNotFoundException) {
            ObjectNotFoundException exception = (ObjectNotFoundException) e;
            message = exceptionHandler.resolveErrorMessage(exception, exception.getArgs());
        }
        return message;
    }

}
