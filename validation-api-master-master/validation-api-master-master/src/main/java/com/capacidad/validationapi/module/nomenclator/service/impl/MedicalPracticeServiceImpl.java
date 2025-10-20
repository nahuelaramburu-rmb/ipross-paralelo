package com.capacidad.validationapi.module.nomenclator.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.nomenclator.dto.MedicalPracticeDTO;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.projection.MedicalPracticeProjection;
import com.capacidad.validationapi.module.nomenclator.repository.MedicalPracticeAreaRepository;
import com.capacidad.validationapi.module.nomenclator.repository.MedicalPracticeRepository;
import com.capacidad.validationapi.module.nomenclator.repository.MedicalPracticeTypeRepository;
import com.capacidad.validationapi.module.nomenclator.service.MedicalPracticeService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
public class MedicalPracticeServiceImpl extends BaseServiceImpl<MedicalPractice, MedicalPracticeDTO, Long> implements MedicalPracticeService {

    private final MedicalPracticeRepository medicalPracticeRepository;
    private final MedicalPracticeAreaRepository medicalPracticeAreaRepository;
    private final MedicalPracticeTypeRepository medicalPracticeTypeRepository;

    @Autowired
    public MedicalPracticeServiceImpl(MedicalPracticeRepository medicalPracticeRepository,
                                      MedicalPracticeAreaRepository medicalPracticeAreaRepository,
                                      MedicalPracticeTypeRepository medicalPracticeTypeRepository) {
        super(medicalPracticeRepository);
        this.medicalPracticeRepository = medicalPracticeRepository;
        this.medicalPracticeAreaRepository = medicalPracticeAreaRepository;
        this.medicalPracticeTypeRepository = medicalPracticeTypeRepository;
    }

    public MedicalPracticeProjection getMedicalPractice(Long medicalPracticeID) throws ObjectNotFoundException {
        Optional<MedicalPractice> option = medicalPracticeRepository.findById(medicalPracticeID);
        if (option.isEmpty())
            throw new ObjectNotFoundException("medicalPractice.notFound", medicalPracticeID.toString());
        return this.getProjectionFactory().createProjection(MedicalPracticeProjection.class, option.get());

    }

    @Override
    public List<IdAndNameOnlyProjection> getMedicalPractices(String name) {
        return medicalPracticeRepository
                .findAllProjectedByNameContainingIgnoreCase(name);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllMedicalPracticeAreas() {
        return medicalPracticeAreaRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllMedicalPracticeTypes() {
        return medicalPracticeTypeRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public MedicalPracticeProjection createNewPractice(MedicalPracticeDTO medicalPracticeDTO) {
        MedicalPractice practice = this.mapDtoToInput(medicalPracticeDTO);
        medicalPracticeRepository.save(practice);
        medicalPracticeRepository.refresh(practice);
        return this.getProjectionFactory().createProjection(MedicalPracticeProjection.class, practice);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long id) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalPractice practice = this.findById(id);
        for (Nomenclator nomenclator : practice.getNomenclators()) {
            if (nomenclator != null && !nomenclator.getDeleted())
                throw new ObjectNotValidException("medicalPractice.cannotDeleteNomenclatorAttached");
        }
        practice.getMedicalSpecialties().clear();
        practice.setDeleted(true);
        practice.setDeletionToken(UUID.randomUUID());
        this.getRepository().save(practice);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(practice, this.getRepository()));
        return buildIdResponse(id);
    }

}
