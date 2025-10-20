package com.capacidad.validationapi.module.medicalauthorization.event;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static com.capacidad.validationapi.module.medicalauthorization.event.MedicalAuthorizationEventListener.*;
import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_TYPE;
import static com.capacidad.validationapi.module.notification.model.NotificationMessageType.VALIDATION_NEW_MESSAGE;
import static com.capacidad.validationapi.module.notification.model.NotificationMessageType.VALIDATION_UPDATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationEventListenerTest {

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Mock
    private AuditHistoryService auditHistoryService;

    @Mock
    private LocaleHandler localeHandler;

    @InjectMocks
    private MedicalAuthorizationEventListener medicalAuthorizationEventListener;

    @Test
    public void testBuildStatusUpdateNotificationIsValid() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();

        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), medicalAuthorization.getStatus().getName()))
                .thenReturn(Optional.of("body"));
        when(beneficiaryFinder.findById(beneficiary.getId())).thenReturn(beneficiary);

        Notification notification = medicalAuthorizationEventListener.buildStatusUpdateNotification(medicalAuthorization,
                AUTHORIZATION_STATUS_UPDATE_TITLE_KEY,
                AUTHORIZATION_STATUS_UPDATE_BODY_KEY);

        assertThat(notification.getTenantId()).isEqualTo(medicalAuthorization.getTenantId());
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getBody()).isEqualTo("body");
        assertThat(notification.getExtraData()).containsEntry(PARAM_TYPE, VALIDATION_UPDATE.toString());
        assertThat(notification.getExtraData()).containsEntry("beneficiaryId", medicalAuthorization.getBeneficiary().getId());
        assertThat(notification.getExtraData()).containsEntry("practitionerId", medicalAuthorization.getPractitioner().getId());
        assertThat(notification.getExtraData()).containsEntry("authorizationId", medicalAuthorization.getId());
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
    }

    @Test
    public void testHandleMedicalAuthorizationStatusUpdateEventDoNotNotifyAuditorsWhenFlagIsFalse() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();

        var event = new MedicalAuthorizationStatusUpdateEvent(medicalAuthorization, false);

        when(beneficiaryFinder.getFamily(beneficiary.getFamilyId())).thenReturn(Collections.singleton(beneficiary));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), medicalAuthorization.getStatus().getName()))
                .thenReturn(Optional.of("body"));
        when(beneficiaryFinder.findById(beneficiary.getId())).thenReturn(beneficiary);

        medicalAuthorizationEventListener.handleMedicalAuthorizationStatusUpdateEvent(event);

        verify(notificationPublisher, times(1)).publishToResourceId(any(Notification.class), anyList());
        verify(notificationPublisher, never()).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleMedicalAuthorizationStatusUpdateEventDoNotNotifyAuditorsWhenEmptyAuditHistory() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();

        var event = new MedicalAuthorizationStatusUpdateEvent(medicalAuthorization, true);

        when(beneficiaryFinder.getFamily(beneficiary.getFamilyId())).thenReturn(Collections.singleton(beneficiary));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), medicalAuthorization.getStatus().getName()))
                .thenReturn(Optional.of("body"));
        when(auditHistoryService.findByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(Collections.emptySet());
        when(beneficiaryFinder.findById(beneficiary.getId())).thenReturn(beneficiary);

        medicalAuthorizationEventListener.handleMedicalAuthorizationStatusUpdateEvent(event);

        verify(notificationPublisher, times(1)).publishToResourceId(any(Notification.class), anyList());
        verify(notificationPublisher, never()).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleMedicalAuthorizationStatusUpdateEventDoNotNotifyAuditorsWhenNotAssignedAuditors() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();

        var event = new MedicalAuthorizationStatusUpdateEvent(medicalAuthorization, true);

        when(beneficiaryFinder.getFamily(beneficiary.getFamilyId())).thenReturn(Collections.singleton(beneficiary));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), medicalAuthorization.getStatus().getName()))
                .thenReturn(Optional.of("body"));
        when(beneficiaryFinder.findById(beneficiary.getId())).thenReturn(beneficiary);

        Set<AuditHistory> auditHistorySet = new HashSet<>();
        auditHistorySet.add(new AuditHistory());

        when(auditHistoryService.findByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(auditHistorySet);

        medicalAuthorizationEventListener.handleMedicalAuthorizationStatusUpdateEvent(event);

        verify(notificationPublisher, times(1)).publishToResourceId(any(Notification.class), anyList());
        verify(notificationPublisher, never()).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleMedicalAuthorizationStatusUpdateEventNotifyAuditorsWhenValidAssignedAuditors() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();

        var event = new MedicalAuthorizationStatusUpdateEvent(medicalAuthorization, true);

        when(beneficiaryFinder.getFamily(beneficiary.getFamilyId())).thenReturn(Collections.singleton(beneficiary));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));
        when(localeHandler.getLocaleMessage(AUTHORIZATION_STATUS_UPDATE_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), medicalAuthorization.getStatus().getName()))
                .thenReturn(Optional.of("body"));
        when(beneficiaryFinder.findById(beneficiary.getId())).thenReturn(beneficiary);

        Set<AuditHistory> auditHistorySet = new HashSet<>();
        AuditHistory auditHistory = new AuditHistory();
        Auditor auditor = new Auditor();
        auditor.setSub(UUID.randomUUID());
        auditHistory.setAuditor(auditor);
        auditHistorySet.add(auditHistory);

        when(auditHistoryService.findByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(auditHistorySet);

        medicalAuthorizationEventListener.handleMedicalAuthorizationStatusUpdateEvent(event);

        verify(notificationPublisher, times(1)).publishToResourceId(any(Notification.class), anyList());
        verify(notificationPublisher, times(1)).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleMedicalAuthorizationNewMessageEventNotifyResourceIdsWhenNotifyAuditorsFalse() {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();
        MedicalAuthorizationNewMessage medicalAuthorizationNewMessage = new MedicalAuthorizationNewMessage(medicalAuthorization, false, Collections.singletonList(UUID.randomUUID().toString()));

        when(localeHandler.getLocaleMessage(AUTHORIZATION_NEW_MESSAGE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));

        medicalAuthorizationEventListener.handleMedicalAuthorizationNewMessageEvent(medicalAuthorizationNewMessage);

        verify(notificationPublisher, times(1)).publishToResourceId(any(Notification.class), anyList());
        verify(notificationPublisher, never()).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testHandleMedicalAuthorizationNewMessageEventDoNotNotifyResourceIdsWhenNotifyAuditorsTrue() {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();
        MedicalAuthorizationNewMessage medicalAuthorizationNewMessage = new MedicalAuthorizationNewMessage(medicalAuthorization, true, Collections.singletonList(UUID.randomUUID().toString()));

        Set<AuditHistory> auditHistorySet = new HashSet<>();
        AuditHistory auditHistory = new AuditHistory();
        Auditor auditor = new Auditor();
        auditor.setSub(UUID.randomUUID());
        auditHistory.setAuditor(auditor);
        auditHistorySet.add(auditHistory);

        when(auditHistoryService.findByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(auditHistorySet);
        when(localeHandler.getLocaleMessage(AUTHORIZATION_NEW_MESSAGE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));

        medicalAuthorizationEventListener.handleMedicalAuthorizationNewMessageEvent(medicalAuthorizationNewMessage);

        verify(notificationPublisher, never()).publishToResourceId(any(Notification.class), anyList());
        verify(notificationPublisher, times(1)).publishToSub(any(Notification.class), anyList());
    }

    @Test
    public void testBuildNewMessageNotificationIsValid() {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();

        when(localeHandler.getLocaleMessage(AUTHORIZATION_NEW_MESSAGE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));

        Notification notification = medicalAuthorizationEventListener.buildNewMessageNotification(medicalAuthorization);

        assertThat(notification.getTenantId()).isEqualTo(medicalAuthorization.getTenantId());
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getExtraData()).containsEntry(PARAM_TYPE, VALIDATION_NEW_MESSAGE.toString());
        assertThat(notification.getExtraData()).containsEntry("authorizationId", medicalAuthorization.getId());
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
    }

    @Test
    public void testBuildDiagnosisUpdateNotificationIsValid() {
        MedicalAuthorization medicalAuthorization = initMedicalAuthorization();

        when(localeHandler.getLocaleMessage(AUTHORIZATION_DIAGNOSIS_UPDATE_TITLE_KEY, Locale.forLanguageTag("es"), medicalAuthorization.getId().toString())).thenReturn(Optional.of("title"));

        Notification notification = medicalAuthorizationEventListener.buildDiagnosisUpdateNotification(medicalAuthorization);

        assertThat(notification.getTenantId()).isEqualTo(medicalAuthorization.getTenantId());
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getExtraData()).containsEntry(PARAM_TYPE, VALIDATION_UPDATE.toString());
        assertThat(notification.getExtraData()).containsEntry("authorizationId", medicalAuthorization.getId());
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
    }

    private MedicalAuthorization initMedicalAuthorization() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        UUID tenantId = UUID.randomUUID();
        medicalAuthorization.setId(1L);
        medicalAuthorization.setTenantId(tenantId);

        Status approved = new Status();
        approved.setName("approved");

        medicalAuthorization.setStatus(approved);

        Practitioner practitioner = new Practitioner();
        practitioner.setResourceId(UUID.randomUUID());
        practitioner.setId(2L);

        medicalAuthorization.setPractitioner(practitioner);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(3L);
        beneficiary.setName("nameTest");
        beneficiary.setLastName("lastnameTest");
        UUID beneficiaryResourceId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        beneficiary.setResourceId(beneficiaryResourceId);
        beneficiary.setFamilyId(familyId);

        medicalAuthorization.setBeneficiary(beneficiary);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setResourceId(UUID.randomUUID());

        medicalAuthorization.setMedicalCenter(medicalCenter);

        return medicalAuthorization;
    }

}
