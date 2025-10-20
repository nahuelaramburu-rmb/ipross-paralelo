package com.capacidad.validationapi.module.audittray.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.config.security.WebSocketAuthentication;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.UUID;

@Controller
@Log4j2
public class AuditTraySocketController {

    private final SimpMessageSendingOperations sendingOperations;
    private final AuditTrayService auditTrayService;
    private final AuditorService auditorService;

    @Autowired
    public AuditTraySocketController(SimpMessageSendingOperations sendingOperations,
                                     AuditTrayService auditTrayService,
                                     AuditorService auditorService) {
        this.sendingOperations = sendingOperations;
        this.auditTrayService = auditTrayService;
        this.auditorService = auditorService;
    }

    @MessageMapping("/auditors/connections")
    public void mapAuditorConnections(@Payload JsonNode payload, Principal principal) {
        TenantContext.setTenant(((WebSocketAuthentication) principal).getTenantId());
        sendingOperations.convertAndSend(auditTrayService.buildAuditorConnectionsTopicName(), payload);
    }

    @Transactional
    @MessageMapping("/audit-trays/{auditTrayResourceId}/broadcast")
    public void mapAuditTrayBroadcast(@DestinationVariable UUID auditTrayResourceId, @Payload JsonNode payload, Principal principal) throws ObjectNotFoundException, ObjectNotValidException {
        validateAuditTray(auditTrayResourceId, principal);
        sendingOperations.convertAndSend(auditTrayService.buildAuditTrayBroadcastTopicName(auditTrayResourceId.toString(), true), payload);
    }

    private void validateAuditTray(UUID auditTrayResourceId, Principal principal) throws ObjectNotValidException, ObjectNotFoundException {
        WebSocketAuthentication webSocketAuthentication = (WebSocketAuthentication) principal;
        TenantContext.setTenant(webSocketAuthentication.getTenantId());
        AuditTray auditTray = auditTrayService.findByResourceId(auditTrayResourceId);
        Auditor auditor = auditorService.findBySub(webSocketAuthentication.getSub());
        if (!auditTray.getAuditors().contains(auditor))
            throw new ObjectNotValidException("Auditor does not belong to specified Audit Tray");
    }

}
