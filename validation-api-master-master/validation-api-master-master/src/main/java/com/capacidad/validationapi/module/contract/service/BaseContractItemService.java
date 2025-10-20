package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.contract.dto.ContractItemDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;

import java.util.List;

public interface BaseContractItemService<T extends ContractItem, D extends ContractItemDTO> extends BaseService<T, D, Long> {

    T create(Contract contract, D input) throws ObjectNotValidException;

    List<ContractItem> findContractItems(Contract contract, Nomenclator nomenclator);

    void calculateAuthorizationItemPrice(T contractItem, MedicalAuthorizationItem medicalAuthorizationItem);

}
