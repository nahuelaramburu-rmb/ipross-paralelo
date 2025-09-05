package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.contract.model.ContractItemSpecialPrice;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.repository.ContractItemSpecialPriceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContractItemSpecialPriceServiceImplTest {

    @Mock
    private ContractItemSpecialPriceRepository contractItemSpecialPriceRepository;

    @InjectMocks
    private ContractItemSpecialPriceServiceImpl contractItemSpecialPriceService;

    @Test
    public void testValidateFailsWhenCalendarEventExistsAndIsNotAndUpdate() {
        ContractItemSpecialPrice contractItemSpecialPrice = new ContractItemSpecialPrice();
        contractItemSpecialPrice.setId(null);
        contractItemSpecialPrice.setEventType(CalendarEventType.HOLIDAY);

        FixedContractItem fixedContractItem = new FixedContractItem();
        fixedContractItem.setId(1L);

        contractItemSpecialPrice.setContractItem(fixedContractItem);

        ContractItemSpecialPrice searchRes = new ContractItemSpecialPrice();
        searchRes.setId(1L);

        when(contractItemSpecialPriceRepository.findByContractItemIdAndEventType(contractItemSpecialPrice.getContractItem().getId(),
                contractItemSpecialPrice.getEventType()))
                .thenReturn(Optional.of(searchRes));

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> contractItemSpecialPriceService.validate(contractItemSpecialPrice));

        assertThat(exception.getMessage()).isEqualTo("contractItemSpecialPrice.alreadyExists");
    }

}
