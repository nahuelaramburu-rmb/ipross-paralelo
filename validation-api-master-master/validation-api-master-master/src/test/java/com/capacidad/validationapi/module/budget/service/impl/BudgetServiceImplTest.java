package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.budget.model.BeneficiaryBudget;
import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.budget.projection.BudgetProjection;
import com.capacidad.validationapi.module.budget.repository.BudgetItemRepository;
import com.capacidad.validationapi.module.budget.repository.BudgetRepository;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.NOT_PAYED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PAYED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private Utils utils;

    @Spy
    @InjectMocks
    private BudgetServiceImpl budgetService;


    @Test
    public void testRemoveFromBudgetDoNothingWhenBudgetItemListIsEmpty() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);

        when(budgetItemRepository.findAllByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(new HashSet<>());
        budgetService.removeFromBudget(medicalAuthorization);

        verify(budgetItemRepository, never()).deleteAll(any());
        verify(budgetRepository, never()).save(any());
    }

    @Test
    public void testRemoveFromBudgetFailsWhenBudgetItemPayed() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);

        Status payed = new Status();
        payed.setId(PAYED.getId());

        Status notPayed = new Status();
        notPayed.setId(NOT_PAYED.getId());

        BeneficiaryBudget budget = new BeneficiaryBudget();
        BigDecimal initialValue = new BigDecimal("1485.26");
        budget.setTotal(initialValue);
        budget.setStatus(payed);

        PractitionerBudget budget1 = new PractitionerBudget();
        BigDecimal initialValue1 = new BigDecimal("1324.54");
        budget1.setTotal(initialValue1);
        budget1.setStatus(notPayed);

        BudgetItem budgetItem = new BudgetItem();
        budgetItem.setChargeSubtotal(new BigDecimal("143.39"));
        budgetItem.setBudget(budget);

        BudgetItem budgetItem1 = new BudgetItem();
        budgetItem1.setChargeSubtotal(new BigDecimal("235.11"));
        budgetItem1.setBudget(budget1);

        Set<BudgetItem> budgetItems = new HashSet<>();
        budgetItems.add(budgetItem);
        budgetItems.add(budgetItem1);

        when(budgetItemRepository.findAllByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(budgetItems);

        RuntimeException exception = (RuntimeException) catchThrowable(() -> budgetService.removeFromBudget(medicalAuthorization));

        assertThat(exception.getMessage()).contains("budget.alreadyPayed");
    }

    @Test
    public void testRemoveFromBudgetRemovesAndSubtractSuccessfullyWhenBudgetItemsNotEmpty() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);

        Status notPayed = new Status();
        notPayed.setId(NOT_PAYED.getId());

        Budget budget = new Budget();
        BigDecimal initialValue = new BigDecimal("1485.26");
        budget.setTotal(initialValue);
        budget.setStatus(notPayed);

        Budget budget1 = new Budget();
        BigDecimal initialValue1 = new BigDecimal("1324.54");
        budget1.setTotal(initialValue1);
        budget1.setStatus(notPayed);

        BudgetItem budgetItem = new BudgetItem();
        budgetItem.setChargeSubtotal(new BigDecimal("143.39"));
        budgetItem.setBudget(budget);

        BudgetItem budgetItem1 = new BudgetItem();
        budgetItem1.setChargeSubtotal(new BigDecimal("235.11"));
        budgetItem1.setBudget(budget);

        BudgetItem budgetItem2 = new BudgetItem();
        budgetItem2.setChargeSubtotal(new BigDecimal("353.39"));
        budgetItem2.setBudget(budget1);

        BudgetItem budgetItem3 = new BudgetItem();
        budgetItem3.setChargeSubtotal(new BigDecimal("478.41"));
        budgetItem3.setBudget(budget1);

        Set<BudgetItem> budgetItems = new HashSet<>();
        budgetItems.add(budgetItem);
        budgetItems.add(budgetItem1);
        budgetItems.add(budgetItem2);
        budgetItems.add(budgetItem3);

        when(budgetItemRepository.findAllByMedicalAuthorizationId(medicalAuthorization.getId())).thenReturn(budgetItems);
        budgetService.removeFromBudget(medicalAuthorization);

        verify(budgetItemRepository, times(1)).deleteAll(budgetItems);
        verify(budgetRepository, times(1)).saveAll(any());
        assertThat(budget.getTotal())
                .isEqualTo(initialValue
                        .subtract(budgetItem.getChargeSubtotal())
                        .subtract(budgetItem1.getChargeSubtotal())
                        .setScale(2, RoundingMode.HALF_UP));
        assertThat(budget1.getTotal())
                .isEqualTo(initialValue1
                        .subtract(budgetItem2.getChargeSubtotal())
                        .subtract(budgetItem3.getChargeSubtotal())
                        .setScale(2, RoundingMode.HALF_UP));
    }

    @Test(expected = ObjectNotValidException.class)
    public void testCloseBudgetThrowsExceptionWhenAlreadyPayed() throws ObjectNotFoundException, ObjectNotValidException {
        Budget budget = new Budget();
        budget.setId(1L);

        Status payed = new Status();
        payed.setId(PAYED.getId());

        budget.setStatus(payed);

        doReturn(budget).when(budgetService).findById(budget.getId());

        budgetService.closeBudget(budget.getId());
    }

    @Test
    public void testCloseBudgetUpdatesSuccessfullyWhenBudgetNotPayed() throws ObjectNotFoundException, ObjectNotValidException {
        Budget budget = new Budget();
        budget.setId(1L);

        Status notPayed = new Status();
        notPayed.setId(StatusReference.NOT_PAYED.getId());

        Status payed = new Status();
        payed.setId(PAYED.getId());

        budget.setStatus(notPayed);

        doReturn(budget).when(budgetService).findById(budget.getId());
        when(budgetService.getUtils()).thenReturn(utils);
        when(utils.getGenericsEntityReference(Status.class, PAYED.getId())).thenReturn(payed);
        when(budgetRepository.save(budget)).thenReturn(budget);

        BudgetProjection budgetProjection = budgetService.closeBudget(budget.getId());

        assertThat(budgetProjection.getStatus().getId()).isEqualTo(PAYED.getId());
        assertThat(budgetProjection.getClosedAt()).isNotNull();
    }

}
