package com.capacidad.validationapi.module.medicalcenter.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalcenter.dto.MedicalCenterDTO;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.projection.MedicalCenterProjection;

import java.util.Set;

public interface MedicalCenterService extends BaseService<MedicalCenter, MedicalCenterDTO, Long> {

    Set<MedicalCenterProjection.IdNameAndAddressProjection> getPractitionerMedicalCenters(long practitionerId);

    boolean authMedicalCenterContainsPractitioner(long practitionerId);

    Set<MedicalCenterProjection> getMedicalCenters(String name);

    MedicalCenter getAuthMedicalCenter() throws ObjectNotFoundException;

    MedicalCenterProjection getProjectedAuthMedicalCenter() throws ObjectNotFoundException;

}