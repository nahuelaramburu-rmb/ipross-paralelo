package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.event.AuditHistoryCreationEvent;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.model.AuditTrayEvent;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditTraySenderImplTest {

    @Mock
    private AuditTrayService auditTrayService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private AuditHistoryService auditHistoryService;

    @Mock
    private ObjectMapper objectMapper;

    @Spy
    @InjectMocks
    private AuditTraySenderImpl auditTraySender;

    @Test
    public void testAuditDoNothingWhenMedicalAuthorizationIsNotPending() throws ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());

        medicalAuthorization.setStatus(approved);

        auditTraySender.audit(medicalAuthorization);

        verify(auditTrayService, never()).resolveCorrespondingQueueName(any(MedicalAuthorizationItem.class), any(City.class));
    }

    @Test
    public void testAuditDoNothingWhenAuditTrayAreNotFoundForSpecifiedItems() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_PENDING.getId());

        medicalAuthorization.setStatus(approved);
        City city = new City();
        medicalAuthorization.setCity(city);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditTraySender.audit(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.cannotFindAuditTrays");
        verify(auditTraySender, never()).sendIssueToAuditTrayQueue(any(AuditHistory.class));
    }

    @Test
    public void testAuditExecutesSendWhenAuditTrayAreFoundForSpecifiedItems() throws ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Status pending = new Status();
        pending.setId(StatusReference.VALIDATION_PENDING.getId());

        medicalAuthorization.setStatus(pending);
        City city = new City();
        medicalAuthorization.setCity(city);

        AuditTray auditTray = new AuditTray();
        AuditTray auditTray2 = new AuditTray();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setStatus(pending);

        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setStatus(pending);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        when(auditTrayService.resolveCorrespondingQueueName(medicalAuthorizationItem, city)).thenReturn(Optional.of(auditTray));
        when(auditTrayService.resolveCorrespondingQueueName(medicalAuthorizationItem2, city)).thenReturn(Optional.of(auditTray2));
        doNothing().when(auditTraySender).sendIssueToAuditTrayQueue(any(AuditHistory.class));

        auditTraySender.audit(medicalAuthorization);

        verify(auditTraySender, times(2)).sendIssueToAuditTrayQueue(any(AuditHistory.class));
    }

    @Test
    public void testAuditExecutesSendWhenAuditTrayIsFoundForOneItemOnly() throws ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Status pending = new Status();
        pending.setId(StatusReference.VALIDATION_PENDING.getId());

        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());

        medicalAuthorization.setStatus(pending);
        City city = new City();
        medicalAuthorization.setCity(city);

        AuditTray auditTray = new AuditTray();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setStatus(pending);

        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setStatus(approved);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        when(auditTrayService.resolveCorrespondingQueueName(medicalAuthorizationItem, city)).thenReturn(Optional.of(auditTray));
        doNothing().when(auditTraySender).sendIssueToAuditTrayQueue(any(AuditHistory.class));

        auditTraySender.audit(medicalAuthorization);

        verify(auditTraySender, times(1)).sendIssueToAuditTrayQueue(any(AuditHistory.class));
    }

    @Test
    public void testAuditDoNotExecuteSendWhenAuditTrayIsNotFoundForItems() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Status pending = new Status();
        pending.setId(StatusReference.VALIDATION_PENDING.getId());

        medicalAuthorization.setStatus(pending);
        City city = new City();
        medicalAuthorization.setCity(city);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setStatus(pending);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        when(auditTrayService.resolveCorrespondingQueueName(medicalAuthorizationItem, city)).thenReturn(Optional.empty());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditTraySender.audit(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.cannotFindAuditTrays");
        verify(auditTraySender, never()).sendIssueToAuditTrayQueue(any(AuditHistory.class));
    }

    @Test
    public void testSendMessageToAssociatedAuditTraysDoNothingWhenAuditHistoriesAreNotFound() {
        when(auditHistoryService.findByMedicalAuthorizationId(1L)).thenReturn(Collections.emptySet());

        auditTraySender.sendMessageToAssociatedAuditTrays(1L, null);

        verify(applicationEventPublisher, never()).publishEvent(any(AuditHistoryCreationEvent.class));
    }

    @Test
    public void testSendMessageToAssociatedAuditTraysExecuteSendingOperationsWhenAuditHistoriesAreFound() {
        JsonNode payload = mock(JsonNode.class);

        AuditHistory auditHistory = new AuditHistory();
        AuditTray auditTray = new AuditTray();
        UUID resourceId = UUID.randomUUID();
        auditTray.setResourceId(resourceId);
        auditHistory.setAuditTray(auditTray);
        var auditHistorySet = new HashSet<AuditHistory>();
        auditHistorySet.add(auditHistory);

        when(auditHistoryService.findByMedicalAuthorizationId(1L)).thenReturn(auditHistorySet);
        String broadcastTopic = "broadcast-audit-tray";
        when(auditTrayService.buildAuditTrayBroadcastTopicName(resourceId.toString(), true)).thenReturn(broadcastTopic);

        auditTraySender.sendMessageToAssociatedAuditTrays(1L, payload);

        verify(applicationEventPublisher, times(1)).publishEvent(any(AuditHistoryCreationEvent.class));
    }

    @Test
    public void testSendIssueToAuditTrayQueueSavesOfflineWhenActiveConsumersAreZero() {
        AuditHistory auditHistory = new AuditHistory();
        AuditTray auditTray = new AuditTray();
        auditHistory.setAuditTray(auditTray);

        when(auditHistoryService.persistAndFlush(auditHistory)).thenReturn(auditHistory);
        when(auditTrayService.getAuditTrayConsumers(auditTray)).thenReturn(0);

        auditTraySender.sendIssueToAuditTrayQueue(auditHistory);

        verify(auditHistoryService, times(1)).persistAndFlush(auditHistory);
        assertThat(auditHistory.getEvent()).isEqualTo(AuditTrayEvent.OFFLINE_ISSUE);
    }

    @Test
    public void testSendIssueToAuditTrayQueueSendsNewIssueWhenActiveConsumersAreNotZero() {
        JsonNode payload = mock(JsonNode.class);

        AuditHistory auditHistory = new AuditHistory();
        AuditTray auditTray = new AuditTray();
        UUID resourceId = UUID.randomUUID();
        auditTray.setResourceId(resourceId);
        auditHistory.setAuditTray(auditTray);

        when(auditHistoryService.persistAndFlush(auditHistory)).thenReturn(auditHistory);
        when(auditTrayService.getAuditTrayConsumers(auditTray)).thenReturn(2);

        String queueName = "audit-tray-online-queue";
        when(auditTrayService.buildAuditTrayOnlineQueueName(resourceId.toString(), true)).thenReturn(queueName);

        when(objectMapper.valueToTree(any(AuditHistoryProjection.Minor.class))).thenReturn(payload);

        auditTraySender.sendIssueToAuditTrayQueue(auditHistory);

        verify(auditHistoryService, times(1)).persistAndFlush(auditHistory);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AuditHistoryCreationEvent.class));
        assertThat(auditHistory.getEvent()).isEqualTo(AuditTrayEvent.NEW_ISSUE);
    }

}
