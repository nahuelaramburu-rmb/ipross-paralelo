package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.contract.dto.ContractAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionMessage;

import java.math.BigDecimal;

public interface BaseContractAdjustmentService<T extends ContractAdjustment, D extends ContractAdjustmentDTO> extends BaseService<T, D, Long> {

    T create(Contract contract, D contractAdjustmentDTO) throws ObjectNotValidException, ObjectNotFoundException;

    void applyContractAdjustment(T contractAdjustment, BigDecimal medicalAuthorizationValue, MedicalAuthorizationItem medicalAuthorizationItem);

    void applyRestriction(T contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem, RestrictionMessage restrictionMessage);

}
