package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationItemDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MedicalAuthorizationItemService extends BaseService<MedicalAuthorizationItem, MedicalAuthorizationItemDTO, Long> {

    Set<MedicalAuthorizationItem> findNotSettledItemsByPractitionerContractAndCollectionOfIds(Practitioner practitioner, Contract contract, Collection<Long> medicalAuthorizationItemIds);

    List<MedicalAuthorizationItem> saveAll(Collection<MedicalAuthorizationItem> medicalAuthorizationItems);

    long countNotTransitByContractAdjustmentAndPractitioner(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem);

    BigDecimal sumNotTransitSubtotalsByContractAdjustmentAndPractitioner(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem);

    long countAllByBatchItemInPeriod(BatchItem batchItem);

    MedicalAuthorizationItem save(MedicalAuthorizationItem medicalAuthorizationItem);

    List<MedicalAuthorizationItem> getBeneficiaryPractitionerAuthorizationItemsInPeriod(MedicalAuthorizationItem medicalAuthorizationItem, LocalDateTime from);

    List<MedicalAuthorizationItem> getBeneficiaryAuthorizationItemAmountInPeriod(MedicalAuthorizationItem medicalAuthorizationItem, LocalDateTime from);

    int countBeneficiaryAuthorizationItemAmount(MedicalAuthorizationItem medicalAuthorizationItem, Status status);

    int countBeneficiaryFreeAuthorizationItemAmountInPeriod(MedicalAuthorizationItem medicalAuthorizationItem, long days);
}
