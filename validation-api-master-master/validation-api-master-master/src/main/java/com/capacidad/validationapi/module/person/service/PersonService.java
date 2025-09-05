package com.capacidad.validationapi.module.person.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.model.MaritalStatus;
import com.capacidad.validationapi.module.person.projection.IdTypeProjection;

import java.util.List;

public interface PersonService {

    List<IdAndNameOnlyProjection> getAllStudies();

    List<IdAndNameOnlyProjection> getAllMaritalStatus();

    List<MaritalStatus> getAllMaritalStatusEntities();

    List<IdTypeProjection> getAllIdType();

    List<IdType> getAllIdTypeEntities();

    List<IdAndNameOnlyProjection> getAllOccupations();

    IdType getIdType(String name) throws ObjectNotFoundException;

}
