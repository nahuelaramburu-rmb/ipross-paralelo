package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.service.BatchService;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.contract.service.ContractAdjustmentService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.Failure;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationApproverImplTest {

    @Mock
    private RuleProcessor ruleProcessor;

    @Mock
    private MedicalCoverageService medicalCoverageService;

    @Mock
    private BatchService batchService;

    @Mock
    private ContractAdjustmentService contractAdjustmentService;

    @Mock
    private MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Mock
    private Utils utils;

    @InjectMocks
    private MedicalAuthorizationApproverImpl medicalAuthorizationApprover;

    @Test
    public void testApproveExecutesSuccessfullyWhenBatchAuthorization() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setBatchItem(new BatchItem());
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_APPROVED.getId())).thenReturn(VALIDATION_APPROVED.getInstance());

        medicalAuthorizationApprover.approve(medicalAuthorization);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        verify(ruleProcessor, times(1)).applyMedicalAuthorizationRules(medicalAuthorization);
        verify(ruleProcessor, times(1)).applyMedicalAuthorizationItemRules(medicalAuthorizationItem);
        verify(contractAdjustmentService, never()).applyContractAdjustments(medicalAuthorizationItem);
        verify(medicalCoverageService, never()).applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);
    }

    @Test
    public void testApproveExecutesSuccessfullyWhenRegularAuth() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());
        medicalAuthorizationItem.setAuthorizationCondition(AuthorizationConditionReference.USAGE_RATE_EXCEEDED.getInstance());

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_APPROVED.getId())).thenReturn(VALIDATION_APPROVED.getInstance());

        medicalAuthorizationApprover.approve(medicalAuthorization);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorization.getAuthorizationCondition().getId()).isEqualTo(AuthorizationConditionReference.CONTRACT_EXCESS.getId());
        verify(medicalCoverageService, times(1)).applyMedicalCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);
        verify(ruleProcessor, times(1)).applyMedicalAuthorizationRules(medicalAuthorization);
        verify(ruleProcessor, times(1)).applyMedicalAuthorizationItemRules(medicalAuthorizationItem);
        verify(contractAdjustmentService, times(1)).applyContractAdjustments(medicalAuthorizationItem);
    }

    @Test
    public void testApproveDeterminesConditionWhenParentConditionTransit() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setAuthorizationCondition(AuthorizationConditionReference.TRANSIT.getInstance());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_APPROVED.getId())).thenReturn(VALIDATION_APPROVED.getInstance());

        medicalAuthorizationApprover.approve(medicalAuthorization);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorization.getAuthorizationCondition().getId()).isEqualTo(AuthorizationConditionReference.TRANSIT.getId());
    }

    @Test
    public void testApproveDeterminesConditionWhenParentConditionNotTransitAndItemConditionNotNull() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setAuthorizationCondition(AuthorizationConditionReference.MAXIMUM_EXCEEDED.getInstance());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());
        medicalAuthorizationItem.setAuthorizationCondition(AuthorizationConditionReference.MAXIMUM_EXCEEDED.getInstance());

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_APPROVED.getId())).thenReturn(VALIDATION_APPROVED.getInstance());

        medicalAuthorizationApprover.approve(medicalAuthorization);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorization.getAuthorizationCondition().getId()).isEqualTo(AuthorizationConditionReference.CONTRACT_EXCESS.getId());
    }

    @Test
    public void testVerifyAlreadyPendingDoNotFailsWhenItemPendingButFirst() throws ObjectNotValidException {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(new Nomenclator());
        medicalAuthorizationItem.setStatus(VALIDATION_PENDING.getInstance());

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(new Beneficiary());

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        when(medicalAuthorizationItemService.countBeneficiaryAuthorizationItemAmount(any(MedicalAuthorizationItem.class), any(Status.class)))
                .thenReturn(0);

        medicalAuthorizationApprover.verifyAlreadyPending(medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_PENDING.getId());
    }

    @Test
    public void testVerifyAlreadyPendingFailsWhenItemPendingAndNotFirst() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(new Nomenclator());
        medicalAuthorizationItem.setStatus(VALIDATION_PENDING.getInstance());

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(new Beneficiary());

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        when(medicalAuthorizationItemService.countBeneficiaryAuthorizationItemAmount(any(MedicalAuthorizationItem.class), any(Status.class)))
                .thenReturn(2);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationApprover.verifyAlreadyPending(medicalAuthorizationItem));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.alreadyPending");
    }

    @Test
    public void testApplyBatchCoverageSetsRejectedWhenFailuresAndApproved() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setBatchItem(new BatchItem());
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());
        medicalAuthorizationItem.getFailures().add(new Failure());

        when(batchService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem)).thenReturn(Optional.of(medicalAuthorizationItem.getBatchItem()));

        medicalAuthorizationApprover.applyBatchCoverage(medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_REJECTED.getId());
    }

    @Test
    public void testApplyBatchCoverageDoNotSetStatusWhenFailuresButAlreadyRejected() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setBatchItem(new BatchItem());
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorizationItem.setStatus(VALIDATION_REJECTED.getInstance());
        medicalAuthorizationItem.getFailures().add(new Failure());

        when(batchService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem)).thenReturn(Optional.of(medicalAuthorizationItem.getBatchItem()));

        medicalAuthorizationApprover.applyBatchCoverage(medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_REJECTED.getId());
    }

    @Test
    public void testApplyBatchCoverageDoNotSetStatusWhenNoFailures() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setBatchItem(new BatchItem());
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());

        when(batchService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem)).thenReturn(Optional.of(medicalAuthorizationItem.getBatchItem()));

        medicalAuthorizationApprover.applyBatchCoverage(medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
    }

}
