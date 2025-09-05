package com.capacidad.validationapi.module.premedicalauthorization.config.security;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PreMedicalAuthorizationChecker {

    private final PreMedicalAuthorizationService preMedicalAuthorizationService;

    @Autowired
    public PreMedicalAuthorizationChecker(PreMedicalAuthorizationService preMedicalAuthorizationService) {
        this.preMedicalAuthorizationService = preMedicalAuthorizationService;
    }

    public boolean hasAccessToPreMedicalAuthorization(long preMedicalAuthorizationId) {
        if (SecurityUtils.isBeneficiary())
            return preMedicalAuthorizationService.existsByAuthBeneficiaryOrRelative(preMedicalAuthorizationId);
        return true;
    }

    public boolean hasAccessToPreMedicalAuthorization(String code) {
        if (SecurityUtils.isBeneficiary())
            return preMedicalAuthorizationService.existsByAuthBeneficiaryOrRelative(code);
        return true;
    }

}
