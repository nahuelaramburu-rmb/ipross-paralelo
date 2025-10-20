package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryResolutionDTO;
import com.capacidad.validationapi.module.audittray.model.AuditTrayEvent;
import com.capacidad.validationapi.module.audittray.service.AuditHistorySupportService;
import com.capacidad.validationapi.module.budget.service.BudgetService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.event.MedicalAuthorizationStatusUpdateEvent;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.capacidad.validationapi.module.settlement.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_REJECTED;

@Service
@Transactional
public class AuditHistorySupportServiceImpl implements AuditHistorySupportService {

    private final SettlementService settlementService;
    private final MedicalAuthorizationItemService medicalAuthorizationItemService;
    private final BudgetService budgetService;
    private final ProjectionFactory projectionFactory;
    private final Utils utils;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PreMedicalAuthorizationService preMedicalAuthorizationService;

    @Autowired
    public AuditHistorySupportServiceImpl(SettlementService settlementService,
                                          MedicalAuthorizationItemService medicalAuthorizationItemService,
                                          BudgetService budgetService,
                                          Utils utils,
                                          ApplicationEventPublisher applicationEventPublisher,
                                          PreMedicalAuthorizationService preMedicalAuthorizationService) {
        this.settlementService = settlementService;
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
        this.budgetService = budgetService;
        this.projectionFactory = new SpelAwareProxyProjectionFactory();
        this.utils = utils;
        this.applicationEventPublisher = applicationEventPublisher;
        this.preMedicalAuthorizationService = preMedicalAuthorizationService;
    }

    @Override
    public MedicalAuthorizationProjection.Status processAuditResolution(MedicalAuthorizationItem medicalAuthorizationItem, AuditHistoryResolutionDTO auditHistoryResolutionDTO) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        updateMedicalAuthorizationItem(medicalAuthorization, medicalAuthorizationItem, auditHistoryResolutionDTO);
        MedicalAuthorizationItem result = medicalAuthorizationItemService.save(medicalAuthorizationItem);
        return projectionFactory.createProjection(MedicalAuthorizationProjection.Status.class, result.getMedicalAuthorization());
    }

    private void updateMedicalAuthorizationItem(MedicalAuthorization medicalAuthorization, MedicalAuthorizationItem medicalAuthorizationItem, AuditHistoryResolutionDTO auditHistoryResolutionDTO) throws ObjectNotValidException, ObjectNotFoundException {
        if (!medicalAuthorizationItem.isPending())
            throw new ObjectNotValidException("medicalAuthorizationItem.invalidStatus");
        StatusReference statusReference = resolveStatusReference(auditHistoryResolutionDTO.getEvent());
        Status newStatus = utils.getGenericsEntityReference(Status.class, statusReference.getId());
        medicalAuthorizationItem.setStatus(newStatus);
        medicalAuthorizationItem.setResolution(auditHistoryResolutionDTO.getResolution());
        StatusReference validatedStatusReference = medicalAuthorization.resolveStatusReferenceBasedOnItems();
        Status validatedStatus = utils.getGenericsEntityReference(Status.class, validatedStatusReference.getId());
        publishEvent(validatedStatus, medicalAuthorization);
        medicalAuthorization.setStatus(validatedStatus);
        medicalAuthorization.setAudited(true);
        recalculateQuantities(medicalAuthorizationItem, auditHistoryResolutionDTO);
        preMedicalAuthorizationService.processMedicalAuthorizationItem(medicalAuthorizationItem, false);
        recalculateChargeSubtotals(medicalAuthorizationItem);
        recalculateBudget(medicalAuthorizationItem, medicalAuthorization);
        calculateSettlement(medicalAuthorizationItem);
    }

    private void publishEvent(Status newStatus, MedicalAuthorization medicalAuthorization) {
        if (!newStatus.getId().equals(medicalAuthorization.getStatus().getId()))
            applicationEventPublisher.publishEvent(new MedicalAuthorizationStatusUpdateEvent(medicalAuthorization, false));
    }

    private StatusReference resolveStatusReference(AuditTrayEvent auditTrayEvent) {
        return auditTrayEvent.equals(AuditTrayEvent.APPROVE_ISSUE) ? VALIDATION_APPROVED : VALIDATION_REJECTED;
    }

    private void recalculateQuantities(MedicalAuthorizationItem medicalAuthorizationItem, AuditHistoryResolutionDTO auditHistoryResolutionDTO) {
        if (medicalAuthorizationItem.isApproved() &&
                auditHistoryResolutionDTO.getQuantity() != null
                && auditHistoryResolutionDTO.getQuantity() <= medicalAuthorizationItem.getQuantity()) {
            medicalAuthorizationItem.setQuantity(auditHistoryResolutionDTO.getQuantity());
        }
    }

    private void recalculateChargeSubtotals(MedicalAuthorizationItem medicalAuthorizationItem) {
        if (medicalAuthorizationItem.isApproved()) {
            medicalAuthorizationItem.setSubtotal(medicalAuthorizationItem.getUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
            medicalAuthorizationItem.setChargeSubtotal(medicalAuthorizationItem.getChargeUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        }
    }

    private void recalculateBudget(MedicalAuthorizationItem medicalAuthorizationItem, MedicalAuthorization medicalAuthorization) {
        if (medicalAuthorizationItem.isApproved())
            medicalAuthorization.setChargeTotal(medicalAuthorization.getChargeTotal().add(medicalAuthorizationItem.getChargeSubtotal()));
        budgetService.calculateBudget(medicalAuthorizationItem);
    }

    private void calculateSettlement(MedicalAuthorizationItem medicalAuthorizationItem) {
        settlementService.createOrUpdateFromMedicalAuthorizationItem(medicalAuthorizationItem);
    }

}
