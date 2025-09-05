package com.capacidad.validationapi.module.procedure.event;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import com.capacidad.validationapi.module.procedure.model.CUDProcedure;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.BENEFICIARY;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.FUNDER;
import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_TYPE;
import static com.capacidad.validationapi.module.notification.model.NotificationMessageType.*;
import static com.capacidad.validationapi.module.procedure.event.ProcedureEventListener.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProcedureEventListenerTest {

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private LocaleHandler localeHandler;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @InjectMocks
    private ProcedureEventListener procedureEventListener;

    @Test
    public void testBuildNewProcedureNotificationIsValid() throws ObjectNotFoundException {
        Procedure procedure = initProcedure();
        Beneficiary beneficiary = procedure.getBeneficiary();

        when(localeHandler.getLocaleMessage(NEW_PROCEDURE_TITLE_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), procedure.getId().toString()))
                .thenReturn(Optional.of("title"));
        when(beneficiaryFinder.findById(beneficiary.getId())).thenReturn(beneficiary);

        Notification notification = procedureEventListener.buildNewProcedureNotification(procedure);

        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
        assertThat(notification.getTenantId()).isEqualTo(procedure.getTenantId());
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getBody()).isEmpty();
        assertThat(notification.getExtraData()).containsEntry(PARAM_TYPE, NEW_PROCEDURE.toString());
        assertThat(notification.getExtraData()).containsEntry("procedureId", procedure.getId());
        assertThat(notification.getExtraData()).containsEntry("procedureType", procedure.getClass().getSimpleName());
    }

    @Test
    public void testBuildProcedureStatusUpdateNotificationIsValid() throws ObjectNotFoundException {
        Procedure procedure = initProcedure();
        Beneficiary beneficiary = procedure.getBeneficiary();

        when(localeHandler.getLocaleMessage(PROCEDURE_STATUS_UPDATE_TITLE_KEY,
                Locale.forLanguageTag("es"),
                procedure.getId().toString()))
                .thenReturn(Optional.of("title"));
        when(localeHandler.getLocaleMessage(PROCEDURE_STATUS_UPDATE_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), procedure.getStatus().getName()))
                .thenReturn(Optional.of("body"));
        when(beneficiaryFinder.findById(beneficiary.getId())).thenReturn(beneficiary);

        Notification notification = procedureEventListener.buildProcedureStatusUpdateNotification(procedure);

        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
        assertThat(notification.getTenantId()).isEqualTo(procedure.getTenantId());
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getBody()).isEqualTo("body");
        assertThat(notification.getExtraData()).containsEntry(PARAM_TYPE, PROCEDURE_UPDATE.toString());
        assertThat(notification.getExtraData()).containsEntry("procedureId", procedure.getId());
        assertThat(notification.getExtraData()).containsEntry("procedureType", procedure.getClass().getSimpleName());
    }

    @Test
    public void testHandleProcedureNewMessageEventPublishValidNotificationToBeneficiary() {
        Procedure procedure = initProcedure();

        var event = new ProcedureNewMessageEvent(procedure, Collections.singletonList(BENEFICIARY));

        when(beneficiaryFinder.getFamily(any(UUID.class))).thenReturn(Collections.emptySet());

        procedureEventListener.handleProcedureNewMessageEvent(event);

        verify(notificationPublisher, times(1)).publishToResourceId(any(Notification.class), anyList());
    }

    @Test
    public void testHandleProcedureNewMessageEventPublishValidNotificationToFunder() {
        Procedure procedure = initProcedure();

        var event = new ProcedureNewMessageEvent(procedure, Collections.singletonList(FUNDER));

        procedureEventListener.handleProcedureNewMessageEvent(event);

        verify(notificationPublisher, times(1)).publishToRole(any(Notification.class), anyList());
    }

    @Test
    public void testBuildProcedureNewMessageNotificationIsValid() {
        Procedure procedure = initProcedure();

        when(localeHandler.getLocaleMessage(PROCEDURE_NEW_MESSAGE_KEY,
                Locale.forLanguageTag("es"),
                procedure.getId().toString()))
                .thenReturn(Optional.of("title"));

        Notification notification = procedureEventListener.buildProcedureNewMessageNotification(procedure);

        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ALL.toString().toLowerCase());
        assertThat(notification.getTenantId()).isEqualTo(procedure.getTenantId());
        assertThat(notification.getMessageId()).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("title");
        assertThat(notification.getBody()).isEmpty();
        assertThat(notification.getExtraData()).containsEntry(PARAM_TYPE, PROCEDURE_NEW_MESSAGE.toString());
        assertThat(notification.getExtraData()).containsEntry("procedureId", procedure.getId());
        assertThat(notification.getExtraData()).containsEntry("procedureType", procedure.getClass().getSimpleName());
    }

    private Procedure initProcedure() {
        Procedure procedure = new CUDProcedure();
        UUID tenantId = UUID.randomUUID();
        procedure.setId(1L);
        procedure.setTenantId(tenantId);

        Status approved = new Status();
        approved.setName("approved");

        procedure.setStatus(approved);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(3L);
        beneficiary.setName("nameTest");
        beneficiary.setLastName("lastnameTest");
        UUID beneficiaryResourceId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        beneficiary.setResourceId(beneficiaryResourceId);
        beneficiary.setFamilyId(familyId);

        procedure.setBeneficiary(beneficiary);

        return procedure;
    }

}
