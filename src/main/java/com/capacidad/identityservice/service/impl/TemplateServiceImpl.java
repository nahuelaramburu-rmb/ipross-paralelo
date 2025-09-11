package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static com.capacidad.identityservice.misc.constant.TemplateContants.*;

@Service
public class TemplateServiceImpl implements TemplateService {

    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public TemplateServiceImpl(TemplateEngine templateEngine,
                               MessageSource messageSource,
                               ApplicationProperties applicationProperties) {
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String prepareRestorePasswordEmail(ApplicationUser user) {
        Map<String, String> templateValues = new HashMap<>();
        templateValues.put(TEMPLATE_NAME, user.getProfile().getName());
        templateValues.put(TEMPLATE_LASTNAME, user.getProfile().getLastName());
        templateValues.put(TEMPLATE_RESTORE_OTP, user.getRestoreOtp().toString());
        templateValues.put(TEMPLATE_VERIFICATION_WINDOW, String.valueOf(applicationProperties.getVerificationWindow()));
        return prepareTemplate(templateValues, "restore-password");
    }

    @Override
    public String prepareVerificationEmail(ApplicationUser user, String tenantName) {
        Map<String, String> templateValues = new HashMap<>();
        templateValues.put(TEMPLATE_NAME, user.getProfile().getName());
        templateValues.put(TEMPLATE_LASTNAME, user.getProfile().getLastName());
        templateValues.put(TEMPLATE_TENANT_NAME, tenantName);
        templateValues.put(TEMPLATE_CONTEXT_PATH, ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
        templateValues.put(TEMPLATE_VERFICATION_OTP, user.getVerificationOtp().toString());
        templateValues.put(TEMPLATE_SUB, user.getSub().toString());
        templateValues.put(TEMPLATE_VERIFICATION_WINDOW, String.valueOf(applicationProperties.getVerificationWindow()));
        return prepareTemplate(templateValues, "verification");
    }

    @Override
    public String prepareConfirmationEmail(ApplicationUser user, String tenantName) {
        Map<String, String> templateValues = new HashMap<>();
        templateValues.put(TEMPLATE_NAME, user.getProfile().getName());
        templateValues.put(TEMPLATE_LASTNAME, user.getProfile().getLastName());
        templateValues.put(TEMPLATE_TENANT_NAME, tenantName);
        templateValues.put(TEMPLATE_TEMPORARY_PASSWORD, user.getTemporaryPassword());
        return prepareTemplate(templateValues, "welcome");
    }

    @Override
    public String prepareTemplate(Map<String, String> values, String templateName) {
        Context context = new Context();
        values.forEach(context::setVariable);
        return templateEngine.process(templateName, context);
    }

    @Override
    public String getLocaleTitle(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
