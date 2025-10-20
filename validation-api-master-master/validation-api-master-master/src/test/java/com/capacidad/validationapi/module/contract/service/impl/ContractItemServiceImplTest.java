package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.service.FixedContractItemService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.model.PractitionerCategory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class ContractItemServiceImplTest {

    @Mock
    private FixedContractItemService fixedContractItemService;

    @Spy
    @InjectMocks
    private ContractItemServiceImpl contractItemService;

    @Test
    public void testCalculateAuthorizationItemPriceExecuteFixedStrategyWhenContractItemTypeIsFixed() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        FixedContractItem fixedContractItem = new FixedContractItem();

        contractItemService.calculateAuthorizationItemPrice(fixedContractItem, medicalAuthorizationItem);

        verify(fixedContractItemService, times(1)).calculateAuthorizationItemPrice(fixedContractItem, medicalAuthorizationItem);
    }

    @Test
    public void testCalculateAuthorizationItemPriceDoNotExecuteFixedStrategyWhenContractItemTypeIsNotFixed() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        ContractItem contractItem = new ContractItem();

        contractItemService.calculateAuthorizationItemPrice(contractItem, medicalAuthorizationItem);

        verify(fixedContractItemService, never()).calculateAuthorizationItemPrice(any(), any());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCalculateAuthorizationItemPriceThrowsExceptionWhenEmptyContractItems() throws ObjectNotFoundException {
        Contract contract = new Contract();
        contract.setName("contractA");
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorCode("12345");

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(fixedContractItemService.findContractItems(contract, nomenclator)).thenReturn(Collections.emptyList());

        contractItemService.calculateAuthorizationItemPrice(contract, medicalAuthorizationItem);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCalculateAuthorizationItemPriceThrowsExceptionWhenNullPractitionerCategoryAndCategorizedContractItem() throws ObjectNotFoundException {
        ContractItem contractItem = new ContractItem();
        contractItem.setPractitionerCategory(new PractitionerCategory());

        Contract contract = new Contract();
        Nomenclator nomenclator = new Nomenclator();
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Practitioner practitioner = new Practitioner();
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(fixedContractItemService.findContractItems(contract, nomenclator)).thenReturn(Collections.singletonList(contractItem));

        contractItemService.calculateAuthorizationItemPrice(contract, medicalAuthorizationItem);
    }

    @Test
    public void testCalculateAuthorizationItemPriceDoNotThrowsExceptionWhenNullPractitionerCategoryAndNotCategorizedContractItem() throws ObjectNotFoundException {
        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setPractitionerCategory(null);

        Contract contract = new Contract();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Practitioner practitioner = new Practitioner();

        Nomenclator nomenclator = new Nomenclator();

        medicalAuthorization.setPractitioner(practitioner);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(fixedContractItemService.findContractItems(contract, nomenclator)).thenReturn(Collections.singletonList(fixedContractItem));

        ContractItem contractItem = contractItemService.calculateAuthorizationItemPrice(contract, medicalAuthorizationItem);

        assertThat(contractItem).isEqualTo(fixedContractItem);
    }

    @Test
    public void testCalculateAuthorizationItemPriceDoNotThrowsExceptionWhenNotNullPractitionerCategoryAndNotCategorizedContractItem() throws ObjectNotFoundException {
        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setPractitionerCategory(null);

        PractitionerCategory practitionerCategory = new PractitionerCategory();
        practitionerCategory.setId(1L);
        practitionerCategory.setName("A");

        Contract contract = new Contract();

        Nomenclator nomenclator = new Nomenclator();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Practitioner practitioner = new Practitioner();
        practitioner.setPractitionerCategory(practitionerCategory);

        medicalAuthorization.setPractitioner(practitioner);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(fixedContractItemService.findContractItems(contract, nomenclator)).thenReturn(Collections.singletonList(fixedContractItem));

        ContractItem contractItem = contractItemService.calculateAuthorizationItemPrice(contract, medicalAuthorizationItem);

        assertThat(contractItem).isEqualTo(fixedContractItem);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCalculateAuthorizationItemPriceThrowsExceptionWhenNotNullPractitionerCategoryAndCategorizedContractItemNotFound() throws ObjectNotFoundException {
        PractitionerCategory a = new PractitionerCategory();
        a.setId(1L);

        PractitionerCategory b = new PractitionerCategory();
        b.setId(2L);

        ContractItem contractItem = new ContractItem();
        contractItem.setPractitionerCategory(a);

        Contract contract = new Contract();

        Nomenclator nomenclator = new Nomenclator();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Practitioner practitioner = new Practitioner();
        practitioner.setPractitionerCategory(b);

        medicalAuthorization.setPractitioner(practitioner);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(fixedContractItemService.findContractItems(contract, nomenclator)).thenReturn(Collections.singletonList(contractItem));

        contractItemService.calculateAuthorizationItemPrice(contract, medicalAuthorizationItem);
    }

    @Test
    public void testCalculateAuthorizationItemPriceDoNotThrowsExceptionWhenNotNullPractitionerCategoryAndCategorizedContractItemFound() throws ObjectNotFoundException {
        PractitionerCategory a = new PractitionerCategory();
        a.setId(1L);

        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setPractitionerCategory(a);

        Nomenclator nomenclator = new Nomenclator();

        Contract contract = new Contract();

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Practitioner practitioner = new Practitioner();
        practitioner.setPractitionerCategory(a);

        medicalAuthorization.setPractitioner(practitioner);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        when(fixedContractItemService.findContractItems(contract, nomenclator)).thenReturn(Collections.singletonList(fixedContractItem));

        ContractItem contractItem = contractItemService.calculateAuthorizationItemPrice(contract, medicalAuthorizationItem);

        assertThat(contractItem).isEqualTo(fixedContractItem);
    }

}
