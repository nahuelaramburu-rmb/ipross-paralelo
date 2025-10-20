package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryResolutionDTO;
import com.capacidad.validationapi.module.audittray.model.AuditTrayEvent;
import com.capacidad.validationapi.module.budget.service.BudgetService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.capacidad.validationapi.module.settlement.service.SettlementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditHistorySupportServiceImplTest {

    @Mock
    private Utils utils;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private SettlementService settlementService;

    @Mock
    private MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Mock
    private BudgetService budgetService;

    @Mock
    private PreMedicalAuthorizationService preMedicalAuthorizationService;

    @InjectMocks
    private AuditHistorySupportServiceImpl auditHistorySupportService;

    @Test
    public void testProcessAuditResolutionThrowsExceptionWhenStatusIsNotPending() {
        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setStatus(approved);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditHistorySupportService.processAuditResolution(medicalAuthorizationItem, new AuditHistoryResolutionDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorizationItem.invalidStatus");
    }

    @Test
    public void testProcessAuditResolutionExecuteSuccessfullyWhenApproved() throws ObjectNotValidException, ObjectNotFoundException {
        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();

        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.APPROVE_ISSUE);
        auditHistoryResolutionDTO.setResolution("approve");
        auditHistoryResolutionDTO.setQuantity(1);
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_APPROVED.getId())).thenReturn(approved);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(pending);
        medicalAuthorization.setChargeTotal(new BigDecimal("0"));

        medicalAuthorizationItem.setStatus(pending);
        medicalAuthorizationItem.setUnitPrice(new BigDecimal("125.7"));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal("83.9"));
        medicalAuthorizationItem.setSubtotal(new BigDecimal("251.4"));
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("167.8"));
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.associateChildObjects();

        when(medicalAuthorizationItemService.save(medicalAuthorizationItem)).thenReturn(medicalAuthorizationItem);

        MedicalAuthorizationProjection.Status result = auditHistorySupportService.processAuditResolution(medicalAuthorizationItem, auditHistoryResolutionDTO);

        assertThat(result.getStatus().getId()).isEqualTo(approved.getId());
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(medicalAuthorizationItem.getChargeUnitPrice());
        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(medicalAuthorizationItem.getUnitPrice());
        assertThat(medicalAuthorizationItem.getQuantity()).isEqualTo(1);
        assertThat(medicalAuthorizationItem.getResolution()).isNotBlank();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(approved);
        assertThat(medicalAuthorization.getChargeTotal()).isNotZero();
        assertThat(medicalAuthorization.getAudited()).isTrue();

        verify(applicationEventPublisher, times(1)).publishEvent(any(ApplicationEvent.class));
        verify(budgetService, times(1)).calculateBudget(medicalAuthorizationItem);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorizationItem(medicalAuthorizationItem);
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorizationItem(medicalAuthorizationItem, false);
    }

    @Test
    public void testProcessAuditResolutionExecuteSuccessfullyWhenNullQuantity() throws ObjectNotValidException, ObjectNotFoundException {
        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();

        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.APPROVE_ISSUE);
        auditHistoryResolutionDTO.setResolution("approve");
        auditHistoryResolutionDTO.setQuantity(null);
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_APPROVED.getId())).thenReturn(approved);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(pending);
        medicalAuthorization.setChargeTotal(new BigDecimal("0"));

        medicalAuthorizationItem.setStatus(pending);
        medicalAuthorizationItem.setUnitPrice(new BigDecimal("125.7"));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal("83.9"));
        medicalAuthorizationItem.setSubtotal(new BigDecimal("251.4"));
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("167.8"));
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.associateChildObjects();

        when(medicalAuthorizationItemService.save(medicalAuthorizationItem)).thenReturn(medicalAuthorizationItem);

        MedicalAuthorizationProjection.Status result = auditHistorySupportService.processAuditResolution(medicalAuthorizationItem, auditHistoryResolutionDTO);

        assertThat(result.getStatus().getId()).isEqualTo(approved.getId());
        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(medicalAuthorizationItem.getUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(medicalAuthorizationItem.getChargeUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getQuantity()).isEqualTo(2);
        assertThat(medicalAuthorizationItem.getResolution()).isNotBlank();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(approved);
        assertThat(medicalAuthorization.getChargeTotal()).isNotZero();
        assertThat(medicalAuthorization.getAudited()).isTrue();

        verify(applicationEventPublisher, times(1)).publishEvent(any(ApplicationEvent.class));
        verify(budgetService, times(1)).calculateBudget(medicalAuthorizationItem);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorizationItem(medicalAuthorizationItem);
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorizationItem(medicalAuthorizationItem, false);
    }

    @Test
    public void testProcessAuditResolutionExecuteSuccessfullyWhenApprovedBiggerQuantity() throws ObjectNotValidException, ObjectNotFoundException {
        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();

        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.APPROVE_ISSUE);
        auditHistoryResolutionDTO.setResolution("approve");
        auditHistoryResolutionDTO.setQuantity(3);
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_APPROVED.getId())).thenReturn(approved);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(pending);
        medicalAuthorization.setChargeTotal(new BigDecimal("0"));

        medicalAuthorizationItem.setStatus(pending);
        medicalAuthorizationItem.setUnitPrice(new BigDecimal("125.7"));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal("83.9"));
        medicalAuthorizationItem.setSubtotal(new BigDecimal("251.4"));
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("167.8"));
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.associateChildObjects();

        when(medicalAuthorizationItemService.save(medicalAuthorizationItem)).thenReturn(medicalAuthorizationItem);

        MedicalAuthorizationProjection.Status result = auditHistorySupportService.processAuditResolution(medicalAuthorizationItem, auditHistoryResolutionDTO);

        assertThat(result.getStatus().getId()).isEqualTo(approved.getId());
        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(medicalAuthorizationItem.getUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(medicalAuthorizationItem.getChargeUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getQuantity()).isEqualTo(2);
        assertThat(medicalAuthorizationItem.getResolution()).isNotBlank();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(approved);
        assertThat(medicalAuthorization.getChargeTotal()).isNotZero();
        assertThat(medicalAuthorization.getAudited()).isTrue();

        verify(applicationEventPublisher, times(1)).publishEvent(any(ApplicationEvent.class));
        verify(budgetService, times(1)).calculateBudget(medicalAuthorizationItem);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorizationItem(medicalAuthorizationItem);
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorizationItem(medicalAuthorizationItem, false);
    }

    @Test
    public void testProcessAuditResolutionDoNotUpdateQuantityOnRejection() throws ObjectNotValidException, ObjectNotFoundException {
        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();

        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.REJECT_ISSUE);
        auditHistoryResolutionDTO.setResolution("reject");
        auditHistoryResolutionDTO.setQuantity(1);
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_REJECTED.getId())).thenReturn(rejected);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(pending);
        medicalAuthorization.setChargeTotal(new BigDecimal("0"));

        medicalAuthorizationItem.setStatus(pending);
        medicalAuthorizationItem.setUnitPrice(new BigDecimal("125.7"));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal("83.9"));
        medicalAuthorizationItem.setSubtotal(new BigDecimal("251.4"));
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("167.8"));
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.associateChildObjects();

        when(medicalAuthorizationItemService.save(medicalAuthorizationItem)).thenReturn(medicalAuthorizationItem);

        MedicalAuthorizationProjection.Status result = auditHistorySupportService.processAuditResolution(medicalAuthorizationItem, auditHistoryResolutionDTO);

        assertThat(result.getStatus().getId()).isEqualTo(rejected.getId());
        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(medicalAuthorizationItem.getUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(medicalAuthorizationItem.getChargeUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getQuantity()).isEqualTo(2);
        assertThat(medicalAuthorizationItem.getResolution()).isNotBlank();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(rejected);
        assertThat(medicalAuthorization.getChargeTotal()).isZero();
        assertThat(medicalAuthorization.getAudited()).isTrue();

        verify(applicationEventPublisher, times(1)).publishEvent(any(ApplicationEvent.class));
        verify(budgetService, times(1)).calculateBudget(medicalAuthorizationItem);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorizationItem(medicalAuthorizationItem);
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorizationItem(medicalAuthorizationItem, false);
    }

    @Test
    public void testProcessAuditResolutionDoNotExecuteEventNotification() throws ObjectNotValidException, ObjectNotFoundException {
        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        AuditHistoryResolutionDTO auditHistoryResolutionDTO = new AuditHistoryResolutionDTO();

        auditHistoryResolutionDTO.setEvent(AuditTrayEvent.REJECT_ISSUE);
        auditHistoryResolutionDTO.setResolution("reject");
        auditHistoryResolutionDTO.setQuantity(1);
        auditHistoryResolutionDTO.setMedicalAuthorizationItemId(1L);

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_REJECTED.getId())).thenReturn(rejected);
        when(utils.getGenericsEntityReference(Status.class, VALIDATION_PENDING.getId())).thenReturn(pending);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(pending);
        medicalAuthorization.setChargeTotal(new BigDecimal("0"));

        medicalAuthorizationItem.setStatus(pending);
        medicalAuthorizationItem.setUnitPrice(new BigDecimal("125.7"));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal("83.9"));
        medicalAuthorizationItem.setSubtotal(new BigDecimal("251.4"));
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("167.8"));
        medicalAuthorizationItem.setQuantity(2);

        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setStatus(pending);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);
        medicalAuthorization.associateChildObjects();

        when(medicalAuthorizationItemService.save(medicalAuthorizationItem)).thenReturn(medicalAuthorizationItem);

        MedicalAuthorizationProjection.Status result = auditHistorySupportService.processAuditResolution(medicalAuthorizationItem, auditHistoryResolutionDTO);

        assertThat(result.getStatus().getId()).isEqualTo(pending.getId());
        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(medicalAuthorizationItem.getUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(medicalAuthorizationItem.getChargeUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        assertThat(medicalAuthorizationItem.getQuantity()).isEqualTo(2);
        assertThat(medicalAuthorizationItem.getResolution()).isNotBlank();
        assertThat(medicalAuthorizationItem.getStatus()).isEqualTo(rejected);
        assertThat(medicalAuthorization.getChargeTotal()).isZero();
        assertThat(medicalAuthorization.getAudited()).isTrue();

        verify(applicationEventPublisher, never()).publishEvent(any(ApplicationEvent.class));
        verify(budgetService, times(1)).calculateBudget(medicalAuthorizationItem);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorizationItem(medicalAuthorizationItem);
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorizationItem(medicalAuthorizationItem, false);
    }

}
