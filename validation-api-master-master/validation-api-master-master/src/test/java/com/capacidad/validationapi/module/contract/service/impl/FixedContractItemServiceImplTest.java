package com.capacidad.validationapi.module.contract.service.impl;


import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.calendar.service.HolidayService;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItemSpecialPrice;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.repository.FixedContractItemRepository;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.reference.ChargeTypeReference;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.PractitionerCategory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FixedContractItemServiceImplTest {

    @Mock
    private FixedContractItemRepository fixedContractItemRepository;

    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private FixedContractItemServiceImpl fixedContractItemService;

    @Test
    public void testCalculateAuthorizationItemPriceReturnsExpectedValueWithoutAnyCalendarEvent() {
        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setValue(new BigDecimal(375));
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        when(holidayService.isDateHolidayOrWeekend(LocalDate.now())).thenReturn(Optional.empty());

        fixedContractItemService.calculateAuthorizationItemPrice(fixedContractItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(fixedContractItem.getValue()
                .multiply(new BigDecimal(medicalAuthorizationItem.getQuantity())
                        .setScale(2, RoundingMode.HALF_UP)));
        assertThat(medicalAuthorizationItem.getCalendarEventType()).isNull();
    }

    @Test
    public void testCalculateAuthorizationItemPriceReturnsExpectedValueWithCalendarEventNoSpecialPrices() {
        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setValue(new BigDecimal(375));
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        when(holidayService.isDateHolidayOrWeekend(LocalDate.now())).thenReturn(Optional.of(CalendarEventType.HOLIDAY));

        fixedContractItemService.calculateAuthorizationItemPrice(fixedContractItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(fixedContractItem.getValue()
                .multiply(new BigDecimal(medicalAuthorizationItem.getQuantity())
                        .setScale(2, RoundingMode.HALF_UP)));
        assertThat(medicalAuthorizationItem.getCalendarEventType()).isNull();
    }

    @Test
    public void testCalculateAuthorizationItemPriceReturnsExpectedValueWithCalendarEventAndSpecialPriceFixedValue() {
        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setValue(new BigDecimal(375));
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        ContractItemSpecialPrice contractItemSpecialPrice = new ContractItemSpecialPrice();
        contractItemSpecialPrice.setEventType(CalendarEventType.WEEKEND);
        contractItemSpecialPrice.setChargeType(ChargeTypeReference.FIXED_VALUE.getInstance());
        contractItemSpecialPrice.setSpecialValue(new BigDecimal(471));

        fixedContractItem.getSpecialPrices().add(contractItemSpecialPrice);

        when(holidayService.isDateHolidayOrWeekend(LocalDate.now())).thenReturn(Optional.of(contractItemSpecialPrice.getEventType()));

        fixedContractItemService.calculateAuthorizationItemPrice(fixedContractItem, medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(contractItemSpecialPrice.getSpecialValue()
                .multiply(new BigDecimal(medicalAuthorizationItem.getQuantity())
                        .setScale(2, RoundingMode.HALF_UP)));
        assertThat(medicalAuthorizationItem.getCalendarEventType()).isEqualTo(contractItemSpecialPrice.getEventType());
    }

    @Test
    public void testCalculateAuthorizationItemPriceReturnsExpectedValueWithCalendarEventAndSpecialPricePercentageValue() {
        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setValue(new BigDecimal(375));
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);

        ContractItemSpecialPrice contractItemSpecialPrice = new ContractItemSpecialPrice();
        contractItemSpecialPrice.setEventType(CalendarEventType.HOLIDAY);
        contractItemSpecialPrice.setChargeType(ChargeTypeReference.PERCENTAGE.getInstance());
        contractItemSpecialPrice.setSpecialValue(new BigDecimal(10));

        fixedContractItem.getSpecialPrices().add(contractItemSpecialPrice);

        when(holidayService.isDateHolidayOrWeekend(LocalDate.now())).thenReturn(Optional.of(contractItemSpecialPrice.getEventType()));

        fixedContractItemService.calculateAuthorizationItemPrice(fixedContractItem, medicalAuthorizationItem);

        BigDecimal baseValue = fixedContractItem.getValue()
                .multiply(new BigDecimal(medicalAuthorizationItem.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(medicalAuthorizationItem.getSubtotal()).isEqualTo(baseValue.add(baseValue.multiply(contractItemSpecialPrice.getSpecialValue())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)));
        assertThat(medicalAuthorizationItem.getCalendarEventType()).isEqualTo(contractItemSpecialPrice.getEventType());
    }

    @Test(expected = ObjectNotValidException.class)
    public void testValidateFailsWhenNotCategorizedItemsAlreadyExistAndCategoryIsNotNull() throws ObjectNotValidException {
        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PractitionerCategory practitionerCategory = new PractitionerCategory();
        practitionerCategory.setId(1L);
        practitionerCategory.setName("A");

        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setContract(contract);
        fixedContractItem.setNomenclator(nomenclator);
        fixedContractItem.setPractitionerCategory(practitionerCategory);

        when(fixedContractItemRepository.existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNull(contract.getId(), nomenclator.getId()))
                .thenReturn(true);

        fixedContractItemService.validate(fixedContractItem);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testValidateFailsWhenCategorizedItemsAlreadyExistAndCategoryIsNull() throws ObjectNotValidException {
        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setContract(contract);
        fixedContractItem.setNomenclator(nomenclator);
        fixedContractItem.setPractitionerCategory(null);

        when(fixedContractItemRepository.existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNotNull(contract.getId(), nomenclator.getId()))
                .thenReturn(true);

        fixedContractItemService.validate(fixedContractItem);
    }

    @Test
    public void testValidateDoNotFailsWhenCategorizedItemsAlreadyExistAndCategoryIsNotNull() throws ObjectNotValidException {
        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PractitionerCategory practitionerCategory = new PractitionerCategory();
        practitionerCategory.setId(1L);
        practitionerCategory.setName("A");

        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setContract(contract);
        fixedContractItem.setNomenclator(nomenclator);
        fixedContractItem.setPractitionerCategory(practitionerCategory);

        when(fixedContractItemRepository.existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNull(contract.getId(), nomenclator.getId()))
                .thenReturn(false);

        fixedContractItemService.validate(fixedContractItem);
    }

    @Test
    public void testValidateDoNotFailsWhenCategorizedItemsDoNotExistsAndCategoryIsNull() throws ObjectNotValidException {
        Contract contract = new Contract();
        contract.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setContract(contract);
        fixedContractItem.setNomenclator(nomenclator);
        fixedContractItem.setPractitionerCategory(null);

        when(fixedContractItemRepository.existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNotNull(contract.getId(), nomenclator.getId()))
                .thenReturn(false);

        fixedContractItemService.validate(fixedContractItem);
    }

}
