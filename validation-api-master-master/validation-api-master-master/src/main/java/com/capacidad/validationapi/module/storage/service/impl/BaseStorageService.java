package com.capacidad.validationapi.module.storage.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.storage.service.StorageService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Getter
public abstract class BaseStorageService extends BaseFileValidator implements StorageService {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public void validate(MultipartFile file) throws ObjectNotValidException {
        super.validate(file, getApplicationProperties().getFileStorageExtensionList(), getApplicationProperties().getFileStorageSignaturesList());
    }


}
