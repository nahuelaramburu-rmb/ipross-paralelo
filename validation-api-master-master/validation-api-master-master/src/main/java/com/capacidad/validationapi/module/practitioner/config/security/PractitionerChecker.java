package com.capacidad.validationapi.module.practitioner.config.security;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.practitioner.service.MedicalRegistrationService;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class PractitionerChecker {

    private final PractitionerService practitionerService;
    private final MedicalCenterService medicalCenterService;
    private final MedicalRegistrationService medicalRegistrationService;

    @Autowired
    public PractitionerChecker(PractitionerService practitionerService,
                               MedicalCenterService medicalCenterService,
                               MedicalRegistrationService medicalRegistrationService) {
        this.practitionerService = practitionerService;
        this.medicalCenterService = medicalCenterService;
        this.medicalRegistrationService = medicalRegistrationService;
    }

    public boolean canGetAll() {
        return !SecurityUtils.isPractitioner();
    }

    public boolean hasAccessToPractitioner(long practitionerId) {
        if (SecurityUtils.isPractitioner())
            return practitionerService.existsByAuthentication(practitionerId);
        if (SecurityUtils.isMedicalCenter())
            return medicalCenterService.authMedicalCenterContainsPractitioner(practitionerId);
        if (SecurityUtils.isOrganization())
            return medicalRegistrationService.practitionerBelongsToAuthOrganization(practitionerId);
        return SecurityUtils.isHighRankingAuthority();
    }

    public boolean hasAccessToMedicalRegistration(long medicalRegistrationId) throws ObjectNotFoundException {
        BaseProjection<Long> practitionerId = practitionerService.getMedicalRegistrationOwner(medicalRegistrationId);
        return hasAccessToPractitioner(practitionerId.getId());
    }

}
