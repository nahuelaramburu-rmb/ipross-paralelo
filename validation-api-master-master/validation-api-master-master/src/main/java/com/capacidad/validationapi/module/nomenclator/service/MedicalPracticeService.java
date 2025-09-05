package com.capacidad.validationapi.module.nomenclator.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.nomenclator.dto.MedicalPracticeDTO;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.projection.MedicalPracticeProjection;

import java.util.List;

public interface MedicalPracticeService extends BaseService<MedicalPractice, MedicalPracticeDTO, Long> {

    List<IdAndNameOnlyProjection> getMedicalPractices(String name);

    List<IdAndNameOnlyProjection> getAllMedicalPracticeTypes();

    List<IdAndNameOnlyProjection> getAllMedicalPracticeAreas();

    MedicalPracticeProjection getMedicalPractice(Long medicalPracticeId) throws ObjectNotFoundException;

    MedicalPracticeProjection createNewPractice(MedicalPracticeDTO medicalPracticeDTO);

}
