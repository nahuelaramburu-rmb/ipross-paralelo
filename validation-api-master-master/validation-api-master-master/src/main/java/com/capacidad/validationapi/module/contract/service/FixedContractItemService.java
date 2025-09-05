package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.contract.dto.ContractItemSpecialPriceDTO;
import com.capacidad.validationapi.module.contract.dto.FixedContractItemDTO;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.projection.ContractItemSpecialPriceProjection;

public interface FixedContractItemService extends BaseContractItemService<FixedContractItem, FixedContractItemDTO> {

    ContractItemSpecialPriceProjection addContractItemSpecialPrice(long fixedContractItemId, ContractItemSpecialPriceDTO input) throws ObjectNotFoundException, ObjectNotValidException;

}
