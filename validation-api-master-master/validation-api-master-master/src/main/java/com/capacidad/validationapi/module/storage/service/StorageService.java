package com.capacidad.validationapi.module.storage.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.model.MultipartFileListWrapper;
import com.capacidad.validationapi.module.storage.model.MultipartFileWrapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {

    void storeFile(FileType type, FileDTO file, boolean async) throws ObjectNotValidException;

    void storeFile(FileType fileType, MultipartFileWrapper multipartFileWrapper, boolean async) throws ObjectNotValidException;

    void storeFile(FileType fileType, MultipartFileListWrapper multipartFileWrapper, boolean async) throws ObjectNotValidException;

    byte[] retrieveFileAsByteArray(FileType type, long relatedId, String filename) throws ObjectNotValidException;

    FileDTO retrieveFileAsDTO(FileType fileType, long relatedId, String filename) throws ObjectNotValidException;

    List<SummaryDTO> getFileList(FileType fileType, long relatedId) throws ObjectNotValidException;

    List<SummaryDTO> getFileList(List<FileType> fileType, long relatedId);

    void deleteFileSync(FileType fileType, long relatedId, String filename) throws ObjectNotValidException;

    void deleteFileAsync(FileType fileType, long relatedId, String filename) throws ObjectNotValidException;

    void deleteFilesSync(FileType fileType, long relatedId, List<String> filenames) throws ObjectNotValidException;

    void validate(MultipartFile file) throws ObjectNotValidException;

}
