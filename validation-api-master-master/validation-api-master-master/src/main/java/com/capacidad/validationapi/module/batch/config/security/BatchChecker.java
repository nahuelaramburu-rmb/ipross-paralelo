package com.capacidad.validationapi.module.batch.config.security;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.batch.service.BatchItemService;
import com.capacidad.validationapi.module.batch.service.BatchService;
import com.capacidad.validationapi.module.beneficiary.config.security.BeneficiaryChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BatchChecker {

    private final BatchService batchService;
    private final BatchItemService batchItemService;
    private final BeneficiaryChecker beneficiaryChecker;

    @Autowired
    public BatchChecker(BatchService batchService,
                        BatchItemService batchItemService,
                        BeneficiaryChecker beneficiaryChecker) {
        this.batchService = batchService;
        this.batchItemService = batchItemService;
        this.beneficiaryChecker = beneficiaryChecker;
    }

    public boolean hasAccessToBatchItem(long batchItemId) throws ObjectNotFoundException {
        long batchId = batchItemService.findBatchItemAndGetParentId(batchItemId);
        return hasAccessToBatch(batchId);
    }

    public boolean hasAccessToBatch(long batchId) throws ObjectNotFoundException {
        long beneficiaryId = batchService.findBatchAndGetBeneficiaryId(batchId);
        return beneficiaryChecker.hasAccessToBeneficiary(beneficiaryId);
    }

}
