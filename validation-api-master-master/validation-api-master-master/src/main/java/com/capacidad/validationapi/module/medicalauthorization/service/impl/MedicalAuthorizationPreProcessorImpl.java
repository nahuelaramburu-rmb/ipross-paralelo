package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.service.BatchService;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationPreProcessor;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class MedicalAuthorizationPreProcessorImpl extends MedicalAuthorizationPreProcessor {

    private final MedicalCoverageService medicalCoverageService;
    private final ContractService contractService;
    private final BatchService batchService;

    @Autowired
    public MedicalAuthorizationPreProcessorImpl(MedicalCoverageService medicalCoverageService,
                                                ContractService contractService,
                                                BatchService batchService) {
        this.medicalCoverageService = medicalCoverageService;
        this.contractService = contractService;
        this.batchService = batchService;
    }

    @Override
    protected Contract findApplicableContract(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        Practitioner practitioner = medicalAuthorization.getPractitioner();
        if (practitioner.getContracts().isEmpty())
            throw new ObjectNotValidException("practitioner.contractRequirement");
        if (practitioner.getContracts().size() == 1)
            return practitioner.getContracts().iterator().next();
        if (medicalAuthorization.getSelectedContract() == null)
            throw new ObjectNotValidException("medicalAuthorization.contractRequirement");
        Contract selectedContract = contractService.findById(medicalAuthorization.getSelectedContract().getId());
        if (!practitioner.getContracts().contains(selectedContract))
            throw new ObjectNotValidException("medicalAuthorization.practitionerInvalidContract");
        return selectedContract;
    }

    @Override
    protected Optional<Batch> findApplicableBatch(MedicalAuthorization medicalAuthorization) {
        return batchService.findApplicableBatch(medicalAuthorization);
    }

    @Override
    protected MedicalCoverage findApplicableCoverage(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        return medicalCoverageService.findApplicableCoverage(medicalAuthorizationItem);
    }

    @Override
    protected void calculateMedicalAuthorizationItemPrice(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        contractService.calculateAuthorizationItemPrice(medicalAuthorizationItem);
    }

    @Override
    protected void calculateRegularMedicalAuthorizationItemCharge(MedicalAuthorizationItem medicalAuthorizationItem) {
        if (medicalAuthorizationItem.getChargeSubtotal() == null)
            medicalCoverageService.calculateAuthorizationItemCharges(medicalAuthorizationItem);
    }

    @Override
    protected void calculateSpecialMedicalAuthorizationItemCharge(MedicalAuthorizationItem medicalAuthorizationItem) {
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(0));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal(0));
    }

    @Override
    protected Optional<BatchItem> applyBatchCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        return batchService.findApplicableBatchItem(medicalAuthorizationItem);
    }

    @Override
    protected void determineRefundable(MedicalAuthorization medicalAuthorization) {
        medicalAuthorization.getMedicalAuthorizationItems()
                .forEach(i -> {
                    if (Boolean.TRUE.equals(i.getContractItem().getRefundable())) {
                        i.setRefundable(true);
                        medicalAuthorization.setRefundableItems(true);
                    }
                });
    }
}
