package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryImportDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.importprocessor.model.ImportOperation;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import com.capacidad.validationapi.module.importprocessor.service.ImportErrorHandler;
import com.capacidad.validationapi.module.render.service.impl.CSVReaderWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryImportServiceImplTest {

    private final Map<String, Object> persistedProperties = new HashMap<>();

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ImportErrorHandler importErrorHandler;

    @Mock
    private BeneficiaryImportPropertiesInitializer importPropertiesInitializer;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private BeneficiaryImportOperationResolver importOperationResolver;

    @Mock
    private BeneficiaryImportBuilderTemplate importBuilder;

    @Mock
    private IdentityClientService tokenVerifier;

    @Mock
    private HikariDataSourcePoolMetadata dataSourcePoolMetadata;

    @Spy
    @InjectMocks
    private BeneficiaryImportServiceImpl beneficiaryImportService;

    @Before
    public void init() {
        when(beneficiaryImportService.getProcessingDataSourcePoolMetadata()).thenReturn(dataSourcePoolMetadata);
        when(dataSourcePoolMetadata.getIdle()).thenReturn(2);
        when(applicationProperties.getFileImportProcessorExtensionList()).thenReturn(Collections.singletonList("csv"));
        when(beneficiaryImportService.getApplicationProperties()).thenReturn(applicationProperties);
        when(importPropertiesInitializer.initializeProperties()).thenReturn(persistedProperties);
        when(beneficiaryImportService.getTokenVerifier()).thenReturn(tokenVerifier);
        doNothing().when(tokenVerifier).verify(anyString());
    }

    @Test
    public void testImportMultipartFileDoNothingWhenIOException() throws IOException, ObjectNotValidException {
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), null, "", null, "token");

        MultipartFile file = mock(MultipartFile.class);
        ImportProperties importProperties = new ImportProperties(Collections.emptyList(),
                0, ';',
                ImportOperation.CREATE_UPDATE,
                file, mock(OutputStream.class),
                jwtAuthenticationToken,
                null);

        when(file.getInputStream()).thenReturn(inputStream).thenThrow(new IOException(""));
        when(file.getOriginalFilename()).thenReturn("file.csv");

        beneficiaryImportService.importMultipartFile(importProperties);

        verify(importOperationResolver, never()).executeImportOperation(any());
    }

    @Test
    public void testImportMultipartFileResolvesExceptionOnBeneficiaryBuildCreateUpdate() throws IOException, ObjectNotValidException, ObjectNotFoundException {
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);
        OutputStream outputStream = mock(OutputStream.class);
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), null, "", null, "token");

        MultipartFile file = mock(MultipartFile.class);
        ImportProperties importProperties = new ImportProperties(Collections.emptyList(),
                0, ';',
                ImportOperation.CREATE_UPDATE,
                file,
                outputStream,
                jwtAuthenticationToken,
                null);

        CSVReaderWrapper<BeneficiaryImportDTO> csvReaderWrapper = mock(CSVReaderWrapper.class);
        Iterator<BeneficiaryImportDTO> iterator = mock(Iterator.class);
        Mockito.doCallRealMethod().when(iterator).forEachRemaining(any(Consumer.class));
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(importDTO);

        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getOriginalFilename()).thenReturn("file.csv");
        doReturn(csvReaderWrapper).when(beneficiaryImportService).buildCsvReader(importProperties);
        when(csvReaderWrapper.iterator(BeneficiaryImportDTO.class, false)).thenReturn(iterator);
        when(csvReaderWrapper.getRowCount(any(Charset.class))).thenReturn(1L);
        doNothing().when(beneficiaryImportService).validateImportObject(importDTO);
        when(importBuilder.buildBeneficiary(importDTO, persistedProperties)).thenThrow(new ObjectNotValidException(""));
        when(importErrorHandler.handleImportError(any(Exception.class))).thenReturn("error");
        when(importOperationResolver.executeImportOperation(any())).thenReturn(new ImportReport());
        when(objectMapper.writeValueAsString(any())).thenReturn("parsedValue");

        beneficiaryImportService.importMultipartFile(importProperties);

        verify(importOperationResolver, times(1)).executeImportOperation(any());
        verify(importErrorHandler, times(1)).handleImportError(any(Exception.class));
        verify(importBuilder, never()).setAuditInfo(any(Beneficiary.class), any(ImportProperties.class));
    }

    @Test
    public void testImportMultipartFileResolvesExceptionOnBeneficiaryBuildDisable() throws IOException, ObjectNotValidException {
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);
        OutputStream outputStream = mock(OutputStream.class);
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), null, "", null, "token");

        MultipartFile file = mock(MultipartFile.class);
        ImportProperties importProperties = new ImportProperties(Collections.emptyList(),
                0, ';',
                ImportOperation.DISABLE,
                file,
                outputStream,
                jwtAuthenticationToken,
                null);

        CSVReaderWrapper<BeneficiaryImportDTO> csvReaderWrapper = mock(CSVReaderWrapper.class);
        Iterator<BeneficiaryImportDTO> iterator = mock(Iterator.class);
        Mockito.doCallRealMethod().when(iterator).forEachRemaining(any(Consumer.class));
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(importDTO);

        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getOriginalFilename()).thenReturn("file.csv");
        doReturn(csvReaderWrapper).when(beneficiaryImportService).buildCsvReader(importProperties);
        when(csvReaderWrapper.iterator(BeneficiaryImportDTO.class, false)).thenReturn(iterator);
        when(csvReaderWrapper.getRowCount(any(Charset.class))).thenReturn(1L);
        when(importBuilder.resolveBeneficiaryCode(importDTO)).thenThrow(new ObjectNotValidException(""));
        when(importOperationResolver.executeImportOperation(any())).thenReturn(new ImportReport());
        when(objectMapper.writeValueAsString(any())).thenReturn("parsedValue");

        beneficiaryImportService.importMultipartFile(importProperties);

        verify(importOperationResolver, times(1)).executeImportOperation(any());
        verify(importErrorHandler, never()).handleImportError(any(Exception.class));
        verify(importBuilder, never()).setAuditInfo(any(Beneficiary.class), any(ImportProperties.class));
    }

    @Test
    public void testImportMultipartFileResolvesEmptyOnBeneficiaryBuildDisable() throws IOException, ObjectNotValidException {
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);
        OutputStream outputStream = mock(OutputStream.class);
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), null, "", null, "token");

        MultipartFile file = mock(MultipartFile.class);
        ImportProperties importProperties = new ImportProperties(Collections.emptyList(),
                0, ';',
                ImportOperation.DISABLE,
                file,
                outputStream,
                jwtAuthenticationToken,
                null);

        CSVReaderWrapper<BeneficiaryImportDTO> csvReaderWrapper = mock(CSVReaderWrapper.class);
        Iterator<BeneficiaryImportDTO> iterator = mock(Iterator.class);
        Mockito.doCallRealMethod().when(iterator).forEachRemaining(any(Consumer.class));
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(importDTO);

        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getOriginalFilename()).thenReturn("file.csv");
        doReturn(csvReaderWrapper).when(beneficiaryImportService).buildCsvReader(importProperties);
        when(csvReaderWrapper.iterator(BeneficiaryImportDTO.class, false)).thenReturn(iterator);
        when(csvReaderWrapper.getRowCount(any(Charset.class))).thenReturn(1L);
        when(importBuilder.resolveBeneficiaryCode(importDTO)).thenReturn("");
        when(importOperationResolver.executeImportOperation(any())).thenReturn(new ImportReport());
        when(objectMapper.writeValueAsString(any())).thenReturn("parsedValue");

        beneficiaryImportService.importMultipartFile(importProperties);

        verify(importOperationResolver, times(1)).executeImportOperation(any());
        verify(importErrorHandler, never()).handleImportError(any(Exception.class));
        verify(importBuilder, never()).setAuditInfo(any(Beneficiary.class), any(ImportProperties.class));
    }

    @Test
    public void testImportMultipartFileResolvesValidBeneficiaryBuildCreateUpdate() throws IOException, ObjectNotValidException, ObjectNotFoundException {
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);
        OutputStream outputStream = mock(OutputStream.class);
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), null, "", null, "token");

        MultipartFile file = mock(MultipartFile.class);
        ImportProperties importProperties = new ImportProperties(Collections.emptyList(),
                0, ';',
                ImportOperation.CREATE_UPDATE,
                file,
                outputStream,
                jwtAuthenticationToken,
                null);

        CSVReaderWrapper<BeneficiaryImportDTO> csvReaderWrapper = mock(CSVReaderWrapper.class);
        Iterator<BeneficiaryImportDTO> iterator = mock(Iterator.class);
        Mockito.doCallRealMethod().when(iterator).forEachRemaining(any(Consumer.class));
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(importDTO);

        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getOriginalFilename()).thenReturn("file.csv");
        doReturn(csvReaderWrapper).when(beneficiaryImportService).buildCsvReader(importProperties);
        when(csvReaderWrapper.iterator(BeneficiaryImportDTO.class, false)).thenReturn(iterator);
        when(csvReaderWrapper.getRowCount(any(Charset.class))).thenReturn(1L);
        doNothing().when(beneficiaryImportService).validateImportObject(importDTO);
        when(importBuilder.buildBeneficiary(importDTO, persistedProperties)).thenReturn(new Beneficiary());
        when(importOperationResolver.executeImportOperation(any())).thenReturn(new ImportReport());
        when(objectMapper.writeValueAsString(any())).thenReturn("parsedValue");

        beneficiaryImportService.importMultipartFile(importProperties);

        verify(importOperationResolver, times(1)).executeImportOperation(any());
        verify(importErrorHandler, never()).handleImportError(any(Exception.class));
        verify(importBuilder, times(1)).setAuditInfo(any(Beneficiary.class), any(ImportProperties.class));
    }

    @Test
    public void testImportMultipartFileResolvesValidBeneficiaryBuildDisable() throws IOException, ObjectNotValidException {
        byte[] fileData = "validSignatureFile".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileData);
        OutputStream outputStream = mock(OutputStream.class);
        BeneficiaryImportDTO importDTO = new BeneficiaryImportDTO();

        JWTAuthenticationToken jwtAuthenticationToken = new JWTAuthenticationToken("", Collections.emptyList(), null, "", null, "token");

        MultipartFile file = mock(MultipartFile.class);
        ImportProperties importProperties = new ImportProperties(Collections.emptyList(),
                0, ';',
                ImportOperation.DISABLE,
                file,
                outputStream,
                jwtAuthenticationToken,
                null);

        CSVReaderWrapper<BeneficiaryImportDTO> csvReaderWrapper = mock(CSVReaderWrapper.class);
        Iterator<BeneficiaryImportDTO> iterator = mock(Iterator.class);
        Mockito.doCallRealMethod().when(iterator).forEachRemaining(any(Consumer.class));
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(importDTO);

        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getOriginalFilename()).thenReturn("file.csv");
        doReturn(csvReaderWrapper).when(beneficiaryImportService).buildCsvReader(importProperties);
        when(csvReaderWrapper.iterator(BeneficiaryImportDTO.class, false)).thenReturn(iterator);
        when(csvReaderWrapper.getRowCount(any(Charset.class))).thenReturn(1L);
        when(importBuilder.resolveBeneficiaryCode(importDTO)).thenReturn("beneficiaryCode");
        when(importOperationResolver.executeImportOperation(any())).thenReturn(new ImportReport());
        when(objectMapper.writeValueAsString(any())).thenReturn("parsedValue");

        beneficiaryImportService.importMultipartFile(importProperties);

        verify(importOperationResolver, times(1)).executeImportOperation(any());
        verify(importErrorHandler, never()).handleImportError(any(Exception.class));
        verify(importBuilder, times(1)).setAuditInfo(any(Beneficiary.class), any(ImportProperties.class));
    }

}
