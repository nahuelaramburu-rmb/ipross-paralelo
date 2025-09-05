package com.capacidad.validationapi.module.storage.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.model.MultipartFileListWrapper;
import com.capacidad.validationapi.module.storage.model.MultipartFileWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.exception.GlobalExceptionHandler.LOG_ERROR_MESSAGE_TEMPLATE;
import static com.capacidad.validationapi.functional.ThrowingFunction.throwingFunction;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;

@Log4j2
@Service
public class StorageServiceImpl extends BaseStorageService {

    private static final String FILE_NOT_FOUND_ERROR_CODE = "storage.fileNotFoundOrCorrupted";
    private static final int MAX_FILENAME_LENGTH = 50;
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private final AmazonS3 amazonS3;
    private final TransferManager transferManager;

    @Autowired
    public StorageServiceImpl(AmazonS3 amazonS3,
                              TransferManager transferManager) {
        this.amazonS3 = amazonS3;
        this.transferManager = transferManager;
    }

    @Override
    public void storeFile(FileType fileType, FileDTO fileDTO, boolean async) throws ObjectNotValidException {
        uploadInputStream(fileDTO, buildBucketName(fileType), buildFilename(fileType, fileDTO.getRelatedId(), null), async);
    }

    @Override
    public void storeFile(FileType fileType, MultipartFileWrapper multipartFileWrapper, boolean async) throws ObjectNotValidException {
        MultipartFile multipartFile = multipartFileWrapper.getFile();
        long relatedId = multipartFileWrapper.getRelatedId();
        validateMultipartFile(multipartFile);
        uploadInputStream(multipartFile, buildBucketName(fileType), buildFilename(fileType, relatedId, multipartFile.getOriginalFilename()), async);
    }

    @Override
    public void storeFile(FileType fileType, MultipartFileListWrapper multipartFileWrapper, boolean async) throws ObjectNotValidException {
        List<MultipartFile> multipartFiles = multipartFileWrapper.getFiles();
        List<File> files = multipartFiles.stream()
                .map(throwingFunction(multipartFile -> {
                    validateMultipartFile(multipartFile);
                    return convertFile(fileType, multipartFile, multipartFileWrapper.getRelatedId());
                }))
                .collect(Collectors.toUnmodifiableList());
        uploadInputStream(files, buildBucketName(fileType), async);
    }

    private File convertFile(FileType fileType, MultipartFile multipartFile, long relatedId) throws ObjectNotValidException {
        try {
            String newFileName = buildFilename(fileType, relatedId, multipartFile.getOriginalFilename());
            File newFile = new File(StringUtils.join(TMP_DIR, SLASH, DOT, SLASH, newFileName));
            multipartFile.transferTo(newFile);
            return newFile;
        } catch (IOException e) {
            log.error("Error converting MultipartFile to File: {}", e.getMessage());
            throw new ObjectNotValidException("storage.errorProcessingFileList");
        }
    }

    @Override
    public byte[] retrieveFileAsByteArray(FileType fileType, long relatedId, String filename) throws ObjectNotValidException {
        return retrieveFileAsByteArray(buildBucketName(fileType), buildFilename(fileType, relatedId, filename));
    }

    @Override
    public FileDTO retrieveFileAsDTO(FileType fileType, long relatedId, String filename) throws ObjectNotValidException {
        return retrieveFileAsFileDTO(buildBucketName(fileType), buildFilename(fileType, relatedId, filename), relatedId, filename);
    }

    @Override
    public List<SummaryDTO> getFileList(FileType fileType, long relatedId) throws ObjectNotValidException {
        return getFileList(fileType, buildFilename(fileType, relatedId, null));
    }

    @Override
    public List<SummaryDTO> getFileList(List<FileType> fileType, long relatedId) {
        return fileType.stream()
                .map(throwingFunction(ft -> getFileList(ft, buildFilename(ft, relatedId, null))))
                .flatMap(List::stream)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void deleteFileSync(FileType fileType, long relatedId, String filename) throws ObjectNotValidException {
        deleteFile(fileType, relatedId, filename);
    }

    @Async
    @Override
    public void deleteFileAsync(FileType fileType, long relatedId, String filename) throws ObjectNotValidException {
        deleteFile(fileType, relatedId, filename);
    }

    @Override
    public void deleteFilesSync(FileType fileType, long relatedId, List<String> filenames) throws ObjectNotValidException {
        List<DeleteObjectsRequest.KeyVersion> keyVersions = filenames.stream()
                .map(f -> {
                    String keyName = StringUtils.join(fileType.getFolder(), SLASH, buildFilename(fileType, relatedId, f));
                    return new DeleteObjectsRequest.KeyVersion(keyName);
                })
                .collect(Collectors.toUnmodifiableList());
        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(buildEnvContainer(fileType))
                    .withKeys(keyVersions)
                    .withQuiet(true);
            amazonS3.deleteObjects(deleteObjectsRequest);
        } catch (SdkClientException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException(FILE_NOT_FOUND_ERROR_CODE);
        }
    }

    private void deleteFile(FileType fileType, long relatedId, String filename) throws ObjectNotValidException {
        try {
            amazonS3.deleteObject(buildBucketName(fileType), buildFilename(fileType, relatedId, filename));
        } catch (SdkClientException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException(FILE_NOT_FOUND_ERROR_CODE);
        }
    }

    private List<SummaryDTO> getFileList(FileType fileType, String filePrefix) throws ObjectNotValidException {
        try {
            String envBucketName = buildEnvContainer(fileType);
            String prefix = StringUtils.join(fileType.getFolder(), SLASH, filePrefix);
            ListObjectsRequest req = new ListObjectsRequest().withBucketName(envBucketName).withPrefix(prefix);
            ObjectListing objectListing = amazonS3.listObjects(req);
            List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            return summaries.stream()
                    .map(summary -> {
                        SummaryDTO summaryDTO = new SummaryDTO();
                        summaryDTO.setName(StringUtils.remove(summary.getKey(), prefix));
                        summaryDTO.setSize(summary.getSize());
                        summaryDTO.setFileType(fileType.name());
                        return summaryDTO;
                    })
                    .collect(Collectors.toUnmodifiableList());
        } catch (SdkClientException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException("storage.invalidRelatedId");
        }
    }

    private void uploadInputStream(FileDTO file, String bucketName, String filename, boolean async) throws ObjectNotValidException {
        InputStream fileStream = new ByteArrayInputStream(file.getFile());
        ObjectMetadata objMetaData = new ObjectMetadata();
        objMetaData.setContentLength(file.getFile().length);
        if (file.getFileEncoding() != null)
            objMetaData.setContentType(file.getFileEncoding().getEncoding());
        uploadInputStreamToS3Bucket(bucketName, filename, fileStream, objMetaData, async);
    }

    private void uploadInputStream(MultipartFile multipartFile, String bucketName, String filename, boolean async) throws ObjectNotValidException {
        ObjectMetadata objMetaData = new ObjectMetadata();
        objMetaData.setContentLength(multipartFile.getSize());
        objMetaData.setContentType(multipartFile.getContentType());
        InputStream inputStream;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException("storage.multipartContentError");
        }
        uploadInputStreamToS3Bucket(bucketName, filename, inputStream, objMetaData, async);
    }

    private void uploadInputStream(List<File> files, String bucketName, boolean async) throws ObjectNotValidException {
        try {
            MultipleFileUpload upload = transferManager.uploadFileList(bucketName,
                    "", new File(String.join(SLASH, TMP_DIR, DOT)), files);
            if (!async)
                upload.waitForCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException("storage.fileUploadError");
        } catch (AmazonServiceException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException("storage.fileListUploadError");
        }
    }

    private void uploadInputStreamToS3Bucket(String bucketName, String filename, InputStream input, ObjectMetadata metadata, boolean async) throws ObjectNotValidException {
        try {
            Upload upload = transferManager.upload(new PutObjectRequest(bucketName, filename, input, metadata));
            if (!async)
                upload.waitForUploadResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException("storage.fileUploadError");
        } catch (SdkClientException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException("storage.fileListUploadError");
        }
    }

    private void validateMultipartFile(MultipartFile multipartFile) throws ObjectNotValidException {
        if (multipartFile.getOriginalFilename() == null || multipartFile.getOriginalFilename().length() > MAX_FILENAME_LENGTH)
            throw new ObjectNotValidException("storage.invalidFilename");
        this.validate(multipartFile);
    }

    private byte[] retrieveFileAsByteArray(String bucketName, String filename) throws ObjectNotValidException {
        File destinationFile = null;
        try {
            destinationFile = new File(StringUtils.join(TMP_DIR, SLASH, filename));
            Download download = transferManager.download(bucketName, filename, destinationFile);
            download.waitForCompletion();
            byte[] result = Files.readAllBytes(destinationFile.toPath());
            Files.delete(destinationFile.toPath());
            return result;
        } catch (InterruptedException e) {
            deleteTmpFile(destinationFile);
            Thread.currentThread().interrupt();
            log.debug(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException("storage.fileDownloadError");
        } catch (IOException | SdkClientException e) {
            deleteTmpFile(destinationFile);
            log.debug(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException(FILE_NOT_FOUND_ERROR_CODE);
        }
    }

    private void deleteTmpFile(File file) {
        if (file != null) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.debug(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            }
        }
    }

    private FileDTO retrieveFileAsFileDTO(String bucketName, String filePath, long relatedId, String filename) throws ObjectNotValidException {
        try {
            byte[] retrievedObject = retrieveFileAsByteArray(bucketName, filePath);
            FileDTO file = new FileDTO();
            file.setRelatedId(relatedId);
            file.setFile(retrievedObject);
            file.setFilename(filename);
            return file;
        } catch (SdkClientException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE, e.getMessage(), e.getClass(), "");
            throw new ObjectNotValidException(FILE_NOT_FOUND_ERROR_CODE);
        }
    }

    private String buildFilename(FileType fileType, Long relatedId, String extraData) {
        String filename = extraData != null ? extraData : "";
        return StringUtils.join(fileType.getFolder(), DASH, this.getApplicationProperties().getActiveProfile(), DASH, String.valueOf(relatedId), DASH, filename);
    }

    private String buildEnvContainer(FileType fileType) {
        return StringUtils.join(fileType.getContainer(), DASH, this.getApplicationProperties().getActiveProfile());
    }

    private String buildBucketName(FileType fileType) {
        String envContainerPrefix = StringUtils.join(buildEnvContainer(fileType), SLASH);
        return StringUtils.join(envContainerPrefix, fileType.getFolder());
    }

}
