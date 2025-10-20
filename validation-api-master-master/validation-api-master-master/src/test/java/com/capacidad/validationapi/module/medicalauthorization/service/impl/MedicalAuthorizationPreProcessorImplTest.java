package com.capacidad.validationapi.module.medicalauthorization.service.impl;


import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.service.BatchService;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.service.ContractService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationPreProcessorImplTest {

    @Mock
    private ContractService contractService;

    @Mock
    private BatchService batchService;

    @Mock
    private MedicalCoverageService medicalCoverageService;

    @InjectMocks
    private MedicalAuthorizationPreProcessorImpl medicalAuthorizationPreProcessor;

    @Test
    public void testFindApplicableContractThrowsExceptionWhenEmptyContracts() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPractitioner(new Practitioner());
        medicalAuthorization.setSelectedContract(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationPreProcessor.findApplicableContract(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("practitioner.contractRequirement");
    }

    @Test
    public void testFindApplicableContractThrowsExceptionWhenMultiplePractitionerContractsButNotSelection() {
        Contract contract = new Contract();
        Contract contract1 = new Contract();

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationPreProcessor.findApplicableContract(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.contractRequirement");
    }

    @Test
    public void testFindApplicableContractThrowsExceptionWhenMultiplePractitionerContractsAndInvalidSelection() throws ObjectNotFoundException {
        Contract contract = new Contract();
        Contract contract1 = new Contract();

        Contract contract3 = new Contract();
        contract3.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(contract3);

        when(contractService.findById(contract3.getId())).thenReturn(contract3);
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationPreProcessor.findApplicableContract(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.practitionerInvalidContract");
    }

    @Test
    public void testFindApplicableContractReturnsSuccessfullyWhenPractitionerContainsOneContractOnly() throws ObjectNotValidException, ObjectNotFoundException {
        Contract contract = new Contract();
        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPractitioner(practitioner);

        Contract result = medicalAuthorizationPreProcessor.findApplicableContract(medicalAuthorization);

        assertThat(result).isEqualTo(contract);
    }

    @Test
    public void testFindApplicableContractReturnsSuccessfullyWhenPractitionerContainsMultipleContractsAndValidSelection() throws ObjectNotValidException, ObjectNotFoundException {
        Contract contract = new Contract();

        Contract contract1 = new Contract();
        contract1.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(contract1);

        when(contractService.findById(contract1.getId())).thenReturn(contract1);

        Contract result = medicalAuthorizationPreProcessor.findApplicableContract(medicalAuthorization);

        assertThat(result).isEqualTo(contract1);
    }

    @Test
    public void testPreProcessThrowsExceptionWhenNotActiveContract() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setPaymentMethod(VOLUNTARY.getInstance());
        medicalAuthorization.setBeneficiary(beneficiary);

        Contract contract = new Contract();

        Contract contract1 = new Contract();
        contract1.setActive(false);
        contract1.setName("contract");
        contract1.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(contract1);

        when(contractService.findById(contract1.getId())).thenReturn(contract1);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationPreProcessor.preProcess(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("contract.notActive");
    }

    @Test
    public void testPreProcessExecutesSuccessfullyWhenBatchAuthorization() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setPaymentMethod(VOLUNTARY.getInstance());
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPreMedicalAuthorization(null);

        Contract contract = new Contract();

        Contract contract1 = new Contract();
        contract1.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(contract1);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setContractItem(new ContractItem());

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        Batch batch = new Batch();
        batch.setId(1L);
        BatchItem batchItem = new BatchItem();
        batchItem.setNomenclator(nomenclator);
        batch.getBatchItems().add(batchItem);

        when(contractService.findById(contract1.getId())).thenReturn(contract1);
        when(batchService.findApplicableBatch(medicalAuthorization)).thenReturn(Optional.of(batch));
        when(batchService.findApplicableBatchItem(medicalAuthorizationItem)).thenReturn(Optional.of(batchItem));

        medicalAuthorizationPreProcessor.preProcess(medicalAuthorization);

        verify(contractService, times(1)).calculateAuthorizationItemPrice(medicalAuthorizationItem);
        verify(medicalCoverageService, never()).calculateAuthorizationItemCharges(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(new BigDecimal(0));
        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(new BigDecimal(0));
        assertThat(medicalAuthorization.getBatch()).isNotNull();
        assertThat(medicalAuthorizationItem.getBatchItem()).isNotNull();
        assertThat(medicalAuthorizationItem.getRefundable()).isFalse();
    }

    @Test
    public void testPreProcessExecutesSuccessfullyWhenBatchAuthorizationButItemDoesNotExists() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setPaymentMethod(VOLUNTARY.getInstance());
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPreMedicalAuthorization(null);

        Contract contract = new Contract();

        Contract contract1 = new Contract();
        contract1.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(contract1);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setContractItem(new ContractItem());

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        Batch batch = new Batch();
        batch.setId(1L);

        when(contractService.findById(contract1.getId())).thenReturn(contract1);
        when(batchService.findApplicableBatch(medicalAuthorization)).thenReturn(Optional.of(batch));
        when(batchService.findApplicableBatchItem(medicalAuthorizationItem)).thenReturn(Optional.empty());

        medicalAuthorizationPreProcessor.preProcess(medicalAuthorization);

        verify(contractService, times(1)).calculateAuthorizationItemPrice(medicalAuthorizationItem);
        verify(medicalCoverageService, times(1)).calculateAuthorizationItemCharges(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorization.getBatch()).isNotNull();
        assertThat(medicalAuthorizationItem.getBatchItem()).isNull();
        assertThat(medicalAuthorizationItem.getRefundable()).isFalse();
    }

    @Test
    public void testPreProcessExecutesSuccessfullyWhenCoverageAuthorization() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setPaymentMethod(VOLUNTARY.getInstance());
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPreMedicalAuthorization(null);

        Contract contract = new Contract();

        Contract contract1 = new Contract();
        contract1.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(contract1);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        medicalAuthorizationItem.setMedicalCoverageItem(new MedicalCoverageItem());
        ContractItem contractItem = new ContractItem();
        contractItem.setRefundable(true);
        medicalAuthorizationItem.setContractItem(contractItem);

        when(medicalCoverageService.findApplicableCoverage(medicalAuthorizationItem)).thenReturn(medicalCoverage);
        when(contractService.findById(contract1.getId())).thenReturn(contract1);

        medicalAuthorizationPreProcessor.preProcess(medicalAuthorization);

        verify(contractService, times(1)).calculateAuthorizationItemPrice(medicalAuthorizationItem);
        verify(medicalCoverageService, times(1)).calculateAuthorizationItemCharges(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorization.getMedicalCoverages()).contains(medicalCoverage);
        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isNotEqualTo(new BigDecimal(0));
        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isNotEqualTo(new BigDecimal(0));
        assertThat(medicalAuthorizationItem.getRefundable()).isTrue();
    }

    @Test
    public void testPreProcessExecutesSuccessfullyWhenPreMedAuthAndPredefinedCharge() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setPaymentMethod(VOLUNTARY.getInstance());
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPreMedicalAuthorization(new PreMedicalAuthorization());

        Contract contract = new Contract();
        Contract contract1 = new Contract();
        contract1.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setSelectedContract(contract1);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setId(1L);

        BigDecimal predefinedSubtotal = new BigDecimal(123);
        medicalAuthorizationItem.setMedicalCoverageItem(new MedicalCoverageItem());
        medicalAuthorizationItem.setChargeSubtotal(predefinedSubtotal);
        medicalAuthorizationItem.setChargeUnitPrice(predefinedSubtotal);

        when(medicalCoverageService.findApplicableCoverage(medicalAuthorizationItem)).thenReturn(medicalCoverage);
        when(contractService.findById(contract1.getId())).thenReturn(contract1);

        medicalAuthorizationPreProcessor.preProcess(medicalAuthorization);

        verify(contractService, times(1)).calculateAuthorizationItemPrice(medicalAuthorizationItem);
        verify(medicalCoverageService, never()).calculateAuthorizationItemCharges(medicalAuthorizationItem);

        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorization.getMedicalCoverages()).contains(medicalCoverage);
        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(VALIDATION_APPROVED.getId());
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(predefinedSubtotal);
        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(predefinedSubtotal);
    }


}
