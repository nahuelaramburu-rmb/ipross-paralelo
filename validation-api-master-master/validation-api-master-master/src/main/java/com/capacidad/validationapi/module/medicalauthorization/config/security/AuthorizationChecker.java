package com.capacidad.validationapi.module.medicalauthorization.config.security;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationChecker {

    private final MedicalAuthorizationService medicalAuthorizationService;

    @Autowired
    public AuthorizationChecker(MedicalAuthorizationService medicalAuthorizationService) {
        this.medicalAuthorizationService = medicalAuthorizationService;
    }

    public boolean hasAccessToAuthorization(long authorizationId) throws ObjectNotFoundException {
        if (SecurityUtils.isBeneficiary())
            return medicalAuthorizationService.existsByAuthBeneficiaryOrRelative(authorizationId);
        if (SecurityUtils.isPractitioner())
            return medicalAuthorizationService.existsByAuthPractitioner(authorizationId);
        if (SecurityUtils.isMedicalCenter())
            return medicalAuthorizationService.existsByAuthMedicalCenter(authorizationId);
        if (SecurityUtils.isOrganization())
            return hasOrganizationAccessToAuthorization(authorizationId);
        return SecurityUtils.isHighRankingAuthority();
    }

    private boolean hasOrganizationAccessToAuthorization(long authorizationId) {
        try {
            return medicalAuthorizationService.existsByAuthOrganization(authorizationId);
        } catch (ObjectNotFoundException e) {
            return false;
        }
    }

    public boolean hasAccessToAuthorizationItem(long authorizationItemId) throws ObjectNotValidException, ObjectNotFoundException {
        return hasAccessToAuthorization(medicalAuthorizationService.getMedicalAuthorizationItemParentId(authorizationItemId));
    }

}
