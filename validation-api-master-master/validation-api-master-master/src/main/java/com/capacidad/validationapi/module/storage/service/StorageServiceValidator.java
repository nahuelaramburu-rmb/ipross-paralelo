package com.capacidad.validationapi.module.storage.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;

public interface StorageServiceValidator {

    void validateAttachment(long authorizationId) throws ObjectNotValidException, ObjectNotFoundException;

    void validateReport(long authorizationId) throws ObjectNotFoundException, ObjectNotValidException;

}
