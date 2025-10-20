package com.capacidad.validationapi.module.nomenclator.service.impl;

import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.service.BatchItemSupportService;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.service.ContractItemService;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageItemService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.repository.NomenclatorRepository;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorSupportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Component
public class NomenclatorSupportServiceImpl implements NomenclatorSupportService {

    private final BatchItemSupportService batchItemSupportService;
    private final ContractItemService contractItemService;
    private final MedicalCoverageItemService medicalCoverageItemService;
    private final NomenclatorRepository nomenclatorRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;


    @Autowired
    public NomenclatorSupportServiceImpl(BatchItemSupportService batchItemSupportService,
                                         ContractItemService contractItemService,
                                         MedicalCoverageItemService medicalCoverageItemService,
                                         NomenclatorRepository nomenclatorRepository,
                                         ApplicationEventPublisher applicationEventPublisher,
                                         ObjectMapper objectMapper) {
        this.batchItemSupportService = batchItemSupportService;
        this.contractItemService = contractItemService;
        this.medicalCoverageItemService = medicalCoverageItemService;
        this.nomenclatorRepository = nomenclatorRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Nomenclator nomenclator) {
        deleteBatchItems(nomenclator.getBatchItems());
        deleteContractAdjustments(nomenclator.getContractAdjustments());
        deleteMedicalCoverageItems(nomenclator.getMedicalCoverageItems());
        deleteContractItems(nomenclator.getFixedContractItems());
        deleteAuditTrayItems(nomenclator);
        nomenclator.setDeleted(true);
        nomenclator.setDeletionToken(UUID.randomUUID());
        nomenclatorRepository.save(nomenclator);
        applicationEventPublisher.publishEvent(new AfterSoftDeleteEvent<>(nomenclator, nomenclatorRepository));
        ObjectNode response = objectMapper.createObjectNode();
        response.put("id", nomenclator.getId().toString());
        return response;
    }

    private void deleteBatchItems(Collection<BatchItem> batchItems) {
        for (BatchItem batchItem : batchItems) {
            if (Boolean.FALSE.equals(batchItem.getDeleted()))
                batchItemSupportService.delete(batchItem);
        }
    }

    private void deleteContractItems(Collection<FixedContractItem> contractItems) {
        for (ContractItem contractItem : contractItems) {
            if (Boolean.FALSE.equals(contractItem.getDeleted()))
                contractItemService.delete(contractItem);
        }
    }

    private void deleteContractAdjustments(Collection<ContractAdjustment> contractAdjustments) {
        for (ContractAdjustment contractAdjustment : contractAdjustments)
            contractAdjustment.getContract().getContractAdjustments().remove(contractAdjustment);
    }

    private void deleteMedicalCoverageItems(Collection<MedicalCoverageItem> medicalCoverageItems) {
        for (MedicalCoverageItem medicalCoverageItem : medicalCoverageItems) {
            if (Boolean.FALSE.equals(medicalCoverageItem.getDeleted()))
                medicalCoverageItemService.delete(medicalCoverageItem);
        }
    }

    private void deleteAuditTrayItems(Nomenclator nomenclator) {
        for (AuditTray auditTray : nomenclator.getAuditTrays())
            auditTray.getNomenclators().remove(nomenclator);
    }
}
