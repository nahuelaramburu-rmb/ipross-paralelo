package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.repository.MedicalAuthorizationItemRepository;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationItemServiceImplTest {

    @Mock
    private MedicalAuthorizationItemRepository medicalAuthorizationItemRepository;

    @Mock
    private MedicalAuthorizationItemContractAdjustmentQueryResolver contractAdjustmentQueryResolver;

    @Spy
    @InjectMocks
    private MedicalAuthorizationItemServiceImpl medicalAuthorizationItemService;

    @Test
    public void countNotTransitByContractAdjustmentAndPractitionerWhenNullRegionAndContractScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setCity(city);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.CONTRACT);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .countNotTransitByCityAndContractScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(35L);

        long result = medicalAuthorizationItemService.countNotTransitByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(35L);
    }

    @Test
    public void countNotTransitByContractAdjustmentAndPractitionerWhenRegionAndContractScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        Region region = new Region();
        region.getCities().add(city);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setRegion(region);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.CONTRACT);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .countNotTransitByRegionAndContractScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(35L);

        long result = medicalAuthorizationItemService.countNotTransitByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(35L);
    }

    @Test
    public void countNotTransitByContractAdjustmentAndPractitionerWhenNullRegionAndPractitionerScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setCity(city);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.PRACTITIONER);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .countNotTransitByCityAndPractitionerScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(35L);

        long result = medicalAuthorizationItemService.countNotTransitByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(35L);
    }

    @Test
    public void countNotTransitByContractAdjustmentAndPractitionerWhenRegionAndPractitionerScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        Region region = new Region();
        region.getCities().add(city);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setRegion(region);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.PRACTITIONER);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .countNotTransitByRegionAndPractitionerScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(35L);

        long result = medicalAuthorizationItemService.countNotTransitByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(35L);
    }

    @Test
    public void sumNotTransitSubtotalsByContractAdjustmentAndPractitionerWhenNullRegionAndContractScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setCity(city);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.CONTRACT);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .sumNotTransitByCityAndContractScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(new BigDecimal(35));

        BigDecimal result = medicalAuthorizationItemService.sumNotTransitSubtotalsByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(new BigDecimal(35));
    }

    @Test
    public void sumNotTransitSubtotalsByContractAdjustmentAndPractitionerWhenRegionAndContractScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        Region region = new Region();
        region.getCities().add(city);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setRegion(region);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.CONTRACT);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .sumNotTransitByRegionAndContractScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(new BigDecimal(35));

        BigDecimal result = medicalAuthorizationItemService.sumNotTransitSubtotalsByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(new BigDecimal(35));
    }

    @Test
    public void sumNotTransitSubtotalsByContractAdjustmentAndPractitionerWhenNullRegionAndPractitionerScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setCity(city);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.PRACTITIONER);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .sumNotTransitByCityAndPractitionerScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(new BigDecimal(35));

        BigDecimal result = medicalAuthorizationItemService.sumNotTransitSubtotalsByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(new BigDecimal(35));
    }

    @Test
    public void sumNotTransitSubtotalsByContractAdjustmentAndPractitionerWhenRegionAndPractitionerScope() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(2L);

        City city = new City();
        city.setId(3L);

        Region region = new Region();
        region.getCities().add(city);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setContract(contract);
        contractAdjustment.setRegion(region);
        contractAdjustment.setNomenclator(nomenclator);
        contractAdjustment.setScope(ContractAdjustmentScope.PRACTITIONER);
        contractAdjustment.setPeriod(Period.MONTHLY);

        when(contractAdjustmentQueryResolver
                .sumNotTransitByRegionAndPractitionerScope(contractAdjustment, medicalAuthorizationItem))
                .thenReturn(new BigDecimal(35));

        BigDecimal result = medicalAuthorizationItemService.sumNotTransitSubtotalsByContractAdjustmentAndPractitioner
                (contractAdjustment, medicalAuthorizationItem);

        assertThat(result).isEqualTo(new BigDecimal(35));
    }

}
