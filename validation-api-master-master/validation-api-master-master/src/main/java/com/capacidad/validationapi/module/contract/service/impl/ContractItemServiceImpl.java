package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.contract.dto.ContractItemDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.repository.ContractItemRepository;
import com.capacidad.validationapi.module.contract.service.ContractItemService;
import com.capacidad.validationapi.module.contract.service.FixedContractItemService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.practitioner.model.PractitionerCategory;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
public class ContractItemServiceImpl extends BaseContractItemServiceImpl<ContractItem, ContractItemDTO> implements ContractItemService {

    private final FixedContractItemService fixedContractItemService;

    @Autowired
    public ContractItemServiceImpl(ContractItemRepository contractItemRepository,
                                   FixedContractItemService fixedContractItemService) {
        super(contractItemRepository);
        this.fixedContractItemService = fixedContractItemService;
    }

    @Override
    public ContractItem calculateAuthorizationItemPrice(Contract contract, MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        ContractItem contractItem = retrieveContractItem(contract, medicalAuthorizationItem);
        calculateAuthorizationItemPrice(contractItem, medicalAuthorizationItem);
        return contractItem;
    }

    @Override
    public JsonNode delete(ContractItem contractItem) {
        contractItem.setDeleted(true);
        contractItem.setDeletionToken(UUID.randomUUID());
        this.getRepository().save(contractItem);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(contractItem, this.getRepository()));
        return buildIdResponse(contractItem.getId());
    }

    private ContractItem retrieveContractItem(Contract contract, MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        List<ContractItem> contractItems = fixedContractItemService.findContractItems(contract, medicalAuthorizationItem.getNomenclator());
        if (contractItems.isEmpty())
            throw new ObjectNotFoundException(
                    "contractItem.notFoundContractMedAuthItem",
                    contract.getName(), medicalAuthorizationItem.getNomenclator().getNomenclatorCode());
        PractitionerCategory practitionerCategory = medicalAuthorizationItem.getMedicalAuthorization().getPractitioner().getPractitionerCategory();
        if (practitionerCategory != null)
            return determineCategorizedContractItem(contractItems, practitionerCategory);
        return determineNonCategorizedContractItem(contractItems);
    }

    private ContractItem determineNonCategorizedContractItem(List<ContractItem> contractItems) throws ObjectNotFoundException {
        Optional<ContractItem> optContractItem = contractItems.stream()
                .filter(item -> item.getPractitionerCategory() == null)
                .findFirst();
        if (optContractItem.isEmpty())
            throw new ObjectNotFoundException("contractItem.notFoundCategorized");
        return optContractItem.get();
    }

    private ContractItem determineCategorizedContractItem(List<ContractItem> contractItems, PractitionerCategory practitionerCategory) throws ObjectNotFoundException {
        Optional<ContractItem> optContractItem = contractItems.stream()
                .filter(item -> item.getPractitionerCategory() != null && item.getPractitionerCategory().getId().equals(practitionerCategory.getId()))
                .findFirst();
        if (optContractItem.isEmpty())
            return determineNonCategorizedContractItem(contractItems);
        return optContractItem.get();
    }

    @Override
    public void calculateAuthorizationItemPrice(ContractItem contractItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (contractItem instanceof FixedContractItem)
            fixedContractItemService.calculateAuthorizationItemPrice((FixedContractItem) contractItem, medicalAuthorizationItem);
    }
}
