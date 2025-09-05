package com.capacidad.validationapi.module.importprocessor.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.importprocessor.model.ImportObject;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.render.service.impl.CSVReaderWrapper;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService<T extends ImportObject> {

    void importMultipartFile(ImportProperties properties) throws ObjectNotValidException;

    void validateFile(MultipartFile file) throws ObjectNotValidException;

    void validateImportObject(T importObject) throws ObjectNotValidException;

    CSVReaderWrapper<T> buildCsvReader(ImportProperties properties);

}
