package com.capacidad.validationapi.module.nomenclator.service.impl;

import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.service.BatchItemSupportService;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.service.ContractItemService;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageItemService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.repository.NomenclatorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NomenclatorSupportServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private BatchItemSupportService batchItemSupportService;

    @Mock
    private ContractItemService contractItemService;

    @Mock
    private MedicalCoverageItemService medicalCoverageItemService;

    @Mock
    private NomenclatorRepository nomenclatorRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private NomenclatorSupportServiceImpl nomenclatorSupportService;

    @Test
    public void testDeleteExecuteSuccessfully() {
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        BatchItem batchItem = new BatchItem();
        BatchItem batchItem1 = new BatchItem();
        batchItem1.setDeleted(true);
        nomenclator.getBatchItems().add(batchItem);
        nomenclator.getBatchItems().add(batchItem1);

        FixedContractItem contractItem = new FixedContractItem();
        FixedContractItem contractItem1 = new FixedContractItem();
        contractItem1.setDeleted(true);
        nomenclator.getFixedContractItems().add(contractItem);
        nomenclator.getFixedContractItems().add(contractItem1);

        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        MedicalCoverageItem medicalCoverageItem1 = new MedicalCoverageItem();
        medicalCoverageItem1.setDeleted(true);
        nomenclator.getMedicalCoverageItems().add(medicalCoverageItem);
        nomenclator.getMedicalCoverageItems().add(medicalCoverageItem1);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        Contract contract = new Contract();
        contract.getContractAdjustments().add(contractAdjustment);
        contractAdjustment.setContract(contract);
        nomenclator.getContractAdjustments().add(contractAdjustment);

        AuditTray auditTray = new AuditTray();
        auditTray.getNomenclators().add(nomenclator);
        nomenclator.getAuditTrays().add(auditTray);

        doReturn(new ObjectMapper().createObjectNode()).when(objectMapper).createObjectNode();

        JsonNode result = nomenclatorSupportService.delete(nomenclator);

        assertThat(result.get("id").asLong()).isEqualTo(nomenclator.getId());
        assertThat(nomenclator.getDeleted()).isTrue();
        assertThat(nomenclator.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(contract.getContractAdjustments()).isEmpty();
        assertThat(auditTray.getNomenclators()).isEmpty();

        verify(nomenclatorRepository, times(1)).save(nomenclator);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
        verify(batchItemSupportService, times(1)).delete(batchItem);
        verify(contractItemService, times(1)).delete(contractItem);
        verify(medicalCoverageItemService, times(1)).delete(medicalCoverageItem);
    }

}
