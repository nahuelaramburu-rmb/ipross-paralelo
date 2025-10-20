package com.capacidad.validationapi.module.audittray.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.WebSocketAuthentication;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.UUID;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AuditTraySocketControllerTest {

    @Mock
    private SimpMessageSendingOperations sendingOperations;

    @Mock
    private AuditTrayService auditTrayService;

    @Mock
    private AuditorService auditorService;

    @Mock
    private WebSocketAuthentication webSocketAuthentication;

    @InjectMocks
    private AuditTraySocketController auditTraySocketController;

    @Test(expected = ObjectNotValidException.class)
    public void testMapAuditTrayBroadcastThrowsObjectNotValidExceptionWhenAuditorDoesNotBelongToAuditTray() throws ObjectNotValidException, ObjectNotFoundException {
        UUID resourceId = UUID.randomUUID();
        JsonNode payload = mock(JsonNode.class);

        AuditTray auditTray = new AuditTray();

        UUID sub = UUID.randomUUID();

        when(auditTrayService.findByResourceId(resourceId)).thenReturn(auditTray);
        when(webSocketAuthentication.getSub()).thenReturn(sub);
        when(auditorService.findBySub(sub)).thenReturn(new Auditor());

        auditTraySocketController.mapAuditTrayBroadcast(resourceId, payload, webSocketAuthentication);
    }

    @Test
    public void testMapAuditTrayBroadcastExecuteSuccessfullyWhenAuditorBelongsToAuditTray() throws ObjectNotValidException, ObjectNotFoundException {
        UUID resourceId = UUID.randomUUID();
        JsonNode payload = mock(JsonNode.class);

        AuditTray auditTray = new AuditTray();
        auditTray.setResourceId(resourceId);
        Auditor auditor = new Auditor();
        auditTray.getAuditors().add(auditor);

        UUID sub = UUID.randomUUID();

        String broadcastTopic = "broadcast-topic";

        when(auditTrayService.findByResourceId(resourceId)).thenReturn(auditTray);
        when(webSocketAuthentication.getSub()).thenReturn(sub);
        when(auditorService.findBySub(sub)).thenReturn(auditor);
        when(auditTrayService.buildAuditTrayBroadcastTopicName(resourceId.toString(), true)).thenReturn(broadcastTopic);

        auditTraySocketController.mapAuditTrayBroadcast(resourceId, payload, webSocketAuthentication);

        verify(sendingOperations, times(1)).convertAndSend(broadcastTopic, payload);
    }


}
