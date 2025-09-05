package com.capacidad.validationapi.module.properties.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.properties.dto.PropertiesDTO;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.projection.PropertiesProjection;

import javax.persistence.EntityManager;
import java.util.Map;

public interface PropertiesService extends BaseService<Properties, PropertiesDTO, Long> {

    PropertiesProjection getPropertiesProjection();

    Properties getProperties();

    Properties getPropertiesTypedQuery(EntityManager entityManager);

    PropertiesProjection update(Map<String, Object> update) throws ObjectNotValidException, ObjectNotFoundException;

    ApplicationProperties getApplicationProperties();

}
