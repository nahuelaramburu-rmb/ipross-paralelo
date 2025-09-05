package com.capacidad.validationapi.module.storage.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileEncoding;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.model.MultipartFileWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageServiceImplTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private TransferManager transferManager;

    @Mock
    private Upload upload;

    @Mock
    private Download download;

    @Spy
    @InjectMocks
    private StorageServiceImpl storageService;

    @Test
    public void testStoreFileSignaturePurposeThrowsObjectNotValidExceptionWhenAmazonS3ThrowsSdkException() {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setRelatedId(1L);
        fileDTO.setFile(new byte[0]);
        fileDTO.setFilename("filename");

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(transferManager.upload(any(PutObjectRequest.class))).thenThrow(new SdkClientException(""));

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.storeFile(FileType.SIGNATURE, fileDTO, false));

        assertThat(objectNotValidException).hasMessage("storage.fileListUploadError");
        assertThat(objectNotValidException.getStackTrace()[0].getMethodName()).isEqualTo("uploadInputStreamToS3Bucket");
    }

    @Test
    public void testStoreFileSignaturePurposeExecutesSuccessfullySyncWhenUploadIsValid() throws ObjectNotValidException, InterruptedException {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setRelatedId(1L);
        fileDTO.setFile(new byte[0]);
        fileDTO.setFilename("filename");
        fileDTO.setFileEncoding(FileEncoding.PNG);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(transferManager.upload(any(PutObjectRequest.class))).thenReturn(upload);

        storageService.storeFile(FileType.SIGNATURE, fileDTO, false);

        verify(transferManager, times(1)).upload(any(PutObjectRequest.class));
        verify(upload, times(1)).waitForUploadResult();
    }

    @Test
    public void testStoreFileSignaturePurposeExecutesSuccessfullyAsyncWhenUploadIsValid() throws ObjectNotValidException, InterruptedException {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setRelatedId(1L);
        fileDTO.setFile(new byte[0]);
        fileDTO.setFilename("filename");
        fileDTO.setFileEncoding(FileEncoding.PNG);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(transferManager.upload(any(PutObjectRequest.class))).thenReturn(upload);

        storageService.storeFile(FileType.SIGNATURE, fileDTO, true);

        verify(transferManager, times(1)).upload(any(PutObjectRequest.class));
        verify(upload, never()).waitForUploadResult();
    }

    @Test
    public void testStoreFileMultipartReportThrowsObjectNotValidExceptionWhenFilenameIsNull() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.getOriginalFilename()).thenReturn(null);
        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper();
        multipartFileWrapper.setFile(multipartFile);
        multipartFileWrapper.setRelatedId(1L);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.storeFile(FileType.REPORT, multipartFileWrapper, false));

        assertThat(objectNotValidException).hasMessage("storage.invalidFilename");
    }

    @Test
    public void testStoreFileMultipartReportThrowsObjectNotValidExceptionWhenFilenameIsTooLarge() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.getOriginalFilename()).thenReturn("large_filename_12345678912345678123456789123456789123456789" +
                "_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789.pdf");
        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper();
        multipartFileWrapper.setFile(multipartFile);
        multipartFileWrapper.setRelatedId(1L);


        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.storeFile(FileType.REPORT, multipartFileWrapper, false));

        assertThat(objectNotValidException).hasMessage("storage.invalidFilename");
    }

    @Test
    public void testStoreFileMultipartReportValidateThrowsObjectNotValidExceptionWhenFileIsEmpty() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.getOriginalFilename()).thenReturn("filename.pdf");
        when(multipartFile.isEmpty()).thenReturn(true);
        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper();
        multipartFileWrapper.setFile(multipartFile);
        multipartFileWrapper.setRelatedId(1L);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.storeFile(FileType.REPORT, multipartFileWrapper, false));

        assertThat(objectNotValidException).hasMessage("generic.invalidFile");
    }

    @Test
    public void testStoreFileMultipartReportValidateThrowsObjectNotValidExceptionWhenFileExtensionIsInvalid() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);

        when(multipartFile.getOriginalFilename()).thenReturn("filename.exe");
        when(multipartFile.isEmpty()).thenReturn(false);

        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper();
        multipartFileWrapper.setFile(multipartFile);
        multipartFileWrapper.setRelatedId(1L);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getFileStorageExtensionList()).thenReturn(Arrays.asList("docx", "doc", "pdf", "png", "jpeg", "jpg"));

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.storeFile(FileType.REPORT, multipartFileWrapper, false));

        assertThat(objectNotValidException).hasMessage("generic.invalidFile");
    }

    @Test
    public void testStoreFileMultipartReportValidateThrowsObjectNotValidExceptionWhenFileEOFException() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        byte[] fileData = "invalidSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);

        when(multipartFile.getOriginalFilename()).thenReturn("filename.pdf");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper();
        multipartFileWrapper.setFile(multipartFile);
        multipartFileWrapper.setRelatedId(1L);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getFileStorageExtensionList()).thenReturn(Arrays.asList("docx", "doc", "pdf", "png", "jpeg", "jpg"));
        when(applicationProperties.getFileStorageSignaturesList()).thenReturn(Collections.singletonList(11223344));

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.storeFile(FileType.REPORT, multipartFileWrapper, false));

        assertThat(objectNotValidException).hasMessage("generic.invalidFile");
    }

    @Test
    public void testStoreFileMultipartReportExecutesSuccessfullyWhenMultipartIsValid() throws IOException, ObjectNotValidException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);

        when(multipartFile.getOriginalFilename()).thenReturn("filename.docx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper();
        multipartFileWrapper.setFile(multipartFile);
        multipartFileWrapper.setRelatedId(1L);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getFileStorageExtensionList()).thenReturn(Arrays.asList("docx", "doc", "pdf", "png", "jpeg", "jpg"));
        when(applicationProperties.getFileStorageSignaturesList()).thenReturn(Arrays.asList(1347093252, -791735840, 626017350, -1991225785, -2555936, 1986096233));

        when(transferManager.upload(any(PutObjectRequest.class))).thenReturn(upload);

        storageService.storeFile(FileType.REPORT, multipartFileWrapper, false);

        verify(transferManager, times(1)).upload(any(PutObjectRequest.class));
    }

    @Test
    public void testStoreFileMultipartReportValidateThrowsObjectNotValidExceptionWhenFileIOException() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);

        when(multipartFile.getOriginalFilename()).thenReturn("filename.docx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getInputStream()).thenReturn(inputStream).thenThrow(new IOException(""));

        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper();
        multipartFileWrapper.setFile(multipartFile);
        multipartFileWrapper.setRelatedId(1L);

        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getFileStorageExtensionList()).thenReturn(Arrays.asList("docx", "doc", "pdf", "png", "jpeg", "jpg"));
        when(applicationProperties.getFileStorageSignaturesList()).thenReturn(Arrays.asList(1347093252, -791735840, 626017350, -1991225785, -2555936, 1986096233));

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.storeFile(FileType.REPORT, multipartFileWrapper, false));

        assertThat(objectNotValidException).hasMessage("storage.multipartContentError");
    }

    @Test
    public void testRetrieveFileAsByteArrayThrowsObjectNotValidExceptionWhenSdkException() {
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(transferManager.download(anyString(), anyString(), any(File.class))).thenThrow(new SdkClientException(""));

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.retrieveFileAsByteArray(FileType.SIGNATURE, 1L, "filename"));

        assertThat(objectNotValidException).hasMessage("storage.fileNotFoundOrCorrupted");
        assertThat(objectNotValidException.getStackTrace()[0].getMethodName()).isEqualTo("retrieveFileAsByteArray");
    }

    @Test
    public void testRetrieveFileAsByteArrayThrowsObjectNotValidExceptionWhenIOException() {
        when(transferManager.download(anyString(), anyString(), any(File.class))).thenReturn(download);
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.retrieveFileAsByteArray(FileType.SIGNATURE, 1L, "filename"));

        assertThat(objectNotValidException).hasMessage("storage.fileNotFoundOrCorrupted");
        assertThat(objectNotValidException.getStackTrace()[0].getMethodName()).isEqualTo("retrieveFileAsByteArray");
    }

    @Test
    public void testRetrieveFileAsFileDTOThrowsObjectNotValidExceptionWhenSdkException() {
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(transferManager.download(anyString(), anyString(), any(File.class))).thenThrow(new SdkClientException(""));

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.retrieveFileAsDTO(FileType.REPORT, 1L, "filename"));

        assertThat(objectNotValidException).hasMessage("storage.fileNotFoundOrCorrupted");
        assertThat(objectNotValidException.getStackTrace()[0].getMethodName()).isEqualTo("retrieveFileAsByteArray");
    }

    @Test
    public void testDeleteFileThrowsObjectNotValidExceptionWhenSdkException() {
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        doThrow(new SdkClientException("")).when(amazonS3).deleteObject(anyString(), anyString());

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> storageService.deleteFileSync(FileType.REPORT, 1L, "filename"));

        assertThat(objectNotValidException).hasMessage("storage.fileNotFoundOrCorrupted");
        assertThat(objectNotValidException.getStackTrace()[0].getMethodName()).isEqualTo("deleteFile");
    }

    @Test
    public void testDeleteFileExecutesSuccessfullyWhenValidFilePurposeAndFileData() throws ObjectNotValidException {
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        storageService.deleteFileSync(FileType.REPORT, 1L, "filename");

        verify(amazonS3, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void testGetFileListReturnsEmptyWhenNoS3Files() throws ObjectNotValidException {
        ObjectListing objectListing = mock(ObjectListing.class);

        when(amazonS3.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);
        when(objectListing.getObjectSummaries()).thenReturn(Collections.emptyList());
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getActiveProfile()).thenReturn("dev");

        List<SummaryDTO> summaries = storageService.getFileList(FileType.REPORT, 1L);

        assertThat(summaries.isEmpty()).isTrue();
    }

    @Test
    public void testGetFileListReturnsValidSummaryListWhenS3FilesExists() throws ObjectNotValidException {
        ObjectListing objectListing = mock(ObjectListing.class);
        S3ObjectSummary s3ObjectSummary = mock(S3ObjectSummary.class);

        List<S3ObjectSummary> s3ObjectSummaries = new ArrayList<>();
        s3ObjectSummaries.add(s3ObjectSummary);

        when(amazonS3.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);
        when(objectListing.getObjectSummaries()).thenReturn(s3ObjectSummaries);
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getActiveProfile()).thenReturn("dev");

        when(s3ObjectSummary.getKey()).thenReturn("reports/reports-dev-1-myFile.png");
        when(s3ObjectSummary.getSize()).thenReturn(12345L);

        List<SummaryDTO> summaries = storageService.getFileList(FileType.REPORT, 1L);

        assertThat(summaries.isEmpty()).isFalse();

        SummaryDTO result = summaries.get(0);

        assertThat(result.getName()).isEqualTo("myFile.png");
        assertThat(result.getSize()).isEqualTo(12345);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testGetFileListThrowsObjectNotValidExceptionWhenSdkClientException() throws ObjectNotValidException {
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getActiveProfile()).thenReturn("dev");
        when(amazonS3.listObjects(any(ListObjectsRequest.class))).thenThrow(new SdkClientException(""));

        storageService.getFileList(FileType.REPORT, 1L);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testDeleteFilesSyncThrowsExceptionWhenSdkException() throws ObjectNotValidException {
        var filesToRemove = new ArrayList<String>();
        filesToRemove.add("file1.pdf");
        filesToRemove.add("file2.jpg");

        when(amazonS3.deleteObjects(any(DeleteObjectsRequest.class))).thenThrow(new SdkClientException(""));
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);

        storageService.deleteFilesSync(FileType.BENEFICIARY_PROCEDURE, 1L, filesToRemove);
    }

    @Test
    public void testDeleteFilesSyncDoNotFailsWhenValidFileNames() throws ObjectNotValidException {
        var filesToRemove = new ArrayList<String>();
        filesToRemove.add("file1.pdf");
        filesToRemove.add("file2.jpg");

        when(amazonS3.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(mock(DeleteObjectsResult.class));
        when(storageService.getApplicationProperties()).thenReturn(applicationProperties);

        storageService.deleteFilesSync(FileType.BENEFICIARY_PROCEDURE, 1L, filesToRemove);

        verify(amazonS3, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
    }

}
