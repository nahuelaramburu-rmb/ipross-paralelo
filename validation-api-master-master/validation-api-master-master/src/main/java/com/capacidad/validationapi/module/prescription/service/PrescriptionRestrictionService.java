package com.capacidad.validationapi.module.prescription.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionRestrictionDTO;
import com.capacidad.validationapi.module.prescription.model.PrescriptionRestriction;

import java.util.Collection;

public interface PrescriptionRestrictionService extends BaseService<PrescriptionRestriction, PrescriptionRestrictionDTO, Long> {

    void validateSpecialties(Collection<MedicalSpecialty> specialties) throws ObjectNotValidException;

}
