package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.service.BatchService;
import com.capacidad.validationapi.module.contract.service.ContractAdjustmentService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationCondition;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationApprover;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_PENDING;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_REJECTED;

@Component
public class MedicalAuthorizationApproverImpl extends MedicalAuthorizationApprover {

    private final RuleProcessor ruleProcessor;
    private final MedicalCoverageService medicalCoverageService;
    private final ContractAdjustmentService contractAdjustmentService;
    private final Utils utils;
    private final MedicalAuthorizationItemService medicalAuthorizationItemService;
    private final BatchService batchService;

    @Autowired
    public MedicalAuthorizationApproverImpl(RuleProcessor ruleProcessor,
                                            MedicalCoverageService medicalCoverageService,
                                            ContractAdjustmentService contractAdjustmentService,
                                            Utils utils,
                                            MedicalAuthorizationItemService medicalAuthorizationItemService,
                                            BatchService batchService) {
        this.ruleProcessor = ruleProcessor;
        this.medicalCoverageService = medicalCoverageService;
        this.contractAdjustmentService = contractAdjustmentService;
        this.utils = utils;
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
        this.batchService = batchService;
    }

    @Override
    protected void applyBatchCoverage(MedicalAuthorizationItem medicalAuthorizationItem) {
        Optional<BatchItem> optBatchItem = batchService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);
        if (optBatchItem.isPresent())
            determineBatchMedicalAuthorizationItemStatus(medicalAuthorizationItem);
    }

    private void determineBatchMedicalAuthorizationItemStatus(MedicalAuthorizationItem medicalAuthorizationItem) {
        if (medicalAuthorizationItem.containsFailures() && medicalAuthorizationItem.isApproved())
            medicalAuthorizationItem.setStatus(VALIDATION_REJECTED.getInstance());
    }

    @Override
    protected void applyMedicalAuthorizationRules(MedicalAuthorization medicalAuthorization) {
        ruleProcessor.applyMedicalAuthorizationRules(medicalAuthorization);
    }

    @Override
    protected void applyMedicalAuthorizationItemRules(MedicalAuthorizationItem medicalAuthorizationItem) {
        ruleProcessor.applyMedicalAuthorizationItemRules(medicalAuthorizationItem);
    }

    @Override
    protected void applyMedicalCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        medicalCoverageService.applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);
    }

    @Override
    protected void applyContractAdjustments(MedicalAuthorizationItem medicalAuthorizationItem) {
        contractAdjustmentService.applyContractAdjustments(medicalAuthorizationItem);
    }

    @Override
    protected void verifyAlreadyPending(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        if (medicalAuthorizationItem.getStatus().getId().equals(VALIDATION_PENDING.getId())) {
            Nomenclator nomenclator = medicalAuthorizationItem.getNomenclator();
            int pendingCount = medicalAuthorizationItemService
                    .countBeneficiaryAuthorizationItemAmount(medicalAuthorizationItem, VALIDATION_PENDING.getInstance());
            if (pendingCount > 0)
                throw new ObjectNotValidException("medicalAuthorization.alreadyPending", nomenclator.getNomenclatorCode());
        }
    }

    @Override
    protected void determineAuthorizationCondition(MedicalAuthorization medicalAuthorization) {
        AuthorizationCondition authorizationCondition = medicalAuthorization.getAuthorizationCondition();
        if (authorizationCondition == null || !authorizationCondition.getId().equals(AuthorizationConditionReference.TRANSIT.getId()))
            medicalAuthorization.getMedicalAuthorizationItems().stream()
                    .filter(i -> i.getAuthorizationCondition() != null)
                    .findFirst()
                    .ifPresent(i -> medicalAuthorization.setAuthorizationCondition(AuthorizationConditionReference.CONTRACT_EXCESS.getInstance()));
    }

    @Override
    protected Status resolveStatus(MedicalAuthorization medicalAuthorization) {
        StatusReference statusReference = medicalAuthorization.resolveStatusReferenceBasedOnItems();
        return utils.getGenericsEntityReference(Status.class, statusReference.getId());
    }
}
