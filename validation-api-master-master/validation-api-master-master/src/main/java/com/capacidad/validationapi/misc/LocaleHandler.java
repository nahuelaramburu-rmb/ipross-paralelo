package com.capacidad.validationapi.misc;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@Log4j2
public class LocaleHandler {

    private final MessageSource messageSource;

    @Autowired
    public LocaleHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public Optional<String> getLocaleMessage(String message, Locale locale, String... args) {
        try {
            return Optional.of(messageSource.getMessage(message, args, locale));
        } catch (NoSuchMessageException e) {
            log.debug("Locale message not found: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<String> getLocaleMessage(String message, String... args) {
        try {
            return Optional.of(messageSource.getMessage(message, args, LocaleContextHolder.getLocale()));
        } catch (NoSuchMessageException e) {
            log.debug("Locale message not found: {}", e.getMessage());
        }
        return Optional.empty();
    }

}
