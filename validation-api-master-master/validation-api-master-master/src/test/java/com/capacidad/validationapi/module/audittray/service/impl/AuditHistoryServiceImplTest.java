package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.constant.ModelConstants;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryAssignDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryResolutionDTO;
import com.capacidad.validationapi.module.audittray.model.*;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.audittray.repository.AuditHistoryRepository;
import com.capacidad.validationapi.module.audittray.service.AuditHistorySupportService;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditHistoryServiceImplTest {

    @Mock
    private AuditHistoryRepository auditHistoryRepository;

    @Mock
    private AuditorService auditorService;

    @Mock
    private AuditTrayService auditTrayService;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private AuditHistorySupportService auditHistorySupportService;

    @Mock
    private SecurityContext securityContext;

    @Spy
    @InjectMocks
    private AuditHistoryServiceImpl auditHistoryService;

    @Test(expected = ObjectNotValidException.class)
    public void testAssociateAuditHistoryWithAuditorThrowsObjectNotValidExceptionWhenAuditorDoesNotBelongToAuditTray() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();
        AuditTray auditTray = new AuditTray();
        auditHistory.setAuditTray(auditTray);

        Auditor auditor = new Auditor();

        UUID sub = UUID.randomUUID();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);
        when(auditorService.findBySub(sub)).thenReturn(auditor);

        auditHistoryService.associateAuditHistoryWithAuditor(1L);
    }

    @Test
    public void testAssociateAuditHistoryWithAuditorExecuteCorrectlyWhenAuditorBelongsToAuditTray() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();
        AuditTray auditTray = new AuditTray();
        auditHistory.setAuditTray(auditTray);

        Auditor auditor = new Auditor();
        auditor.getAuditTrays().add(auditTray);

        UUID sub = UUID.randomUUID();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);
        when(auditorService.findBySub(sub)).thenReturn(auditor);

        auditHistoryService.associateAuditHistoryWithAuditor(1L);

        assertThat(auditHistory.getAuditor()).isEqualTo(auditor);
        verify(auditHistoryRepository, times(1)).save(auditHistory);
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testAssignAuditHistoryThrowsObjectAlreadyExistExceptionWhenAuditTrayAlreadyContainsMedicalAuthorization() throws ObjectNotFoundException, ObjectNotValidException {
        AuditHistory auditHistory = new AuditHistory();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        auditHistory.setMedicalAuthorization(medicalAuthorization);

        AuditTray auditTray = new AuditTray();
        auditTray.setName("auditTray");

        auditHistory.setAuditTray(auditTray);

        UUID resourceId = UUID.randomUUID();
        AuditHistoryAssignDTO auditHistoryAssignDTO = new AuditHistoryAssignDTO();
        auditHistoryAssignDTO.setAuditTrayResourceId(resourceId);

        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(auditHistoryRepository.existsByMedicalAuthorizationIdAndAuditTrayResourceId(1L, resourceId)).thenReturn(true);

        auditHistoryService.assignAuditHistory(1L, auditHistoryAssignDTO);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testAssignAuditHistoryThrowsObjectNotValidExceptionWhenAuditHistoryDoesNotBelongToAuditor() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        auditHistory.setMedicalAuthorization(medicalAuthorization);

        Auditor currentAuditor = new Auditor();
        currentAuditor.setId(2L);
        auditHistory.setAuditor(currentAuditor);

        UUID sub = UUID.randomUUID();

        Auditor auditor = new Auditor();
        auditor.setId(1L);
        auditor.setSub(sub);

        UUID resourceId = UUID.randomUUID();
        AuditHistoryAssignDTO auditHistoryAssignDTO = new AuditHistoryAssignDTO();
        auditHistoryAssignDTO.setAuditTrayResourceId(resourceId);

        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(auditHistoryRepository.existsByMedicalAuthorizationIdAndAuditTrayResourceId(1L, resourceId)).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);
        when(auditorService.findBySub(sub)).thenReturn(auditor);

        auditHistoryService.assignAuditHistory(1L, auditHistoryAssignDTO);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testAssignAuditHistoryThrowsObjectNotValidExceptionWhenAuditHistoryHasNullAuditor() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        auditHistory.setMedicalAuthorization(medicalAuthorization);

        Auditor currentAuditor = new Auditor();
        currentAuditor.setId(2L);
        auditHistory.setAuditor(null);

        UUID sub = UUID.randomUUID();

        Auditor auditor = new Auditor();
        auditor.setId(1L);
        auditor.setSub(sub);

        UUID resourceId = UUID.randomUUID();
        AuditHistoryAssignDTO auditHistoryAssignDTO = new AuditHistoryAssignDTO();
        auditHistoryAssignDTO.setAuditTrayResourceId(resourceId);

        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(auditHistoryRepository.existsByMedicalAuthorizationIdAndAuditTrayResourceId(1L, resourceId)).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);
        when(auditorService.findBySub(sub)).thenReturn(auditor);

        auditHistoryService.assignAuditHistory(1L, auditHistoryAssignDTO);
    }

    @Test
    public void testAssignAuditHistoryAssignIssueSuccessfullyWhenValidDataProvided() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        auditHistory.setMedicalAuthorization(medicalAuthorization);

        UUID sub = UUID.randomUUID();
        Auditor currentAuditor = new Auditor();
        currentAuditor.setId(1L);
        currentAuditor.setSub(sub);
        auditHistory.setAuditor(currentAuditor);

        AuditTray currentAuditTray = new AuditTray();
        auditHistory.setAuditTray(currentAuditTray);

        UUID resourceId = UUID.randomUUID();
        AuditHistoryAssignDTO auditHistoryAssignDTO = new AuditHistoryAssignDTO();
        auditHistoryAssignDTO.setAuditTrayResourceId(resourceId);

        AuditTray newAuditTray = new AuditTray();

        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(auditHistoryRepository.existsByMedicalAuthorizationIdAndAuditTrayResourceId(1L, resourceId)).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);
        when(auditorService.findBySub(sub)).thenReturn(currentAuditor);
        when(auditTrayService.findByResourceId(resourceId)).thenReturn(newAuditTray);

        AuditHistory result = auditHistoryService.assignAuditHistory(1L, auditHistoryAssignDTO);

        assertThat(result.getAuditor()).isNull();
        assertThat(result.getAuditTray()).isEqualTo(newAuditTray);
        assertThat(result.getFromAuditor()).isEqualTo(currentAuditor);
        assertThat(result.getFromAuditTray()).isEqualTo(currentAuditTray);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testResolveIssueIssueThrowsObjectNotFoundExceptionWhenInvalidMedicalAuthorizationItemIsProvided() throws ObjectNotFoundException, ObjectNotValidException {
        AuditHistory auditHistory = new AuditHistory();

        var historyItems = new HashSet<AuditHistoryItem>();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);

        AuditHistoryItem auditHistoryItem = new AuditHistoryItem();
        auditHistoryItem.setMedicalAuthorizationItem(medicalAuthorizationItem);

        historyItems.add(auditHistoryItem);

        auditHistory.setHistoryItems(historyItems);

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();
        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.APPROVE_ISSUE);
        auditHistoryResolutionDTO.setResolution("test");
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(2L);

        when(auditHistoryService.findById(1L)).thenReturn(auditHistory);

        auditHistoryService.resolveIssue(1L, auditHistoryResolutionDTO);
    }

    @Test
    public void testResolveIssueIssueThrowsObjectNotValidExceptionWhenAuditHistoryAuditorIsNull() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();
        auditHistory.setId(1L);

        var historyItems = new HashSet<AuditHistoryItem>();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);

        AuditHistoryItem auditHistoryItem = new AuditHistoryItem();
        auditHistoryItem.setMedicalAuthorizationItem(medicalAuthorizationItem);

        historyItems.add(auditHistoryItem);

        auditHistory.setHistoryItems(historyItems);

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();
        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.APPROVE_ISSUE);
        auditHistoryResolutionDTO.setResolution("test");
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        Auditor auditor = new Auditor();
        auditor.setSub(UUID.randomUUID());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(jwtAuthenticationToken.getSub()).thenReturn(auditor.getSub());
        when(auditorService.findBySub(auditor.getSub())).thenReturn(auditor);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditHistoryService.resolveIssue(auditHistory.getId(), auditHistoryResolutionDTO));

        assertThat(exception.getMessage()).isEqualTo("auditHistory.doesNotBelongToAuditor");
    }

    @Test
    public void testResolveIssueIssueThrowsObjectNotValidExceptionWhenAuditHistoryDoesNotBelongToAuditor() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();
        auditHistory.setId(1L);

        Auditor historyAuditor = new Auditor();
        historyAuditor.setId(2L);
        auditHistory.setAuditor(historyAuditor);

        var historyItems = new HashSet<AuditHistoryItem>();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);

        AuditHistoryItem auditHistoryItem = new AuditHistoryItem();
        auditHistoryItem.setMedicalAuthorizationItem(medicalAuthorizationItem);

        historyItems.add(auditHistoryItem);

        auditHistory.setHistoryItems(historyItems);

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();
        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.APPROVE_ISSUE);
        auditHistoryResolutionDTO.setResolution("test");
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        Auditor auditor = new Auditor();
        auditor.setId(1L);
        auditor.setSub(UUID.randomUUID());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(jwtAuthenticationToken.getSub()).thenReturn(auditor.getSub());
        when(auditorService.findBySub(auditor.getSub())).thenReturn(auditor);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditHistoryService.resolveIssue(auditHistory.getId(), auditHistoryResolutionDTO));

        assertThat(exception.getMessage()).isEqualTo("auditHistory.doesNotBelongToAuditor");
    }

    @Test
    public void testResolveIssueExecutesSuccessfullyWhenValidDataProvided() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        AuditHistory auditHistory = new AuditHistory();
        auditHistory.setId(1L);

        Auditor historyAuditor = new Auditor();
        historyAuditor.setId(1L);
        auditHistory.setAuditor(historyAuditor);

        var historyItems = new HashSet<AuditHistoryItem>();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);

        AuditHistoryItem auditHistoryItem = new AuditHistoryItem();
        auditHistoryItem.setMedicalAuthorizationItem(medicalAuthorizationItem);

        historyItems.add(auditHistoryItem);

        auditHistory.setHistoryItems(historyItems);

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();
        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.APPROVE_ISSUE);
        auditHistoryResolutionDTO.setResolution("test");
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        Auditor auditor = new Auditor();
        auditor.setId(1L);
        auditor.setSub(UUID.randomUUID());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        doReturn(auditHistory).when(auditHistoryService).findById(1L);
        when(jwtAuthenticationToken.getSub()).thenReturn(auditor.getSub());
        when(auditorService.findBySub(auditor.getSub())).thenReturn(auditor);

        auditHistoryService.resolveIssue(1L, auditHistoryResolutionDTO);

        verify(auditHistorySupportService, times(1)).processAuditResolution
                (medicalAuthorizationItem, auditHistoryResolutionDTO);
    }

    @Test
    public void testGetHistoryReturnsPendingWhenFlagIsTrue() {
        SecurityContextHolder.setContext(securityContext);

        Set<UUID> resourceIds = new HashSet<>();

        Pageable pageable = PageRequest.of(1, 30);
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, ModelConstants.MODIFIED_AT);
        UUID sub = UUID.randomUUID();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);

        auditHistoryService.getHistory(resourceIds, true, pageable);

        verify(auditHistoryRepository, times(1))
                .findAllByAuditorSubAndAuditTrayResourceIdInAndMedicalAuthorizationStatusId
                        (sub, resourceIds, StatusReference.VALIDATION_PENDING.getId(), pageRequest);
    }

    @Test
    public void testGetHistoryReturnsResolvedWhenFlagIsFalse() {
        SecurityContextHolder.setContext(securityContext);

        Set<UUID> resourceIds = new HashSet<>();

        Pageable pageable = PageRequest.of(1, 30);
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, ModelConstants.MODIFIED_AT);
        UUID sub = UUID.randomUUID();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);

        auditHistoryService.getHistory(resourceIds, false, pageable);

        verify(auditHistoryRepository, times(1))
                .findAllByAuditorSubAndAuditTrayResourceIdInAndMedicalAuthorizationStatusIdIsNot
                        (sub, resourceIds, StatusReference.VALIDATION_PENDING.getId(), pageRequest);
    }

    @Test
    public void testGetAuditHistoryThrowsExceptionWhenInvalidAuditor() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        UUID sub = UUID.randomUUID();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);

        AuditHistory auditHistory = new AuditHistory();
        auditHistory.setId(1L);
        AuditTray auditTray = new AuditTray();
        auditTray.setName("tray");
        auditHistory.setAuditTray(auditTray);

        doReturn(auditHistory).when(auditHistoryService).findById(auditHistory.getId());
        when(auditorService.findBySub(sub)).thenReturn(new Auditor());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditHistoryService.getAuditHistory(auditHistory.getId()));

        assertThat(exception.getMessage()).isEqualTo("auditHistory.auditorInvalidAuditTray");

        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetAuditHistoryReturnsProjectionWhenValidAuditor() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        UUID sub = UUID.randomUUID();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getSub()).thenReturn(sub);

        AuditHistory auditHistory = new AuditHistory();
        auditHistory.setId(1L);
        AuditTray auditTray = new AuditTray();
        auditTray.setName("tray");
        auditHistory.setAuditTray(auditTray);

        Auditor auditor = new Auditor();
        auditor.setId(2L);
        auditHistory.getAuditTray().getAuditors().add(auditor);

        doReturn(auditHistory).when(auditHistoryService).findById(auditHistory.getId());
        when(auditorService.findBySub(sub)).thenReturn(auditor);

        AuditHistoryProjection result = auditHistoryService.getAuditHistory(auditHistory.getId());

        assertThat(result).isNotNull();

        SecurityContextHolder.clearContext();
    }


}
