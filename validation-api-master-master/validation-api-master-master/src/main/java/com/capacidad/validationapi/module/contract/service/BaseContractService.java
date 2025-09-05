package com.capacidad.validationapi.module.contract.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.contract.dto.ContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;

public interface BaseContractService<T extends Contract, D extends ContractDTO> extends BaseService<T, D, Long> {
}
