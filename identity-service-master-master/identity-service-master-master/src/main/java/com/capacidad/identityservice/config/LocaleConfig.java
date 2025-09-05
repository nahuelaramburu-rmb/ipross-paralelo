package com.capacidad.identityservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Collections;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    private final MessageSource messageSource;

    @Autowired
    public LocaleConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    //Only "es" is enabled
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        Locale es = new Locale("es");
        localeResolver.setSupportedLocales(Collections.singletonList(es));
        localeResolver.setDefaultLocale(es);
        return localeResolver;
    }

    @Bean(name = "customLocaleValidator")
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

}
