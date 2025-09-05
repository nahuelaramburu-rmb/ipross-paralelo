package com.capacidad.validationapi.module.base.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Map;


public interface BaseController<E extends BaseDTO<I>, I extends Serializable> {

    ResponseEntity<Object> getOne(I objectId) throws ObjectNotFoundException, ObjectNotValidException;

    ResponseEntity<Object> getAll(int page, int size, String search) throws ObjectNotFoundException, ObjectNotValidException;

    ResponseEntity<Object> addOne(E input) throws ObjectNotValidException, ObjectNotFoundException;

    ResponseEntity<Object> updateOne(I objectId, Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException;

    ResponseEntity<Object> deleteOne(I objectId) throws ObjectNotFoundException, ObjectNotValidException;

    ResponseEntity<Object> getAuditLogs(I objectId);

}
