package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionRestrictionDTO;
import com.capacidad.validationapi.module.prescription.model.PrescriptionRestriction;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionRestrictionRepository;
import com.capacidad.validationapi.module.prescription.service.PrescriptionRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class PrescriptionRestrictionServiceImpl extends BaseServiceImpl<PrescriptionRestriction, PrescriptionRestrictionDTO, Long> implements PrescriptionRestrictionService {

    private final PrescriptionRestrictionRepository prescriptionRestrictionRepository;

    @Autowired
    public PrescriptionRestrictionServiceImpl(PrescriptionRestrictionRepository prescriptionRestrictionRepository) {
        super(prescriptionRestrictionRepository);
        this.prescriptionRestrictionRepository = prescriptionRestrictionRepository;
    }

    @Override
    public void validateSpecialties(Collection<MedicalSpecialty> specialties) throws ObjectNotValidException {
        int restrictionCount = prescriptionRestrictionRepository
                .countAllByMedicalSpecialtyIn(specialties);
        if (specialties.size() == restrictionCount)
            throw new ObjectNotValidException("prescriptionRestriction.invalidSpecialty");
    }
}
