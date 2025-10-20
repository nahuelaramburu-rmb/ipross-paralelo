package com.capacidad.validationapi.module.settlement.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.settlement.dto.SettlementDTO;
import com.capacidad.validationapi.module.settlement.dto.SettlementUpdateDTO;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.projection.SettlementProjection;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Set;

public interface SettlementService extends BaseService<Settlement, SettlementDTO, Long> {

    SettlementProjection createSettlement(SettlementDTO settlementDTO) throws ObjectNotValidException, ObjectNotFoundException;

    /**
     * Creates or update existent opened settlement with an specific MedicalAuthorization.
     * Every item is validated on already settled, status, subtotal, expiration and report-required.
     * Failure on items DO NOT implies failure on the entire transaction. Item is not settled on failure.
     *
     * @param medicalAuthorization MedicalAuthorization reference.
     **/
    void createOrUpdateFromMedicalAuthorization(MedicalAuthorization medicalAuthorization);

    /**
     * Creates or update existent opened settlement with an specific MedicalAuthorizationItem.
     * Item is validated on already settled, status, subtotal, expiration and report-required.
     * Failure on item DO NOT implies failure on the entire transaction. Item is not settled on failure.
     *
     * @param medicalAuthorizationItem MedicalAuthorizationItem reference.
     **/
    void createOrUpdateFromMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem);

    SettlementProjection updateSettlement(long settlementId, SettlementUpdateDTO updateDTO) throws ObjectNotFoundException, ObjectNotValidException;

    ByteArrayOutputStream generateReceipt(long budgetId) throws ObjectNotValidException, ObjectNotFoundException;

    ByteArrayOutputStream generateItemsReceipt(long settlementId, Set<Long> itemIds) throws ObjectNotValidException;

    ByteArrayOutputStream generateBeneficiaryItemsReceipt(long settlementId, String beneficiaryCode) throws ObjectNotValidException;

    boolean belongsToPractitioner(long settlementId);

    boolean belongsToContract(long settlementId);

    void closeAllIfContractDayMatchesDate(LocalDate dateToCompare);

}
