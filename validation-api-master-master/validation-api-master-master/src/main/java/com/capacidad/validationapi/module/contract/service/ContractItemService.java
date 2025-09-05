package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.contract.dto.ContractItemDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.fasterxml.jackson.databind.JsonNode;

public interface ContractItemService extends BaseContractItemService<ContractItem, ContractItemDTO> {

    ContractItem calculateAuthorizationItemPrice(Contract contract, MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException;

    JsonNode delete(ContractItem contractItem);

}
