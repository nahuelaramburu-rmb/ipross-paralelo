package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.projection.MedicalSpecialtyProjection;
import com.capacidad.validationapi.module.practitioner.repository.MedicalSpecialtyRepository;
import com.capacidad.validationapi.module.practitioner.repository.MedicalSpecialtyTypeRepository;
import com.capacidad.validationapi.module.practitioner.service.MedicalSpecialtyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MedicalSpecialtyServiceImpl extends BaseServiceImpl<MedicalSpecialty, IdDTO<Long>, Long> implements MedicalSpecialtyService {

    private final MedicalSpecialtyRepository medicalSpecialtyRepository;
    private final MedicalSpecialtyTypeRepository medicalSpecialtyTypeRepository;

    @Autowired
    public MedicalSpecialtyServiceImpl(MedicalSpecialtyRepository repository,
                                       MedicalSpecialtyTypeRepository medicalSpecialtyTypeRepository) {
        super(repository);
        this.medicalSpecialtyRepository = repository;
        this.medicalSpecialtyTypeRepository = medicalSpecialtyTypeRepository;
    }

    @Override
    public Set<IdAndNameOnlyProjection> getPractitionerMedicalSpecialties(long practitionerId) {
        return medicalSpecialtyRepository.findAllProjectedByPractitionersId(practitionerId);
    }

    @Override
    public List<IdAndNameOnlyProjection> getMedicalSpecialties(long medicalSpecialtyTypeId) {
        return medicalSpecialtyRepository.findAllByMedicalSpecialtyTypeId(medicalSpecialtyTypeId);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllMedicalSpecialties() {
        return medicalSpecialtyRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public Set<MedicalSpecialtyProjection.Full> findAllByName(String name) {
        return medicalSpecialtyRepository.findAllProjectedByNameContainingIgnoreCase(name);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllMedicalSpecialtyTypes() {
        return medicalSpecialtyTypeRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public Set<MedicalSpecialty> getMedicalPracticeSpecialties(long medicalPracticeId) {
        return medicalSpecialtyRepository.findAllByMedicalPracticesId(medicalPracticeId);
    }

}
