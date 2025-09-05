package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.dto.ContractItemSpecialPriceDTO;
import com.capacidad.validationapi.module.contract.model.ContractItemSpecialPrice;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.projection.ContractItemSpecialPriceProjection;
import com.capacidad.validationapi.module.contract.repository.ContractItemSpecialPriceRepository;
import com.capacidad.validationapi.module.contract.service.ContractItemSpecialPriceService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContractItemSpecialPriceServiceImpl extends BaseServiceImpl<ContractItemSpecialPrice, ContractItemSpecialPriceDTO, Long> implements ContractItemSpecialPriceService {

    private final ContractItemSpecialPriceRepository contractItemSpecialPriceRepository;

    public ContractItemSpecialPriceServiceImpl(ContractItemSpecialPriceRepository contractItemSpecialPriceRepository) {
        super(contractItemSpecialPriceRepository);
        this.contractItemSpecialPriceRepository = contractItemSpecialPriceRepository;
    }

    @Override
    public ContractItemSpecialPrice addContractItemSpecialPrice(FixedContractItem contractItem, ContractItemSpecialPriceDTO input) throws ObjectNotValidException {
        var contractItemSpecialPrice = this.mapDtoToInput(input);
        contractItemSpecialPrice.setContractItem(contractItem);
        validate(contractItemSpecialPrice);
        contractItem.getSpecialPrices().add(contractItemSpecialPrice);
        var result = contractItemSpecialPriceRepository.save(contractItemSpecialPrice);
        contractItemSpecialPriceRepository.refresh(result);
        return result;
    }

    @Override
    public long getContractItemSpecialPriceParentId(long contractItemSpecialPriceId) throws ObjectNotFoundException {
        ContractItemSpecialPriceProjection.ParentId res = contractItemSpecialPriceRepository.findProjectedById(contractItemSpecialPriceId, ContractItemSpecialPriceProjection.ParentId.class)
                .orElseThrow(() -> new ObjectNotFoundException("contractItemSpecialPrice.notFound", String.valueOf(contractItemSpecialPriceId)));
        return res.getContractItem().getId();
    }

    @Override
    public void validate(ContractItemSpecialPrice contractItemSpecialPrice) throws ObjectAlreadyExistsException {
        Optional<ContractItemSpecialPrice> specialPrice = contractItemSpecialPriceRepository.findByContractItemIdAndEventType(contractItemSpecialPrice.getContractItem().getId(),
                contractItemSpecialPrice.getEventType());
        if (specialPrice.isPresent() && !specialPrice.get().getId().equals(contractItemSpecialPrice.getId()))
            throw new ObjectAlreadyExistsException("contractItemSpecialPrice.alreadyExists");
    }

}
