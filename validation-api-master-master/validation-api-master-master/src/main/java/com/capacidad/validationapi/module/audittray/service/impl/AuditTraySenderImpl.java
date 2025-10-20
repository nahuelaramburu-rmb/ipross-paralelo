package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.event.AuditHistoryCreationEvent;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.AuditHistoryItem;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.model.AuditTrayEvent;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.audittray.service.AuditTraySender;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Log4j2
public class AuditTraySenderImpl implements AuditTraySender {

    private final AuditTrayService auditTrayService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditHistoryService auditHistoryService;
    private final ObjectMapper objectMapper;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public AuditTraySenderImpl(AuditTrayService auditTrayService,
                               ApplicationEventPublisher applicationEventPublisher,
                               AuditHistoryService auditHistoryService,
                               ObjectMapper objectMapper) {
        this.auditTrayService = auditTrayService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.auditHistoryService = auditHistoryService;
        this.objectMapper = objectMapper;
        this.projectionFactory = new SpelAwareProxyProjectionFactory();
    }

    @Override
    public void audit(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException {
        Long medicalAuthorizationStatusId = medicalAuthorization.getStatus().getId();
        if (medicalAuthorizationStatusId.equals(StatusReference.VALIDATION_PENDING.getId())) {
            var applicableAuditTrays = new HashSet<AuditTray>();
            var auditHistoryItems = new HashSet<AuditHistoryItem>();
            for (MedicalAuthorizationItem medicalAuthorizationItem : medicalAuthorization.getMedicalAuthorizationItems()) {
                AuditHistoryItem auditHistoryItem = new AuditHistoryItem(medicalAuthorizationItem, null);
                if (medicalAuthorizationItem.getStatus().getId().equals(StatusReference.VALIDATION_PENDING.getId())) {
                    auditTrayService.resolveCorrespondingQueueName(medicalAuthorizationItem, medicalAuthorization.getCity())
                            .ifPresent(at -> {
                                applicableAuditTrays.add(at);
                                auditHistoryItem.setAuditTray(at);
                            });
                }
                auditHistoryItems.add(auditHistoryItem);
            }
            if (applicableAuditTrays.isEmpty())
                throw new ObjectNotValidException("medicalAuthorization.cannotFindAuditTrays");
            applicableAuditTrays.forEach(auditTray -> {
                AuditHistory auditHistory = new AuditHistory();
                auditHistory.setMedicalAuthorization(medicalAuthorization);
                auditHistory.setAuditTray(auditTray);
                auditHistory.getHistoryItems().addAll(new HashSet<>(auditHistoryItems));
                sendIssueToAuditTrayQueue(auditHistory);
            });
        }
    }

    @Override
    public void sendMessageToAssociatedAuditTrays(long medicalAuthorizationId, JsonNode payload) {
        Set<AuditHistory> auditHistorySet = auditHistoryService.findByMedicalAuthorizationId(medicalAuthorizationId);
        if (!auditHistorySet.isEmpty())
            auditHistorySet.forEach(auditHistory -> {
                AuditTray auditTray = auditHistory.getAuditTray();
                String auditTrayBroadcastQueueName = auditTrayService.buildAuditTrayBroadcastTopicName(auditTray.getResourceId().toString(), true);
                applicationEventPublisher.publishEvent(new AuditHistoryCreationEvent(auditTrayBroadcastQueueName, payload));
            });
    }

    @Override
    public void sendIssueToAuditTrayQueue(AuditHistory auditHistory) {
        AuditTray auditTray = auditHistory.getAuditTray();
        Integer activeConsumers = auditTrayService.getAuditTrayConsumers(auditTray);
        if (activeConsumers > 0) {
            AuditHistory persistedAuditHistory = saveAuditHistory(auditHistory, AuditTrayEvent.NEW_ISSUE);
            String auditTrayOnlineQueueName = auditTrayService.buildAuditTrayOnlineQueueName(auditTray.getResourceId().toString(), true);
            AuditHistoryProjection.Minor auditHistoryProjection = projectionFactory.createProjection(AuditHistoryProjection.Minor.class, persistedAuditHistory);
            JsonNode payload = objectMapper.valueToTree(auditHistoryProjection);
            applicationEventPublisher.publishEvent(new AuditHistoryCreationEvent(auditTrayOnlineQueueName, payload));
        } else
            saveAuditHistory(auditHistory, AuditTrayEvent.OFFLINE_ISSUE);
    }

    private AuditHistory saveAuditHistory(AuditHistory auditHistory, AuditTrayEvent event) {
        auditHistory.setEvent(event);
        return auditHistoryService.persistAndFlush(auditHistory);
    }


}
