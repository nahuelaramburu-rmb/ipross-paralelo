package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.Optional;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;

@Log4j2
public abstract class MedicalAuthorizationPreProcessor {

    public final void preProcess(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        medicalAuthorization.associateChildObjects();
        medicalAuthorization.setPaymentMethod(medicalAuthorization.getBeneficiary().getPaymentMethod());
        if (medicalAuthorization.getPaymentMethod().isPaycheck())
            medicalAuthorization.setCompany(medicalAuthorization.getBeneficiary().getCompany());
        Contract contract = findApplicableContract(medicalAuthorization);
        if (Boolean.FALSE.equals(contract.getActive()))
            throw new ObjectNotValidException("contract.notActive", contract.getName());
        medicalAuthorization.setContract(contract);
        initializeMedicalAuthorization(medicalAuthorization);
        initializeTransientPreMedicalAuthorization(medicalAuthorization);
        if (medicalAuthorization.containsPreMedicalAuthorization())
            processRegularMedicalAuthorization(medicalAuthorization);
        else {
            Optional<Batch> batch = findApplicableBatch(medicalAuthorization);
            if (batch.isPresent()) {
                medicalAuthorization.setBatch(batch.get());
                processSpecialMedicalAuthorization(medicalAuthorization);
            } else
                processRegularMedicalAuthorization(medicalAuthorization);
            determineRefundable(medicalAuthorization);
        }
    }

    private void processRegularMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        medicalAuthorization.getMedicalAuthorizationItems().forEach(throwingConsumer(this::processRegularMedicalAuthorizationItem));
    }

    private void initializeMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        medicalAuthorization.setStatus(VALIDATION_APPROVED.getInstance());
        medicalAuthorization.setChargeTotal(new BigDecimal(0));
    }

    private void processSpecialMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        medicalAuthorization.getMedicalAuthorizationItems().forEach(throwingConsumer(this::processSpecialMedicalAuthorizationItem));
    }

    /**
     * The purpose of this private method is to avoid persistence of PreMedicalAuthorization reference
     * on persist step (after pre process). The object is moved to a temp transient variable.
     * PreMedicalAuthorizationService is in charge of validating and setting persistable object when present
     */
    private void initializeTransientPreMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        medicalAuthorization.setSelectedPreMedicalAuthorization(medicalAuthorization.getPreMedicalAuthorization());
        medicalAuthorization.setPreMedicalAuthorization(null);
    }

    private void processSpecialMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());
        Optional<BatchItem> optBatchItem = applyBatchCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);
        if (optBatchItem.isPresent()) {
            medicalAuthorizationItem.setBatchItem(optBatchItem.get());
            calculateMedicalAuthorizationItemPrice(medicalAuthorizationItem);
            calculateSpecialMedicalAuthorizationItemCharge(medicalAuthorizationItem);
        } else
            processRegularMedicalAuthorizationItem(medicalAuthorizationItem);
    }

    private void processRegularMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        MedicalCoverage coverage = findApplicableCoverage(medicalAuthorizationItem);
        medicalAuthorization.getMedicalCoverages().add(coverage);
        calculateMedicalAuthorizationItemPrice(medicalAuthorizationItem);
        calculateRegularMedicalAuthorizationItemCharge(medicalAuthorizationItem);
    }

    protected abstract Optional<Batch> findApplicableBatch(MedicalAuthorization medicalAuthorization);

    protected abstract MedicalCoverage findApplicableCoverage(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException;

    protected abstract Contract findApplicableContract(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException;

    protected abstract void calculateMedicalAuthorizationItemPrice(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException;

    protected abstract void calculateRegularMedicalAuthorizationItemCharge(MedicalAuthorizationItem medicalAuthorizationItem);

    protected abstract void calculateSpecialMedicalAuthorizationItemCharge(MedicalAuthorizationItem medicalAuthorizationItem);

    protected abstract Optional<BatchItem> applyBatchCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem);

    protected abstract void determineRefundable(MedicalAuthorization medicalAuthorization);

}
