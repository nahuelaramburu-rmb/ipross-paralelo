package com.capacidad.validationapi.module.prescription.event;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationMessageType;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import com.capacidad.validationapi.module.prescription.model.Prescription;
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
public class PrescriptionEventListener {

    protected static final String NEW_PRESCRIPTION_TITLE_KEY = "newPrescriptionNotification.title";
    protected static final String NEW_PRESCRIPTION_BODY_KEY = "newPrescriptionNotification.body";
    private final NotificationPublisher notificationPublisher;
    private final BeneficiaryFinder beneficiaryFinder;
    private final LocaleHandler localeHandler;

    @Autowired
    public PrescriptionEventListener(NotificationPublisher notificationPublisher,
                                     BeneficiaryFinder beneficiaryFinder,
                                     LocaleHandler localeHandler) {
        this.notificationPublisher = notificationPublisher;
        this.beneficiaryFinder = beneficiaryFinder;
        this.localeHandler = localeHandler;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleNewPrescriptionEvent(NewPrescriptionEvent event) throws ObjectNotFoundException {
        Prescription prescription = event.getPrescription();
        Notification notification = buildNewPrescription(prescription);
        List<String> resourceIds = beneficiaryFinder.getFamily(prescription.getBeneficiary().getFamilyId()).stream()
                .map(b -> b.getResourceId().toString())
                .collect(Collectors.toUnmodifiableList());
        notificationPublisher.publishToResourceId(notification, resourceIds);
    }

    public Notification buildNewPrescription(Prescription prescription) throws ObjectNotFoundException {
        Beneficiary beneficiary = beneficiaryFinder.findById(prescription.getBeneficiary().getId());
        UUID tenantId = prescription.getTenantId();
        String title = localeHandler.getLocaleMessage(NEW_PRESCRIPTION_TITLE_KEY,
                Locale.forLanguageTag("es"),
                prescription.getId().toString())
                .orElse("");
        String body = localeHandler.getLocaleMessage(NEW_PRESCRIPTION_BODY_KEY,
                Locale.forLanguageTag("es"),
                beneficiary.getLastName(), beneficiary.getName(), prescription.getStatus().getName())
                .orElse("");
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(PARAM_TYPE, NotificationMessageType.NEW_PRESCRIPTION.toString());
        extraData.put("prescriptionId", prescription.getId());
        extraData.put("beneficiaryId", prescription.getBeneficiary().getId());
        extraData.put("practitionerId", prescription.getPractitioner().getId());
        return new Notification(NotificationType.ALL, tenantId, title, body, extraData);
    }

}
