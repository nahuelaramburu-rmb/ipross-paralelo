package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.audittray.service.AuditTraySender;
import com.capacidad.validationapi.module.budget.service.BudgetService;
import com.capacidad.validationapi.module.medicalauthorization.event.MedicalAuthorizationNewEvent;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationPostProcessor;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.capacidad.validationapi.module.prescription.event.NewPrescriptionEvent;
import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
import com.capacidad.validationapi.module.settlement.service.SettlementService;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.model.FileEncoding;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.service.StorageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Log4j2
public class MedicalAuthorizationPostProcessorImpl extends MedicalAuthorizationPostProcessor {

    private final StorageService storageService;
    private final AuditTraySender auditTraySender;
    private final BudgetService budgetService;
    private final SettlementService settlementService;
    private final PreMedicalAuthorizationService preMedicalAuthorizationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PrescriptionService prescriptionService;

    @Autowired
    public MedicalAuthorizationPostProcessorImpl(StorageService storageService,
                                                 AuditTraySender auditTraySender,
                                                 BudgetService budgetService,
                                                 SettlementService settlementService,
                                                 PreMedicalAuthorizationService preMedicalAuthorizationService,
                                                 ApplicationEventPublisher applicationEventPublisher,
                                                 PrescriptionService prescriptionService) {
        this.storageService = storageService;
        this.auditTraySender = auditTraySender;
        this.budgetService = budgetService;
        this.settlementService = settlementService;
        this.preMedicalAuthorizationService = preMedicalAuthorizationService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.prescriptionService = prescriptionService;
    }

    @Override
    protected void determinePreMedicalAuthorization(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        preMedicalAuthorizationService.processMedicalAuthorization(medicalAuthorization);
    }

    @Override
    protected void saveDigitalSignature(MedicalAuthorization medicalAuthorization) {
        try {
            if (medicalAuthorization.getDigitalSignature() != null) {
                FileDTO fileDTO = new FileDTO();
                fileDTO.setFile(medicalAuthorization.getDigitalSignature());
                fileDTO.setRelatedId(medicalAuthorization.getId());
                fileDTO.setFileEncoding(FileEncoding.PNG);
                storageService.storeFile(FileType.SIGNATURE, fileDTO, true);
            }
        } catch (ObjectNotValidException e) {
            log.error("({}) - saveDigitalSignature: {} ({})", this.getClass(), e.getMessage(), e.getArgs());
        }
    }

    @Override
    protected void audit(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException {
        auditTraySender.audit(medicalAuthorization);
    }

    @Override
    protected void calculateMedicalAuthorizationChargeTotal(MedicalAuthorization medicalAuthorization) {
        medicalAuthorization.setChargeTotal(new BigDecimal(0));
        medicalAuthorization.getMedicalAuthorizationItems()
                .forEach(medicalAuthorizationItem -> {
                    if (medicalAuthorizationItem.isApproved())
                        medicalAuthorization.setChargeTotal(medicalAuthorization.getChargeTotal()
                                .add(medicalAuthorizationItem.getChargeSubtotal()));
                });
        medicalAuthorization.setChargeTotal(medicalAuthorization.getChargeTotal()
                .setScale(0, RoundingMode.HALF_DOWN));
    }

    @Override
    protected void calculateBudget(MedicalAuthorization medicalAuthorization) {
        if (SecurityUtils.isMedicalCenter())
            budgetService.calculateBudget(medicalAuthorization);
        if (SecurityUtils.isPractitioner())
            budgetService.calculateBeneficiaryBudget(medicalAuthorization);
    }

    @Override
    protected void settle(MedicalAuthorization medicalAuthorization) {
        settlementService.createOrUpdateFromMedicalAuthorization(medicalAuthorization);
    }

    @Override
    protected void savePrescriptions(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        prescriptionService.createFromMedicalAuthorization(medicalAuthorization);
    }

    @Override
    public void notify(MedicalAuthorization medicalAuthorization) {
        applicationEventPublisher.publishEvent(new MedicalAuthorizationNewEvent(medicalAuthorization));
        medicalAuthorization.getPrescriptions().forEach(p -> applicationEventPublisher.publishEvent(new NewPrescriptionEvent(p)));
    }

}
