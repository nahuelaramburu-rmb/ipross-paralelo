package com.capacidad.validationapi.module.procedure.event;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationMessageType;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.BENEFICIARY;
import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_TYPE;

@Component
public class ProcedureEventListener {

    protected static final String NEW_PROCEDURE_TITLE_KEY = "procedureNew.title";
    protected static final String PROCEDURE_STATUS_UPDATE_TITLE_KEY = "procedureStatusUpdateNotification.title";
    protected static final String PROCEDURE_STATUS_UPDATE_BODY_KEY = "procedureStatusUpdateNotification.body";
    protected static final String PROCEDURE_NEW_MESSAGE_KEY = "procedureNewMessageNotification.title";
    private final NotificationPublisher notificationPublisher;
    private final BeneficiaryFinder beneficiaryFinder;
    private final LocaleHandler localeHandler;


    @Autowired
    public ProcedureEventListener(NotificationPublisher notificationPublisher,
                                  BeneficiaryFinder beneficiaryFinder,
                                  LocaleHandler localeHandler) {
        this.notificationPublisher = notificationPublisher;
        this.beneficiaryFinder = beneficiaryFinder;
        this.localeHandler = localeHandler;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleProcedureCreationEvent(ProcedureCreationEvent event) throws ObjectNotFoundException {
        Procedure procedure = event.getProcedure();
        Notification notification = buildNewProcedureNotification(procedure);
        notificationPublisher.publishToRole(notification, event.getRoles());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleProcedureResolutionEvent(ProcedureResolutionEvent event) throws ObjectNotFoundException {
        Procedure procedure = event.getProcedure();
        Notification notification = buildProcedureStatusUpdateNotification(procedure);
        List<String> resourceIds = getFamilyResourceIds(procedure.getBeneficiary());
        notificationPublisher.publishToResourceId(notification, resourceIds);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleProcedureNewMessageEvent(ProcedureNewMessageEvent event) {
        Procedure procedure = event.getProcedure();
        Notification notification = buildProcedureNewMessageNotification(procedure);
        if (event.getRoles().contains(BENEFICIARY)) {
            List<String> resourceIds = getFamilyResourceIds(procedure.getBeneficiary());
            notificationPublisher.publishToResourceId(notification, resourceIds);
        } else
            notificationPublisher.publishToRole(notification, event.getRoles());
    }

    public Notification buildNewProcedureNotification(Procedure procedure) throws ObjectNotFoundException {
        Beneficiary beneficiary = beneficiaryFinder.findById(procedure.getBeneficiary().getId());
        UUID tenantId = procedure.getTenantId();
        String title = localeHandler.getLocaleMessage(NEW_PROCEDURE_TITLE_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(),
                beneficiary.getName(),
                procedure.getId().toString())
                .orElse("");
        Map<String, Object> extraData = buildExtraDataObject(procedure.getId(), procedure.getClass().getSimpleName(), NotificationMessageType.NEW_PROCEDURE);
        return new Notification(NotificationType.ALL, tenantId, title, "", extraData);
    }

    public Notification buildProcedureStatusUpdateNotification(Procedure procedure) throws ObjectNotFoundException {
        Beneficiary beneficiary = beneficiaryFinder.findById(procedure.getBeneficiary().getId());
        UUID tenantId = procedure.getTenantId();
        String title = localeHandler.getLocaleMessage(PROCEDURE_STATUS_UPDATE_TITLE_KEY,
                Locale.forLanguageTag("es"),
                procedure.getId().toString())
                .orElse("");
        String body = localeHandler.getLocaleMessage(PROCEDURE_STATUS_UPDATE_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), procedure.getStatus().getName())
                .orElse("");
        Map<String, Object> extraData = buildExtraDataObject(procedure.getId(), procedure.getClass().getSimpleName(), NotificationMessageType.PROCEDURE_UPDATE);
        return new Notification(NotificationType.ALL, tenantId, title, body, extraData);
    }

    public Notification buildProcedureNewMessageNotification(Procedure procedure) {
        UUID tenantId = procedure.getTenantId();
        String title = localeHandler.getLocaleMessage(PROCEDURE_NEW_MESSAGE_KEY,
                Locale.forLanguageTag("es"),
                procedure.getId().toString())
                .orElse("");
        Map<String, Object> extraData = buildExtraDataObject(procedure.getId(), procedure.getClass().getSimpleName(), NotificationMessageType.PROCEDURE_NEW_MESSAGE);
        return new Notification(NotificationType.ALL, tenantId, title, "", extraData);
    }

    public Map<String, Object> buildExtraDataObject(long procedureId, String procedureType, NotificationMessageType messageType) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(PARAM_TYPE, messageType.toString());
        extraData.put("procedureId", procedureId);
        extraData.put("procedureType", procedureType);
        return extraData;
    }

    public List<String> getFamilyResourceIds(Beneficiary beneficiary) {
        return beneficiaryFinder.getFamily(beneficiary.getFamilyId()).stream()
                .map(b -> b.getResourceId().toString())
                .collect(Collectors.toUnmodifiableList());
    }

}
