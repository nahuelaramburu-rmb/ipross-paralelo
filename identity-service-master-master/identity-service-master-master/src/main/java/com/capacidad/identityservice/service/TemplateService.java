package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.ApplicationUser;

import java.util.Map;

public interface TemplateService {

    String prepareRestorePasswordEmail(ApplicationUser user);

    String prepareVerificationEmail(ApplicationUser user, String tenantName);

    String prepareConfirmationEmail(ApplicationUser user, String tenantName);

    String prepareTemplate(Map<String, String> values, String templateName);

    String getLocaleTitle(String code);

}
