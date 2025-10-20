package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.budget.repository.PractitionerBudgetRepository;
import com.capacidad.validationapi.module.budget.service.BudgetItemService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_ADMIN_INSTANCE;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_MEDICAL_CENTER_INSTANCE;
import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.PAYCHECK;
import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerBudgetServiceImplTest {

    @Mock
    private PractitionerBudgetRepository practitionerBudgetRepository;

    @Mock
    private BudgetItemService budgetItemService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @InjectMocks
    private PractitionerBudgetServiceImpl practitionerBudgetService;

    @Test
    public void testCalculateBudgetFromItemReturnsEmptyWhenBeneficiaryNotVoluntary() {
        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());
        beneficiary.setPaymentMethod(paycheck);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorization.setBeneficiary(beneficiary);

        Optional<PractitionerBudget> result = practitionerBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetReturnsEmptyWhenItemChargeSubtotalIsZero() {
        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(0));
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorization.setBeneficiary(beneficiary);

        Optional<PractitionerBudget> result = practitionerBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetReturnsEmptyWhenItemNotApproved() {
        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(123));
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorization.setBeneficiary(beneficiary);

        Status pending = new Status();
        pending.setId(StatusReference.VALIDATION_PENDING.getId());
        medicalAuthorizationItem.setStatus(pending);

        Optional<PractitionerBudget> result = practitionerBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testCalculateBudgetCreatesNewBudgetWhenLastPayedAndItemApproved() {
        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(123));
        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());
        medicalAuthorizationItem.setStatus(approved);

        PractitionerBudget budget = new PractitionerBudget();
        budget.setTotal(new BigDecimal(0));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setMedicalCenter(medicalCenter);
        medicalAuthorization.setBeneficiary(beneficiary);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        when(practitionerBudgetRepository.findByPractitionerIdAndMedicalCenterIdAndStatusId
                (practitioner.getId(), medicalCenter.getId(), StatusReference.NOT_PAYED.getId()))
                .thenReturn(Optional.empty());
        when(practitionerBudgetRepository.save(any(PractitionerBudget.class))).thenReturn(budget);

        Optional<PractitionerBudget> result = practitionerBudgetService.calculateBudget(medicalAuthorizationItem);

        assertThat(result).isPresent();
        verify(budgetItemService, times(1)).addBudgetItem(budget, medicalAuthorizationItem);
        verify(practitionerBudgetRepository, times(1)).save(budget);
    }

    @Test
    public void testCalculateBudgetReturnsEmptyWhenAuthorizationDoesNotContainApprovedItems() {
        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        PractitionerBudget budget = new PractitionerBudget();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setMedicalCenter(medicalCenter);
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setChargeTotal(new BigDecimal(123));

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Status pending = new Status();
        pending.setId(StatusReference.VALIDATION_PENDING.getId());
        medicalAuthorizationItem.setStatus(pending);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        when(practitionerBudgetRepository.findByPractitionerIdAndMedicalCenterIdAndStatusId
                (practitioner.getId(), medicalCenter.getId(), StatusReference.NOT_PAYED.getId()))
                .thenReturn(Optional.of(budget));
        when(budgetItemService.calculateBudgetItems(budget, medicalAuthorization)).thenReturn(Collections.emptyList());

        Optional<PractitionerBudget> result = practitionerBudgetService.calculateBudget(medicalAuthorization);

        assertThat(result).isNotPresent();
        verify(practitionerBudgetRepository, never()).save(budget);
    }

    @Test
    public void testCalculateBudgetReturnsValidBudgetWhenNotEmptyBudgetItemList() {
        PractitionerBudget budget = new PractitionerBudget();
        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(VOLUNTARY.getId());
        beneficiary.setPaymentMethod(voluntary);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setMedicalCenter(medicalCenter);
        medicalAuthorization.setChargeTotal(new BigDecimal(123));

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Status pending = new Status();
        pending.setId(StatusReference.VALIDATION_PENDING.getId());
        medicalAuthorizationItem.setStatus(pending);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        BudgetItem budgetItem = new BudgetItem();
        List<BudgetItem> budgetItems = new ArrayList<>();
        budgetItems.add(budgetItem);

        when(practitionerBudgetRepository.findByPractitionerIdAndMedicalCenterIdAndStatusId
                (practitioner.getId(), medicalCenter.getId(), StatusReference.NOT_PAYED.getId()))
                .thenReturn(Optional.of(budget));
        when(budgetItemService.calculateBudgetItems(budget, medicalAuthorization)).thenReturn(budgetItems);
        when(practitionerBudgetRepository.save(budget)).thenReturn(budget);

        Optional<PractitionerBudget> result = practitionerBudgetService.calculateBudget(medicalAuthorization);

        assertThat(result).isPresent();
    }

    @Test
    public void testCalculateBudgetReturnsEmptyWhenBeneficiaryNotVoluntary() {
        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod paycheck = new PaymentMethod();
        paycheck.setId(PAYCHECK.getId());
        beneficiary.setPaymentMethod(paycheck);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);

        Optional<PractitionerBudget> result = practitionerBudgetService.calculateBudget(medicalAuthorization);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testAppendCustomSpecificationReturnsSpecWhenRoleIsMedicalCenter() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        Optional<Specification<PractitionerBudget>> spec = practitionerBudgetService.appendCustomSpecification();

        assertThat(spec).isPresent();
    }

    @Test
    public void testAppendCustomSpecificationReturnsEmptyWhenRoleIsNotMedicalCenter() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(ROLE_ADMIN_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        Optional<Specification<PractitionerBudget>> spec = practitionerBudgetService.appendCustomSpecification();

        assertThat(spec).isNotPresent();
    }

}
