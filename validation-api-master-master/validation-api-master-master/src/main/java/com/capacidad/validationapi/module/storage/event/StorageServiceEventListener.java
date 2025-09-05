package com.capacidad.validationapi.module.storage.event;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationMessageType;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_TYPE;

@Component
public class StorageServiceEventListener {

    protected static final String NEW_REPORT_FILE_TITLE_KEY = "newReportFile.title";
    protected static final String NEW_ATTACHMENT_FILE_TITLE_KEY = "newAttachmentFile.title";
    private final NotificationPublisher notificationPublisher;
    private final AuditHistoryService auditHistoryService;
    private final LocaleHandler localeHandler;

    @Autowired
    public StorageServiceEventListener(NotificationPublisher notificationPublisher,
                                       AuditHistoryService auditHistoryService,
                                       LocaleHandler localeHandler) {
        this.notificationPublisher = notificationPublisher;
        this.auditHistoryService = auditHistoryService;
        this.localeHandler = localeHandler;
    }

    @EventListener
    public Optional<Notification> handleNewReportEvent(NewReportEvent event) {
        List<String> subs = getAuditorSubs(event.getRelatedId());
        if (!subs.isEmpty()) {
            Notification notification = buildNewReportNotification(event.getRelatedId(), event.getFilename(), event.getTenantId());
            notificationPublisher.publishToSub(notification, subs);
            return Optional.of(notification);
        }
        return Optional.empty();
    }

    @EventListener
    public Optional<Notification> handleNewAttachmentEvent(NewAttachmentEvent event) {
        List<String> subs = getAuditorSubs(event.getRelatedId());
        if (!subs.isEmpty()) {
            Notification notification = buildNewAttachmentNotification(event.getRelatedId(), event.getFilename(), event.getTenantId());
            notificationPublisher.publishToSub(notification, subs);
            return Optional.of(notification);
        }
        return Optional.empty();
    }

    private Notification buildNewReportNotification(long relatedId, String filename, UUID tenantId) {
        String title = localeHandler.getLocaleMessage(NEW_REPORT_FILE_TITLE_KEY,
                filename,
                String.valueOf(relatedId))
                .orElse("");
        Map<String, Object> extraData = buildExtraDataObject(relatedId);
        return new Notification(NotificationType.ALL, tenantId, title, "", extraData);
    }

    private Notification buildNewAttachmentNotification(long relatedId, String filename, UUID tenantId) {
        String title = localeHandler.getLocaleMessage(NEW_ATTACHMENT_FILE_TITLE_KEY,
                filename,
                String.valueOf(relatedId))
                .orElse("");
        Map<String, Object> extraData = buildExtraDataObject(relatedId);
        return new Notification(NotificationType.ALL, tenantId, title, "", extraData);
    }

    private Map<String, Object> buildExtraDataObject(long relatedId) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(PARAM_TYPE, NotificationMessageType.VALIDATION_NEW_FILE.toString());
        extraData.put("authorizationId", relatedId);
        return extraData;
    }

    private List<String> getAuditorSubs(long medicalAuthorizationId) {
        Set<Auditor> auditors = new HashSet<>();
        for (AuditHistory auditHistory : auditHistoryService.findByMedicalAuthorizationId(medicalAuthorizationId)) {
            if (auditHistory.getAuditor() != null)
                auditors.add(auditHistory.getAuditor());
        }
        if (!auditors.isEmpty()) {
            return auditors.stream()
                    .map(a -> a.getSub().toString())
                    .collect(Collectors.toUnmodifiableList());
        }
        return Collections.emptyList();
    }

}
