package com.capacidad.validationapi.module.storage.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.storage.service.FileValidator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;

import static com.capacidad.validationapi.exception.GlobalExceptionHandler.LOG_ERROR_MESSAGE_TEMPLATE;

@Log4j2
public abstract class BaseFileValidator implements FileValidator {

    @Override
    public void validate(MultipartFile file, Collection<String> extensionCollection, Collection<Integer> signatureCollection) throws ObjectNotValidException {
        boolean valid = false;
        if (!file.isEmpty()) {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            if (extensionCollection.contains(extension)) {
                try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(file.getInputStream()))) {
                    int fileSignature = inputStream.readInt();
                    log.debug("File Signature: {}", fileSignature);
                    if (signatureCollection.contains(fileSignature) || signatureCollection.isEmpty())
                        valid = true;
                } catch (IOException e) {
                    log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
                }
            }
        }
        if (!valid)
            throw new ObjectNotValidException("generic.invalidFile");
    }

}
