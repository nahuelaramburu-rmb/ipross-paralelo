package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.procedure.dto.CertificateProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.CertificateProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.hateoas.CertificateProcedureResource;
import com.capacidad.validationapi.module.procedure.model.CertificateProcedure;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.procedure.model.ProcedureResolution;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.CertificateProcedureRepository;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.model.MultipartFileListWrapper;
import com.capacidad.validationapi.module.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_FUNDER_INSTANCE;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CertificateProcedureServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private Utils utils;

    @Mock
    private CertificateProcedureRepository certificateProcedureRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private CertificateProcedureServiceImpl certificateProcedureService;

    @Before
    public void init() {
        when(certificateProcedureService.getObjectMapper()).thenReturn(objectMapper);
        when(certificateProcedureService.getUtils()).thenReturn(utils);
        when(certificateProcedureService.getStorageService()).thenReturn(storageService);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testCreateThrowsExceptionWhenActiveProcedureExists() throws ObjectNotValidException, ObjectNotFoundException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(multipartFile);
        CertificateProcedureDTO certificateProcedureDTO = new CertificateProcedureDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setBeneficiary(beneficiary);

        when(objectMapper.convertValue(certificateProcedureDTO, CertificateProcedure.class)).thenReturn(certificateProcedure);
        when(certificateProcedureRepository.findExistentProcedure(beneficiary.getId(), PROCEDURE_REVISION.getId())).thenReturn(true);

        certificateProcedureService.create(certificateProcedureDTO, files);
    }

    @Test
    public void testCreateIsSuccessfulWithRevisionStatusWhenValidData() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        MultipartFile multipartFile = mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(multipartFile);
        CertificateProcedureDTO certificateProcedureDTO = new CertificateProcedureDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setId(1L);
        certificateProcedure.setBeneficiary(beneficiary);

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        when(objectMapper.convertValue(certificateProcedureDTO, CertificateProcedure.class)).thenReturn(certificateProcedure);
        when(certificateProcedureRepository.findExistentProcedure(beneficiary.getId(), PROCEDURE_REVISION.getId())).thenReturn(false);

        when(utils.getEntityReference(Status.class, revision.getId())).thenReturn(revision);
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);
        doNothing().when(storageService).storeFile(any(FileType.class), any(MultipartFileListWrapper.class), anyBoolean());
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_FUNDER_INSTANCE));

        ProcedureProjection result = certificateProcedureService.create(certificateProcedureDTO, files);

        assertThat(result.getStatus().getId()).isEqualTo(revision.getId());
        assertThat(result.getFileCount()).isEqualTo(files.size());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testCreateIsSuccessfulWithRevisionStatusWhenValidDataAndEmptyFiles() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        CertificateProcedureDTO certificateProcedureDTO = new CertificateProcedureDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setId(1L);
        certificateProcedure.setBeneficiary(beneficiary);

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        when(objectMapper.convertValue(certificateProcedureDTO, CertificateProcedure.class)).thenReturn(certificateProcedure);
        when(certificateProcedureRepository.findExistentProcedure(beneficiary.getId(), PROCEDURE_REVISION.getId())).thenReturn(false);

        when(utils.getEntityReference(Status.class, revision.getId())).thenReturn(revision);
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_FUNDER_INSTANCE));

        ProcedureProjection result = certificateProcedureService.create(certificateProcedureDTO, Collections.emptyList());

        assertThat(result.getStatus().getId()).isEqualTo(revision.getId());
        assertThat(result.getFileCount()).isZero();
        verify(storageService, never()).storeFile(any(FileType.class), any(MultipartFileListWrapper.class), anyBoolean());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testCreateIsSuccessfulWithRevisionStatusWhenValidDataAndNullFiles() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        CertificateProcedureDTO certificateProcedureDTO = new CertificateProcedureDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setId(1L);
        certificateProcedure.setBeneficiary(beneficiary);

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        when(objectMapper.convertValue(certificateProcedureDTO, CertificateProcedure.class)).thenReturn(certificateProcedure);
        when(certificateProcedureRepository.findExistentProcedure(beneficiary.getId(), PROCEDURE_REVISION.getId())).thenReturn(false);

        when(utils.getEntityReference(Status.class, revision.getId())).thenReturn(revision);
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_FUNDER_INSTANCE));

        ProcedureProjection result = certificateProcedureService.create(certificateProcedureDTO, null);

        assertThat(result.getStatus().getId()).isEqualTo(revision.getId());
        assertThat(result.getFileCount()).isZero();
        verify(storageService, never()).storeFile(any(FileType.class), any(MultipartFileListWrapper.class), anyBoolean());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testAddMessagesThrowsExceptionWhenProcedureNotInRevision() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setId(1L);
        Status approved = new Status();
        approved.setId(PROCEDURE_APPROVED.getId());
        certificateProcedure.setStatus(approved);

        doReturn(certificateProcedure).when(certificateProcedureService).findById(1L);

        certificateProcedureService.addMessage(1L, new MessageDTO());
    }

    @Test
    public void testAddMessagesExecuteSuccessfullyWhenProcedureStatusIsInRevision() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        CertificateProcedure certificateProcedure = new CertificateProcedure();
        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());
        certificateProcedure.setStatus(revision);

        String from = "principal";
        String text = "message1";

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setText(text);

        Message message = new Message();
        message.setText(text);

        doReturn(certificateProcedure).when(certificateProcedureService).findById(1L);
        doReturn(applicationEventPublisher).when(certificateProcedureService).getApplicationEventPublisher();
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(jwtAuthenticationToken.getPrincipal()).thenReturn("principal");
        when(objectMapper.convertValue(messageDTO, Message.class)).thenReturn(message);

        Message result = certificateProcedureService.addMessage(1L, messageDTO);

        assertThat(result.getFrom()).isEqualTo(from);
        assertThat(result.getSentAt()).isNotNull();
        assertThat(certificateProcedure.getMessages().size()).isEqualTo(1);
        verify(certificateProcedureRepository, times(1)).save(certificateProcedure);
    }

    @Test
    public void testAddFilesExecuteSuccessfullyWhenProcedureStatusIsInRevision() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());
        certificateProcedure.setStatus(revision);
        certificateProcedure.setFileCount(2);

        MultipartFile multipartFile = mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(multipartFile);

        doNothing().when(storageService).storeFile(any(FileType.class), any(MultipartFileListWrapper.class), anyBoolean());
        doReturn(certificateProcedure).when(certificateProcedureService).findById(1L);

        certificateProcedureService.addFiles(1L, files);

        assertThat(certificateProcedure.getFileCount()).isEqualTo(3);
        verify(certificateProcedureRepository, times(1)).save(certificateProcedure);
    }

    @Test
    public void testRemoveFilesExecuteSuccessfullyWhenProcedureStatusIsInRevision() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());
        certificateProcedure.setStatus(revision);
        certificateProcedure.setFileCount(3);

        String filename = "file.pdf";

        doNothing().when(storageService).deleteFileSync(FileType.BENEFICIARY_PROCEDURE, 1L, filename);
        doReturn(certificateProcedure).when(certificateProcedureService).findById(1L);

        certificateProcedureService.removeFile(1L, filename);

        assertThat(certificateProcedure.getFileCount()).isEqualTo(2);
        verify(certificateProcedureRepository, times(1)).save(certificateProcedure);
    }

    @Test
    public void testResolveExecuteSuccessfullyWhenResolutionIsApprove() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        certificateProcedure.setStatus(revision);

        CertificateProcedureResolutionDTO resolutionDTO = new CertificateProcedureResolutionDTO();
        resolutionDTO.setExpiration(LocalDate.now());
        resolutionDTO.setResolution(ProcedureResolution.APPROVE);

        Status approved = new Status();
        approved.setId(PROCEDURE_APPROVED.getId());

        doReturn(certificateProcedure).when(certificateProcedureService).findById(1L);
        doReturn(applicationEventPublisher).when(certificateProcedureService).getApplicationEventPublisher();
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);
        when(utils.getEntityReference(Status.class, approved.getId())).thenReturn(approved);

        CertificateProcedureResource result = (CertificateProcedureResource) certificateProcedureService.resolve(1L, resolutionDTO);
        ProcedureProjection procedureProjection = result.getContent();

        assertThat(procedureProjection.getExpiration()).isEqualTo(certificateProcedure.getExpiration());
        assertThat(procedureProjection.getStatus().getId()).isEqualTo(approved.getId());
        assertThat(procedureProjection.getClosedAt()).isNotNull();
    }

    @Test(expected = ObjectNotValidException.class)
    public void testResolveThrowsExceptionWhenResolutionIsRejectAndEmptyReason() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        certificateProcedure.setStatus(revision);

        CertificateProcedureResolutionDTO resolutionDTO = new CertificateProcedureResolutionDTO();
        resolutionDTO.setExpiration(LocalDate.now());
        resolutionDTO.setResolution(ProcedureResolution.REJECT);
        resolutionDTO.setReason(null);

        doReturn(certificateProcedure).when(certificateProcedureService).findById(1L);

        certificateProcedureService.resolve(1L, resolutionDTO);
    }

    @Test
    public void testResolveExecuteSuccessfullyWhenResolutionIsRejectAndReasonNotEmpty() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        CertificateProcedure certificateProcedure = new CertificateProcedure();

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        certificateProcedure.setStatus(revision);

        CertificateProcedureResolutionDTO resolutionDTO = new CertificateProcedureResolutionDTO();
        resolutionDTO.setExpiration(LocalDate.now());
        resolutionDTO.setResolution(ProcedureResolution.REJECT);
        resolutionDTO.setReason("reject reason");

        Status reject = new Status();
        reject.setId(PROCEDURE_REJECTED.getId());

        doReturn(certificateProcedure).when(certificateProcedureService).findById(1L);
        doReturn(applicationEventPublisher).when(certificateProcedureService).getApplicationEventPublisher();
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);
        when(utils.getEntityReference(Status.class, reject.getId())).thenReturn(reject);
        when(jwtAuthenticationToken.getPrincipal()).thenReturn("principal");

        CertificateProcedureResource result = (CertificateProcedureResource) certificateProcedureService.resolve(1L, resolutionDTO);
        ProcedureProjection procedureProjection = result.getContent();

        assertThat(procedureProjection.getExpiration()).isEqualTo(certificateProcedure.getExpiration());
        assertThat(procedureProjection.getStatus().getId()).isEqualTo(reject.getId());
        assertThat(procedureProjection.getClosedAt()).isNotNull();
        assertThat(procedureProjection.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void testUpdateReturnsValidUpdatedProcedureNullFileRemoval() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setId(1L);
        certificateProcedure.setFileCount(1);

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        certificateProcedure.setStatus(revision);

        MultipartFile multipartFile = mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(multipartFile);

        var updateObject = new HashMap<String, Object>();

        doReturn(certificateProcedure).when(certificateProcedureService).inMemoryUpdate(updateObject, certificateProcedure.getId());
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);

        ProcedureProjection result = certificateProcedureService.update(new HashMap<>(), certificateProcedure.getId(), files, null);

        assertThat(result).isNotNull();
        assertThat(result.getFileCount()).isEqualTo(2);
        verify(storageService, times(1)).storeFile(any(FileType.class), any(MultipartFileListWrapper.class), anyBoolean());
        verify(storageService, never()).deleteFilesSync(any(FileType.class), anyLong(), anyList());
    }

    @Test
    public void testUpdateReturnsValidUpdatedProcedureEmptyFileRemoval() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setId(1L);
        certificateProcedure.setFileCount(1);

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        certificateProcedure.setStatus(revision);

        MultipartFile multipartFile = mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(multipartFile);

        var updateObject = new HashMap<String, Object>();

        doReturn(certificateProcedure).when(certificateProcedureService).inMemoryUpdate(updateObject, certificateProcedure.getId());
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);

        ProcedureProjection result = certificateProcedureService.update(new HashMap<>(), certificateProcedure.getId(), files, new ArrayList<>());

        assertThat(result).isNotNull();
        assertThat(result.getFileCount()).isEqualTo(2);
        verify(storageService, times(1)).storeFile(any(FileType.class), any(MultipartFileListWrapper.class), anyBoolean());
        verify(storageService, never()).deleteFilesSync(any(FileType.class), anyLong(), anyList());
    }

    @Test
    public void testUpdateReturnsValidUpdatedProcedureWithFileRemoval() throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure certificateProcedure = new CertificateProcedure();
        certificateProcedure.setId(1L);
        certificateProcedure.setFileCount(1);

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        certificateProcedure.setStatus(revision);

        MultipartFile multipartFile = mock(MultipartFile.class);
        List<MultipartFile> files = Collections.singletonList(multipartFile);

        var updateObject = new HashMap<String, Object>();
        var fileRemoval = new ArrayList<String>();
        fileRemoval.add("filetoremove.jpg");

        doReturn(certificateProcedure).when(certificateProcedureService).inMemoryUpdate(updateObject, certificateProcedure.getId());
        when(certificateProcedureRepository.save(certificateProcedure)).thenReturn(certificateProcedure);

        ProcedureProjection result = certificateProcedureService.update(new HashMap<>(), certificateProcedure.getId(), files, fileRemoval);

        assertThat(result).isNotNull();
        assertThat(result.getFileCount()).isEqualTo(1);
        verify(storageService, times(1)).storeFile(any(FileType.class), any(MultipartFileListWrapper.class), anyBoolean());
        verify(storageService, times(1)).deleteFilesSync(any(FileType.class), anyLong(), anyList());
    }

}
