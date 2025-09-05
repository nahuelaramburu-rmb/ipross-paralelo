package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.encryption.EncryptionService;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.prescription.event.NewPrescriptionEvent;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.service.PrescriptionSupportService;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.module.render.service.RenderService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PRESCRIPTION_APPROVED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PRESCRIPTION_UTILIZED;

@Log4j2
@Service
public class PrescriptionSupportServiceImpl implements PrescriptionSupportService {

    private final EncryptionService encryptionService;
    private final PropertiesService propertiesService;
    private final RenderService renderService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public PrescriptionSupportServiceImpl(EncryptionService encryptionService,
                                          PropertiesService propertiesService,
                                          RenderService renderService,
                                          ApplicationEventPublisher applicationEventPublisher) {
        this.encryptionService = encryptionService;
        this.propertiesService = propertiesService;
        this.renderService = renderService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Period getPrescriptionExpirationPeriod() {
        return propertiesService.getProperties().getPrescriptionExpirationPeriod();
    }

    @Override
    public String buildPrescriptionKey(Prescription prescription) throws ObjectNotValidException {
        return encryptionService.encrypt(StringUtils.join(prescription.getPractitioner().getIdNumber().toString(), SLASH, prescription.getBeneficiary().getIdNumber(),
                SLASH, UUID.randomUUID()));
    }

    @Override
    public void sendNewPrescriptionNotification(Prescription prescription) {
        applicationEventPublisher.publishEvent(new NewPrescriptionEvent(prescription));
    }

    @Override
    public ByteArrayOutputStream generatePrescriptionReceipt(Prescription prescription) throws ObjectNotValidException {
        Long statusId = prescription.getStatus().getId();
        if (!statusId.equals(PRESCRIPTION_APPROVED.getId()) && !statusId.equals(PRESCRIPTION_UTILIZED.getId()))
            throw new ObjectNotValidException("prescription.invalidReceipt");
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("generatedBy", SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("-"));
        templateValues.put("generatedAt", LocalDateTime.now());
        templateValues.put("data", prescription);
        return renderService.renderPDF("prescription", templateValues);
    }
}
