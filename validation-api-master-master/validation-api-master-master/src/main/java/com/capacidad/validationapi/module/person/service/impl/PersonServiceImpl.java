package com.capacidad.validationapi.module.person.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.model.MaritalStatus;
import com.capacidad.validationapi.module.person.projection.IdTypeProjection;
import com.capacidad.validationapi.module.person.repository.IdTypeRepository;
import com.capacidad.validationapi.module.person.repository.MaritalStatusRepository;
import com.capacidad.validationapi.module.person.repository.OccupationRepository;
import com.capacidad.validationapi.module.person.repository.StudiesRepository;
import com.capacidad.validationapi.module.person.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {

    private final StudiesRepository studiesRepository;
    private final MaritalStatusRepository maritalStatusRepository;
    private final IdTypeRepository idTypeRepository;
    private final OccupationRepository occupationRepository;

    @Autowired
    public PersonServiceImpl(StudiesRepository studiesRepository,
                             MaritalStatusRepository maritalStatusRepository,
                             IdTypeRepository idTypeRepository,
                             OccupationRepository occupationRepository) {
        this.studiesRepository = studiesRepository;
        this.maritalStatusRepository = maritalStatusRepository;
        this.idTypeRepository = idTypeRepository;
        this.occupationRepository = occupationRepository;
    }


    @Override
    public List<IdAndNameOnlyProjection> getAllStudies() {
        return studiesRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllMaritalStatus() {
        return maritalStatusRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public List<MaritalStatus> getAllMaritalStatusEntities() {
        return maritalStatusRepository.findAll();
    }

    @Override
    public List<IdTypeProjection> getAllIdType() {
        return idTypeRepository.findAllProjectedBy(IdTypeProjection.class);
    }

    @Override
    public List<IdType> getAllIdTypeEntities() {
        return idTypeRepository.findAll();
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllOccupations() {
        return occupationRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public IdType getIdType(String name) throws ObjectNotFoundException {
        return idTypeRepository
                .findByNameOrAliasIgnoreCase(name, name)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "person.idTypeNotFound",
                        String.valueOf(name)));
    }

}
