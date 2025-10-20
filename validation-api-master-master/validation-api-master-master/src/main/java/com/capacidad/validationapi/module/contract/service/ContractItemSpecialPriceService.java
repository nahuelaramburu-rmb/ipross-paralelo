package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.contract.dto.ContractItemSpecialPriceDTO;
import com.capacidad.validationapi.module.contract.model.ContractItemSpecialPrice;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;

public interface ContractItemSpecialPriceService extends BaseService<ContractItemSpecialPrice, ContractItemSpecialPriceDTO, Long> {

    ContractItemSpecialPrice addContractItemSpecialPrice(FixedContractItem contractItem, ContractItemSpecialPriceDTO input) throws ObjectNotValidException;

    long getContractItemSpecialPriceParentId(long contractItemSpecialPriceId) throws ObjectNotFoundException;

}
