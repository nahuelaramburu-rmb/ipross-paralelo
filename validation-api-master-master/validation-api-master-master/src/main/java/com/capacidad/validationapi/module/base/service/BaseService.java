package com.capacidad.validationapi.module.base.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.model.AuditLog;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BaseService<T extends BaseEntity<I>, R extends BaseDTO<I>, I extends Serializable> extends BaseServiceFinder<T, I> {

    T create(R dto) throws ObjectNotValidException, ObjectNotFoundException;

    T mapDtoToInput(R dto);

    PageRequest buildPageRequest(Pageable pageable);

    <P extends BaseProjection<I>> List<AuditLog<P, I>> auditLogs(Class<P> projectionType, I id);

    <P extends BaseProjection<I>> EntityModel<P> update(Map<String, Object> update, I objectId) throws ObjectNotFoundException, ObjectNotValidException;

    <P extends BaseProjection<I>> EntityModel<P> update(Map<String, Object> update, I objectId, Class<P> projectionClazz) throws ObjectNotFoundException, ObjectNotValidException;

    T inMemoryUpdate(Map<String, Object> update, I objectId) throws ObjectNotFoundException, ObjectNotValidException;

    JsonNode delete(I objectId) throws ObjectNotFoundException, ObjectNotValidException;

    void validate(T object) throws ObjectNotValidException, ObjectNotFoundException;

    void validateUpdate(T object) throws ObjectNotValidException, ObjectNotFoundException;

    T validateReference(I objectId) throws ObjectNotFoundException;

}
