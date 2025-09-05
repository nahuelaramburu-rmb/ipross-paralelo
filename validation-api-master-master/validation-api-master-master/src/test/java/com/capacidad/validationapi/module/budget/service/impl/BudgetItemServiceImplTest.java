package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.repository.BudgetItemRepository;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_PENDING;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BudgetItemServiceImplTest {

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @InjectMocks
    private BudgetItemServiceImpl budgetItemService;


    @Test
    public void testCalculateBudgetItemsReturnsEmptyBudgetItemListWhenNotApprovedItems() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        medicalAuthorizationItem.setStatus(pending);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        List<BudgetItem> result = budgetItemService.calculateBudgetItems(new Budget(), medicalAuthorization);

        assertThat(result).isEmpty();
    }

    @Test
    public void testCalculateBudgetItemsReturnsValidBudgetListWhenValidItems() {
        Budget budget = new Budget();
        budget.setTotal(new BigDecimal(0));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorizationItem medicalAuthorizationItem1 = new MedicalAuthorizationItem();
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        medicalAuthorizationItem.setStatus(pending);
        medicalAuthorizationItem1.setStatus(approved);
        medicalAuthorizationItem2.setStatus(approved);

        medicalAuthorizationItem1.setNomenclator(new Nomenclator());
        medicalAuthorizationItem1.setQuantity(2);
        medicalAuthorizationItem1.setChargeUnitPrice(new BigDecimal("160.725"));
        medicalAuthorizationItem1.setChargeSubtotal(new BigDecimal("321.45"));

        medicalAuthorizationItem2.setNomenclator(new Nomenclator());
        medicalAuthorizationItem2.setQuantity(2);
        medicalAuthorizationItem2.setChargeUnitPrice(new BigDecimal("0.0"));
        medicalAuthorizationItem2.setChargeSubtotal(new BigDecimal("0.0"));

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem1);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);
        medicalAuthorization.associateChildObjects();

        List<BudgetItem> budgetItems = new ArrayList<>();
        budgetItems.add(new BudgetItem());

        when(budgetItemRepository.saveAll(any())).thenReturn(budgetItems);

        List<BudgetItem> result = budgetItemService.calculateBudgetItems(budget, medicalAuthorization);

        assertThat(result.size()).isEqualTo(1);
        assertThat(budget.getTotal()).isEqualTo(medicalAuthorizationItem1.getChargeSubtotal().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void testAddBudgetItemCalculateSuccessfullyNewResult() {
        Budget budget = new Budget();
        BigDecimal currentTotal = new BigDecimal("3831.77");
        budget.setTotal(currentTotal);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem1 = new MedicalAuthorizationItem();
        medicalAuthorizationItem1.setNomenclator(new Nomenclator());
        medicalAuthorizationItem1.setQuantity(2);
        medicalAuthorizationItem1.setChargeUnitPrice(new BigDecimal("160.725"));
        medicalAuthorizationItem1.setChargeSubtotal(new BigDecimal("321.45"));
        medicalAuthorizationItem1.setStatus(approved);

        budgetItemService.addBudgetItem(budget, medicalAuthorizationItem1);

        assertThat(budget.getTotal()).isEqualTo(currentTotal.add(medicalAuthorizationItem1.getChargeSubtotal())
                .setScale(2, RoundingMode.HALF_UP));
        assertThat(budget.getBudgetItems().size()).isEqualTo(1);

        BudgetItem budgetItem = budget.getBudgetItems().iterator().next();

        assertThat(budgetItem.getQuantity()).isEqualTo(medicalAuthorizationItem1.getQuantity());
        assertThat(budgetItem.getChargeUnitPrice()).isEqualTo(medicalAuthorizationItem1.getChargeUnitPrice());
        assertThat(budgetItem.getChargeSubtotal()).isEqualTo(medicalAuthorizationItem1.getChargeSubtotal());
        assertThat(budgetItem.getMedicalAuthorization()).isEqualTo(medicalAuthorizationItem1.getMedicalAuthorization());
        assertThat(budgetItem.getNomenclator()).isEqualTo(medicalAuthorizationItem1.getNomenclator());
    }

}
