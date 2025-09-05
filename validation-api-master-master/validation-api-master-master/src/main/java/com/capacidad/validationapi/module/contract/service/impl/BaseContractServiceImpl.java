package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.dto.ContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.repository.BaseContractRepository;
import com.capacidad.validationapi.module.contract.service.BaseContractService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseContractServiceImpl<T extends Contract, D extends ContractDTO> extends BaseServiceImpl<T, D, Long> implements BaseContractService<T, D> {

    private final BaseContractRepository<T, Long> repository;

    public BaseContractServiceImpl(BaseContractRepository<T, Long> repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public void validate(T contract) throws ObjectNotValidException {
        if (contract.getDateFrom().isAfter(contract.getDateTo()))
            throw new ObjectNotValidException("generic.dateFromDateTo");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long contractId) throws ObjectNotFoundException {
        T contract = this.findById(contractId);
        UUID deletionToken = UUID.randomUUID();
        contract.setDeleted(true);
        contract.setDeletionToken(deletionToken);
        contract.getContractAdjustments().clear();
        contract.getRuleConfigurations().clear();
        contract.getPractitioners().forEach(p -> p.getContracts().remove(contract));
        handleContractItemsDelete(contract);
        this.getRepository().save(contract);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(contract, this.getRepository()));
        return this.buildIdResponse(contract.getId());
    }

    private void handleContractItemsDelete(Contract contract) {
        contract.getContractItems().forEach(i -> {
            if (Boolean.FALSE.equals(i.getDeleted())) {
                i.setDeleted(true);
                i.setDeletionToken(contract.getDeletionToken());
            }
        });
        Set<ContractItem> itemsToHardDelete = contract.getContractItems().stream()
                .filter(i -> i.getMedicalAuthorizationItems().isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        contract.getContractItems().removeAll(itemsToHardDelete);
    }

    @Override
    public BaseContractRepository<T, Long> getRepository() {
        return repository;
    }

}
