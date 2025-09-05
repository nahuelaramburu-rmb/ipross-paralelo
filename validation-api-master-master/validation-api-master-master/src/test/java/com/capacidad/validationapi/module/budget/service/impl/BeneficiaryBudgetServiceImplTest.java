package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import com.capacidad.validationapi.module.budget.model.BeneficiaryBudget;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.repository.BeneficiaryBudgetRepository;
import com.capacidad.validationapi.module.budget.service.BudgetItemService;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.PAYCHECK;
import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryBudgetServiceImplTest {

    @Mock
    private BeneficiaryBudgetRepository beneficiaryBudgetRepository;

    @Mock
    private BudgetItemService budgetItemService;

    @InjectMocks
    private BeneficiaryBudgetServiceImpl beneficiaryBudgetService;

    @Test
    public void testCalculateBudgetFromItemReturnsEmptyWhenBeneficiaryPaymentMethodNotPaycheck() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());
        medicalAuthorizationItem.setStatus(approved);

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetFromItemReturnsEmptyWhenItemChargeSubtotalIsZero() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(0));

        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());
        medicalAuthorizationItem.setStatus(approved);

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetFromItemReturnsEmptyWhenBeneficiaryPaymentMethodNotPaycheckButNotApprovedItem() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());
        beneficiary.setPaymentMethod(paycheck);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(123));

        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_PENDING.getId());
        medicalAuthorizationItem.setStatus(approved);

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetFromItemCreatesNewBudgetWhenLastPayedAndItemApprovedWithHolder() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());
        medicalAuthorizationItem.setStatus(approved);

        BeneficiaryBudget budget = new BeneficiaryBudget();
        budget.setTotal(new BigDecimal(0));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());
        beneficiary.setPaymentMethod(paycheck);

        Company company = new Company();
        company.setId(1L);

        beneficiary.setCompany(company);

        medicalAuthorization.setBeneficiary(beneficiary);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(123));

        when(beneficiaryBudgetRepository.findByBeneficiaryIdAndCompanyIdAndStatusId
                (beneficiary.getId(), company.getId(), StatusReference.NOT_PAYED.getId()))
                .thenReturn(Optional.empty());
        when(beneficiaryBudgetRepository.save(any(BeneficiaryBudget.class))).thenReturn(budget);

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isPresent();
        verify(budgetItemService, times(1)).addBudgetItem(budget, medicalAuthorizationItem);
        verify(beneficiaryBudgetRepository, times(1)).save(budget);
    }

    @Test
    public void testCalculateBudgetFromItemCreatesNewBudgetWhenLastPayedAndItemApprovedWithRelative() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());
        medicalAuthorizationItem.setStatus(approved);

        BeneficiaryBudget budget = new BeneficiaryBudget();
        budget.setTotal(new BigDecimal(0));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Beneficiary relative = new Beneficiary();
        relative.setId(2L);

        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());

        relative.setPaymentMethod(paycheck);
        beneficiary.setRelatedBeneficiary(relative);

        Company company = new Company();
        company.setId(1L);

        relative.setCompany(company);

        medicalAuthorization.setBeneficiary(beneficiary);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(123));

        when(beneficiaryBudgetRepository.findByBeneficiaryIdAndCompanyIdAndStatusId
                (relative.getId(), company.getId(), StatusReference.NOT_PAYED.getId()))
                .thenReturn(Optional.empty());
        when(beneficiaryBudgetRepository.save(any(BeneficiaryBudget.class))).thenReturn(budget);

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isPresent();
        verify(budgetItemService, times(1)).addBudgetItem(budget, medicalAuthorizationItem);
        verify(beneficiaryBudgetRepository, times(1)).save(budget);
    }

    @Test
    public void testCalculateBudgetReturnsEmptyWhenBeneficiaryPaymentMethodNotPaycheck() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        medicalAuthorization.setBeneficiary(beneficiary);

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorization);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetReturnsEmptyWhenEmptyBudgetItemList() {
        BeneficiaryBudget budget = new BeneficiaryBudget();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(PAYCHECK.getId());
        beneficiary.setPaymentMethod(voluntary);

        Company company = new Company();
        company.setId(1L);

        beneficiary.setCompany(company);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setChargeTotal(new BigDecimal(123));

        when(beneficiaryBudgetRepository.findByBeneficiaryIdAndCompanyIdAndStatusId
                (beneficiary.getId(), company.getId(), StatusReference.NOT_PAYED.getId()))
                .thenReturn(Optional.of(budget));
        when(budgetItemService.calculateBudgetItems(budget, medicalAuthorization)).thenReturn(Collections.emptyList());

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorization);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetReturnsValidBudgetWhenNotEmptyBudgetItemList() {
        BeneficiaryBudget budget = new BeneficiaryBudget();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(PAYCHECK.getId());
        beneficiary.setPaymentMethod(voluntary);

        Company company = new Company();
        company.setId(1L);

        beneficiary.setCompany(company);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setChargeTotal(new BigDecimal(123));

        BudgetItem budgetItem = new BudgetItem();
        List<BudgetItem> budgetItems = new ArrayList<>();
        budgetItems.add(budgetItem);

        when(beneficiaryBudgetRepository.findByBeneficiaryIdAndCompanyIdAndStatusId
                (beneficiary.getId(), company.getId(), StatusReference.NOT_PAYED.getId()))
                .thenReturn(Optional.of(budget));
        when(budgetItemService.calculateBudgetItems(budget, medicalAuthorization)).thenReturn(budgetItems);
        when(beneficiaryBudgetRepository.save(budget)).thenReturn(budget);

        Optional<BeneficiaryBudget> result = beneficiaryBudgetService.calculateBudget(medicalAuthorization);

        assertThat(result).isPresent();
    }

}
