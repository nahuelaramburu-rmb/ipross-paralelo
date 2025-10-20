package com.capacidad.validationapi.module.practitioner.service;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.projection.MedicalSpecialtyProjection;

import java.util.List;
import java.util.Set;

public interface MedicalSpecialtyService extends BaseService<MedicalSpecialty, IdDTO<Long>, Long> {

    Set<IdAndNameOnlyProjection> getPractitionerMedicalSpecialties(long practitionerId);

    List<IdAndNameOnlyProjection> getMedicalSpecialties(long medicalSpecialtyTypeId);

    List<IdAndNameOnlyProjection> getAllMedicalSpecialties();

    Set<MedicalSpecialtyProjection.Full> findAllByName(String name);

    List<IdAndNameOnlyProjection> getAllMedicalSpecialtyTypes();

    Set<MedicalSpecialty> getMedicalPracticeSpecialties(long medicalPracticeId);

}
