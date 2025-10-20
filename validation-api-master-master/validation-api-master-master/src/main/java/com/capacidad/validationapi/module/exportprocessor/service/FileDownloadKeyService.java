package com.capacidad.validationapi.module.exportprocessor.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;

import javax.persistence.EntityManager;

public interface FileDownloadKeyService {

    FileDownloadKey generateDownloadKey(String origin);

    FileDownloadKey findDownloadKeyTypedQuery(String key, String origin, EntityManager entityManager) throws ObjectNotFoundException, ObjectNotValidException;

    void deleteAllDownloadKeys();

}
