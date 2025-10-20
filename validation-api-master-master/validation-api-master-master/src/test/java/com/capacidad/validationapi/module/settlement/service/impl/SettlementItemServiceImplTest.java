package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import com.capacidad.validationapi.module.settlement.repository.SettlementItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.CLOSED_SETTLEMENT;
import static com.capacidad.validationapi.module.general.reference.StatusReference.OPEN_SETTLEMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettlementItemServiceImplTest {

    @Mock
    private SettlementItemRepository settlementItemRepository;

    @InjectMocks
    private SettlementItemServiceImpl settlementItemService;


    @Test
    public void testRemoveFromSettlementFailsWhenAlreadyClosed() {
        SettlementItem settlementItem = new SettlementItem();
        Set<SettlementItem> settlementItems = new HashSet<>();

        Settlement settlement = new Settlement();
        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());
        settlement.setStatus(closed);

        settlementItem.setSettlement(settlement);

        settlementItems.add(settlementItem);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);

        when(settlementItemRepository.findAllByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(settlementItems);

        RuntimeException exception = (RuntimeException) catchThrowable(() -> settlementItemService.removeItems(medicalAuthorization));

        assertThat(exception.getMessage()).contains("settlement.cannotRemoveFromAlreadyClosedSettle");
    }

    @Test
    public void testRemoveFromSettlementDoNotFailsWhenNotClosed() {
        SettlementItem settlementItem1 = new SettlementItem();
        settlementItem1.setSubtotal(new BigDecimal("167.5"));
        SettlementItem settlementItem2 = new SettlementItem();
        settlementItem2.setSubtotal(new BigDecimal("345.54"));
        Set<SettlementItem> settlementItems = new HashSet<>();

        Settlement settlement = new Settlement();
        Status open = new Status();
        open.setId(OPEN_SETTLEMENT.getId());
        settlement.setStatus(open);
        BigDecimal settlementTotal = new BigDecimal("780.95");
        settlement.setTotal(settlementTotal);

        settlementItem1.setSettlement(settlement);
        settlementItem2.setSettlement(settlement);

        settlementItems.add(settlementItem1);
        settlementItems.add(settlementItem2);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);

        when(settlementItemRepository.findAllByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(settlementItems);

        settlementItemService.removeItems(medicalAuthorization);

        assertThat(settlement.getSettlementItems().size()).isZero();
        assertThat(settlement.getTotal()).isEqualTo(settlementTotal
                .subtract(settlementItem1.getSubtotal().add(settlementItem2.getSubtotal())));
    }

}
