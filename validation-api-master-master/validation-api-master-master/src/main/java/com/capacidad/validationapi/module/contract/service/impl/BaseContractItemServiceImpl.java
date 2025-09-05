package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.dto.ContractItemDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.repository.BaseContractItemRepository;
import com.capacidad.validationapi.module.contract.service.BaseContractItemService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;

import java.util.Collections;
import java.util.List;

public abstract class BaseContractItemServiceImpl<T extends ContractItem, D extends ContractItemDTO> extends BaseServiceImpl<T, D, Long> implements BaseContractItemService<T, D> {
    public BaseContractItemServiceImpl(BaseContractItemRepository<T, Long> repository) {
        super(repository);
    }

    @Override
    public T create(Contract contract, D input) throws ObjectNotValidException {
        return null;
    }

    @Override
    public List<ContractItem> findContractItems(Contract contract, Nomenclator nomenclator) {
        return Collections.emptyList();
    }


}
