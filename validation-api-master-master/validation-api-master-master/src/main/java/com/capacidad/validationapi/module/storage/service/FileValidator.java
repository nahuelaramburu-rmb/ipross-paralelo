package com.capacidad.validationapi.module.storage.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface FileValidator {

    void validate(MultipartFile file, Collection<String> extensionCollection, Collection<Integer> signatureCollection) throws ObjectNotValidException;

}
