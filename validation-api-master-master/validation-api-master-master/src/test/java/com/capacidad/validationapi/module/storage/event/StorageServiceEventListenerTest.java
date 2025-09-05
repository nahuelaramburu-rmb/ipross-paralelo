package com.capacidad.validationapi.module.storage.event;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_TYPE;
import static com.capacidad.validationapi.module.notification.model.NotificationMessageType.VALIDATION_NEW_FILE;
import static com.capacidad.validationapi.module.storage.event.StorageServiceEventListener.NEW_ATTACHMENT_FILE_TITLE_KEY;
import static com.capacidad.validationapi.module.storage.event.StorageServiceEventListener.NEW_REPORT_FILE_TITLE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageServiceEventListenerTest {

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private AuditHistoryService auditHistoryService;

    @Mock
    private LocaleHandler localeHandler;

    @InjectMocks
    private StorageServiceEventListener storageServiceEventListener;

    @Test
    public void testHandleNewReportEventReturnsEmptyWhenNoAuditHistoryPresent() {
        UUID tenantId = UUID.randomUUID();
        long relatedId = 1L;
        String filename = "test.pdf";

        NewReportEvent event = new NewReportEvent(relatedId, filename, tenantId);

        Optional<Notification> result = storageServiceEventListener.handleNewReportEvent(event);

        assertThat(result).isEmpty();
        verify(notificationPublisher, never()).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleNewReportEventReturnsEmptyWhenNoAuditorsAssigned() {
        UUID tenantId = UUID.randomUUID();
        long relatedId = 1L;
        String filename = "test.pdf";

        NewReportEvent event = new NewReportEvent(relatedId, filename, tenantId);

        Set<AuditHistory> auditHistorySet = new HashSet<>();
        auditHistorySet.add(new AuditHistory());

        when(auditHistoryService.findByMedicalAuthorizationId(relatedId)).thenReturn(auditHistorySet);

        Optional<Notification> result = storageServiceEventListener.handleNewReportEvent(event);

        assertThat(result).isEmpty();
        verify(notificationPublisher, never()).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleNewReportEventReturnsValidNotificationWhenAuditorsAssigned() {
        UUID tenantId = UUID.randomUUID();
        long relatedId = 1L;
        String filename = "test.pdf";

        NewReportEvent event = new NewReportEvent(relatedId, filename, tenantId);

        Set<AuditHistory> auditHistorySet = new HashSet<>();
        AuditHistory auditHistory = new AuditHistory();
        Auditor auditor = new Auditor();
        auditor.setSub(UUID.randomUUID());
        auditHistory.setAuditor(auditor);
        auditHistorySet.add(auditHistory);

        when(auditHistoryService.findByMedicalAuthorizationId(relatedId)).thenReturn(auditHistorySet);
        when(localeHandler.getLocaleMessage(NEW_REPORT_FILE_TITLE_KEY, filename, String.valueOf(relatedId))).thenReturn(Optional.of("title"));

        Optional<Notification> result = storageServiceEventListener.handleNewReportEvent(event);

        assertThat(result).isPresent();
        Notification notification = result.get();

        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
        assertThat(notification.getTenantId()).isEqualTo(tenantId);
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getExtraData().get(PARAM_TYPE)).isEqualTo(VALIDATION_NEW_FILE.toString());
        assertThat(notification.getExtraData().get("authorizationId")).isEqualTo(relatedId);

        verify(notificationPublisher, times(1)).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleNewAttachmentEventReturnsEmptyWhenNoAuditHistoryPresent() {
        UUID tenantId = UUID.randomUUID();
        long relatedId = 1L;
        String filename = "test.pdf";

        NewAttachmentEvent event = new NewAttachmentEvent(relatedId, filename, tenantId);

        Optional<Notification> result = storageServiceEventListener.handleNewAttachmentEvent(event);

        assertThat(result).isEmpty();
        verify(notificationPublisher, never()).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleNewAttachmentEventReturnsValidNotificationWhenAuditorsAssigned() {
        UUID tenantId = UUID.randomUUID();
        long relatedId = 1L;
        String filename = "test.pdf";

        NewAttachmentEvent event = new NewAttachmentEvent(relatedId, filename, tenantId);

        Set<AuditHistory> auditHistorySet = new HashSet<>();
        AuditHistory auditHistory = new AuditHistory();
        Auditor auditor = new Auditor();
        auditor.setSub(UUID.randomUUID());
        auditHistory.setAuditor(auditor);
        auditHistorySet.add(auditHistory);

        when(auditHistoryService.findByMedicalAuthorizationId(relatedId)).thenReturn(auditHistorySet);
        when(localeHandler.getLocaleMessage(NEW_ATTACHMENT_FILE_TITLE_KEY, filename, String.valueOf(relatedId))).thenReturn(Optional.of("title"));

        Optional<Notification> result = storageServiceEventListener.handleNewAttachmentEvent(event);

        assertThat(result).isPresent();
        Notification notification = result.get();

        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
        assertThat(notification.getTenantId()).isEqualTo(tenantId);
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getExtraData().get(PARAM_TYPE)).isEqualTo(VALIDATION_NEW_FILE.toString());
        assertThat(notification.getExtraData().get("authorizationId")).isEqualTo(relatedId);

        verify(notificationPublisher, times(1)).publishToSub(any(Notification.class), anyList());
    }


}
