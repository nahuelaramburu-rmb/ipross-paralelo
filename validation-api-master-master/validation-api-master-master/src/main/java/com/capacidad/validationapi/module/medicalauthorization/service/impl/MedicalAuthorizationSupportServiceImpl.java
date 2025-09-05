package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.budget.service.BudgetService;
import com.capacidad.validationapi.module.medicalauthorization.event.MedicalAuthorizationDiagnosisUpdateEvent;
import com.capacidad.validationapi.module.medicalauthorization.event.MedicalAuthorizationNewMessage;
import com.capacidad.validationapi.module.medicalauthorization.event.MedicalAuthorizationStatusUpdateEvent;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationSupportService;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.capacidad.validationapi.module.render.service.RenderService;
import com.capacidad.validationapi.module.settlement.service.SettlementItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_PARTIALLY_APPROVED;

@Service
@Transactional
public class MedicalAuthorizationSupportServiceImpl implements MedicalAuthorizationSupportService {

    private final BudgetService budgetService;
    private final SettlementItemService settlementItemService;
    private final RenderService renderService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PreMedicalAuthorizationService preMedicalAuthorizationService;

    @Autowired
    public MedicalAuthorizationSupportServiceImpl(BudgetService budgetService,
                                                  SettlementItemService settlementItemService,
                                                  RenderService renderService,
                                                  ApplicationEventPublisher applicationEventPublisher,
                                                  PreMedicalAuthorizationService preMedicalAuthorizationService) {
        this.budgetService = budgetService;
        this.settlementItemService = settlementItemService;
        this.renderService = renderService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.preMedicalAuthorizationService = preMedicalAuthorizationService;
    }

    @Override
    public void discountChargesAndValues(MedicalAuthorization medicalAuthorization) {
        settlementItemService.removeItems(medicalAuthorization);
        budgetService.removeFromBudget(medicalAuthorization);
    }

    @Override
    public void rollBackPreMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        preMedicalAuthorizationService.rollbackConsumptionFromMedicalAuthorization(medicalAuthorization);
    }

    @Override
    public ByteArrayOutputStream buildReceipt(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException {
        Long statusId = medicalAuthorization.getStatus().getId();
        if (!statusId.equals(VALIDATION_APPROVED.getId()) && !statusId.equals(VALIDATION_PARTIALLY_APPROVED.getId()))
            throw new ObjectNotValidException("medicalAuthorization.invalidReceipt");
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("generatedBy", SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("-"));
        templateValues.put("generatedAt", LocalDateTime.now());
        templateValues.put("data", medicalAuthorization);
        return renderService.renderPDF("medical_authorization", templateValues);
    }

    @Override
    public void publishStatusUpdateEventAndNotifyAuditors(MedicalAuthorization medicalAuthorization) {
        applicationEventPublisher.publishEvent(new MedicalAuthorizationStatusUpdateEvent(medicalAuthorization, true));
    }

    @Override
    public void publishDiagnosisUpdateEventAndNotifyAuditors(MedicalAuthorization medicalAuthorization) {
        applicationEventPublisher.publishEvent(new MedicalAuthorizationDiagnosisUpdateEvent(medicalAuthorization, true));
    }

    @Override
    public void publishNewMessageEventAndNotifyAuditors(MedicalAuthorization medicalAuthorization) {
        boolean notifyAuditors = !SecurityUtils.isAuditor();
        List<String> resourceIds = new ArrayList<>();
        resourceIds.add(medicalAuthorization.getMedicalCenter().getResourceId().toString());
        resourceIds.add(medicalAuthorization.getPractitioner().getResourceId().toString());
        applicationEventPublisher.publishEvent(new MedicalAuthorizationNewMessage(medicalAuthorization, notifyAuditors, resourceIds));
    }
}
