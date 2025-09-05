package com.capacidad.validationapi.module.medicalauthorization.event;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationMessageType;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_TYPE;

@Component
public class MedicalAuthorizationEventListener {

    protected static final String AUTHORIZATION_NEW_TITLE_KEY = "newAuthorizationNotification.title";
    protected static final String AUTHORIZATION_NEW_BODY_KEY = "newAuthorizationNotification.body";
    protected static final String AUTHORIZATION_STATUS_UPDATE_TITLE_KEY = "authorizationStatusUpdateNotification.title";
    protected static final String AUTHORIZATION_STATUS_UPDATE_BODY_KEY = "authorizationStatusUpdateNotification.body";
    protected static final String AUTHORIZATION_DIAGNOSIS_UPDATE_TITLE_KEY = "authorizationDiagnosisUpdateNotification.title";
    protected static final String AUTHORIZATION_NEW_MESSAGE_TITLE_KEY = "authorizationNewMessageNotification.title";
    private final NotificationPublisher notificationPublisher;
    private final BeneficiaryFinder beneficiaryFinder;
    private final AuditHistoryService auditHistoryService;
    private final LocaleHandler localeHandler;

    @Autowired
    public MedicalAuthorizationEventListener(NotificationPublisher notificationPublisher,
                                             BeneficiaryFinder beneficiaryFinder,
                                             AuditHistoryService auditHistoryService,
                                             LocaleHandler localeHandler) {
        this.notificationPublisher = notificationPublisher;
        this.beneficiaryFinder = beneficiaryFinder;
        this.auditHistoryService = auditHistoryService;
        this.localeHandler = localeHandler;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleMedicalAuthorizationNewEvent(MedicalAuthorizationNewEvent event) throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = event.getMedicalAuthorization();
        Notification notification = buildStatusUpdateNotification(medicalAuthorization,
                AUTHORIZATION_NEW_TITLE_KEY,
                AUTHORIZATION_NEW_BODY_KEY);
        List<String> resourceIds = beneficiaryFinder.getFamily(medicalAuthorization.getBeneficiary().getFamilyId()).stream()
                .map(b -> b.getResourceId().toString())
                .collect(Collectors.toUnmodifiableList());
        notificationPublisher.publishToResourceId(notification, resourceIds);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleMedicalAuthorizationStatusUpdateEvent(MedicalAuthorizationStatusUpdateEvent event) throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = event.getMedicalAuthorization();
        Notification notification = buildStatusUpdateNotification(medicalAuthorization,
                AUTHORIZATION_STATUS_UPDATE_TITLE_KEY,
                AUTHORIZATION_STATUS_UPDATE_BODY_KEY);
        List<String> resourceIds = beneficiaryFinder.getFamily(medicalAuthorization.getBeneficiary().getFamilyId()).stream()
                .map(b -> b.getResourceId().toString())
                .collect(Collectors.toList());
        resourceIds.add(medicalAuthorization.getPractitioner().getResourceId().toString());
        resourceIds.add(medicalAuthorization.getMedicalCenter().getResourceId().toString());
        notificationPublisher.publishToResourceId(notification, resourceIds);
        notifyAuditors(event, notification, medicalAuthorization);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleMedicalAuthorizationDiagnosisUpdateEvent(MedicalAuthorizationDiagnosisUpdateEvent event) {
        MedicalAuthorization medicalAuthorization = event.getMedicalAuthorization();
        Notification notification = buildDiagnosisUpdateNotification(medicalAuthorization);
        notifyAuditors(event, notification, medicalAuthorization);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleMedicalAuthorizationNewMessageEvent(MedicalAuthorizationNewMessage event) {
        MedicalAuthorization medicalAuthorization = event.getMedicalAuthorization();
        Notification notification = buildNewMessageNotification(medicalAuthorization);
        notifyAuditors(event, notification, medicalAuthorization);
        if (!event.isNotifyAuditors())
            notificationPublisher.publishToResourceId(notification, event.getResourceIds());
    }

    private void notifyAuditors(AuditorAwareEvent event, Notification notification, MedicalAuthorization medicalAuthorization) {
        if (event.isNotifyAuditors()) {
            Set<Auditor> auditors = new HashSet<>();
            for (AuditHistory auditHistory : auditHistoryService.findByMedicalAuthorizationId(medicalAuthorization.getId())) {
                if (auditHistory.getAuditor() != null)
                    auditors.add(auditHistory.getAuditor());
            }
            if (!auditors.isEmpty()) {
                List<String> auditorSubs = auditors.stream()
                        .map(a -> a.getSub().toString())
                        .collect(Collectors.toUnmodifiableList());
                notification.setNotificationType(NotificationType.WEB);
                notificationPublisher.publishToSub(notification, auditorSubs);
            }
        }
    }

    public Notification buildStatusUpdateNotification(MedicalAuthorization medicalAuthorization, String titleKey, String bodyKey) throws ObjectNotFoundException {
        Beneficiary beneficiary = beneficiaryFinder.findById(medicalAuthorization.getBeneficiary().getId());
        UUID tenantId = medicalAuthorization.getTenantId();
        String title = localeHandler.getLocaleMessage(titleKey,
                Locale.forLanguageTag("es"),
                medicalAuthorization.getId().toString())
                .orElse("");
        String body = localeHandler.getLocaleMessage(bodyKey,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), medicalAuthorization.getStatus().getName())
                .orElse("");
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(PARAM_TYPE, NotificationMessageType.VALIDATION_UPDATE.toString());
        extraData.put("authorizationId", medicalAuthorization.getId());
        extraData.put("beneficiaryId", medicalAuthorization.getBeneficiary().getId());
        extraData.put("practitionerId", medicalAuthorization.getPractitioner().getId());
        return new Notification(NotificationType.ALL, tenantId, title, body, extraData);
    }

    public Notification buildDiagnosisUpdateNotification(MedicalAuthorization medicalAuthorization) {
        UUID tenantId = medicalAuthorization.getTenantId();
        String title = localeHandler.getLocaleMessage(AUTHORIZATION_DIAGNOSIS_UPDATE_TITLE_KEY,
                Locale.forLanguageTag("es"),
                medicalAuthorization.getId().toString())
                .orElse("");
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(PARAM_TYPE, NotificationMessageType.VALIDATION_UPDATE.toString());
        extraData.put("authorizationId", medicalAuthorization.getId());
        return new Notification(NotificationType.ALL, tenantId, title, "", extraData);
    }

    public Notification buildNewMessageNotification(MedicalAuthorization medicalAuthorization) {
        UUID tenantId = medicalAuthorization.getTenantId();
        String title = localeHandler.getLocaleMessage(AUTHORIZATION_NEW_MESSAGE_TITLE_KEY,
                Locale.forLanguageTag("es"),
                medicalAuthorization.getId().toString())
                .orElse("");
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(PARAM_TYPE, NotificationMessageType.VALIDATION_NEW_MESSAGE.toString());
        extraData.put("authorizationId", medicalAuthorization.getId());
        return new Notification(NotificationType.ALL, tenantId, title, "", extraData);
    }


}
