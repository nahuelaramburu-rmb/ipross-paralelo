package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.budget.model.BeneficiaryBudget;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.repository.BeneficiaryBudgetRepository;
import com.capacidad.validationapi.module.budget.service.BeneficiaryBudgetService;
import com.capacidad.validationapi.module.budget.service.BudgetItemService;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.render.service.RenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.PAYCHECK;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;

@Service
@Transactional
public class BeneficiaryBudgetServiceImpl extends BaseServiceImpl<BeneficiaryBudget, IdDTO<Long>, Long> implements BeneficiaryBudgetService {

    private final BeneficiaryBudgetRepository beneficiaryBudgetRepository;
    private final BudgetItemService budgetItemService;
    private final RenderService renderService;

    @Autowired
    public BeneficiaryBudgetServiceImpl(BeneficiaryBudgetRepository beneficiaryBudgetRepository,
                                        BudgetItemService budgetItemService,
                                        RenderService renderService) {
        super(beneficiaryBudgetRepository);
        this.beneficiaryBudgetRepository = beneficiaryBudgetRepository;
        this.budgetItemService = budgetItemService;
        this.renderService = renderService;
    }

    @Override
    public Optional<BeneficiaryBudget> calculateBudget(MedicalAuthorizationItem medicalAuthorizationItem) {
        Beneficiary beneficiary = getHolderBeneficiary(medicalAuthorizationItem.getMedicalAuthorization().getBeneficiary());
        if (shouldCalculateBudget(beneficiary, medicalAuthorizationItem.getChargeSubtotal()) &&
                medicalAuthorizationItem.getStatus().getId().equals(VALIDATION_APPROVED.getId())) {
            BeneficiaryBudget beneficiaryBudget = createOrRetrieveNotPayedBudget(beneficiary);
            budgetItemService.addBudgetItem(beneficiaryBudget, medicalAuthorizationItem);
            return Optional.of(beneficiaryBudgetRepository.save(beneficiaryBudget));
        }
        return Optional.empty();
    }

    private Beneficiary getHolderBeneficiary(Beneficiary beneficiary) {
        return beneficiary.getRelatedBeneficiary() != null ? beneficiary.getRelatedBeneficiary() : beneficiary;
    }

    private boolean shouldCalculateBudget(Beneficiary beneficiary, BigDecimal chargeValue) {
        return beneficiary.getPaymentMethod().getId().equals(PAYCHECK.getId()) && chargeValue.intValue() != 0;
    }

    @Override
    public Optional<BeneficiaryBudget> calculateBudget(MedicalAuthorization medicalAuthorization) {
        Beneficiary beneficiary = getHolderBeneficiary(medicalAuthorization.getBeneficiary());
        if (shouldCalculateBudget(beneficiary, medicalAuthorization.getChargeTotal())) {
            BeneficiaryBudget beneficiaryBudget = createOrRetrieveNotPayedBudget(beneficiary);
            List<BudgetItem> newBudgetItems = budgetItemService.calculateBudgetItems(beneficiaryBudget, medicalAuthorization);
            if (!newBudgetItems.isEmpty())
                return Optional.of(beneficiaryBudgetRepository.save(beneficiaryBudget));
        }
        return Optional.empty();
    }

    @Override
    public boolean belongsToBeneficiary(long budgetId) {
        UUID beneficiaryResourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return beneficiaryBudgetRepository.existsByIdAndBeneficiaryResourceId(budgetId, beneficiaryResourceId);
    }

    @Override
    public ByteArrayOutputStream generateReceipt(long budgetId) throws ObjectNotValidException, ObjectNotFoundException {
        BeneficiaryBudget beneficiaryBudget = this.findById(budgetId);
        if (!beneficiaryBudget.getStatus().getId().equals(PAYED.getId()))
            throw new ObjectNotValidException("budget.invalidReceipt");
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("generatedBy", SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("-"));
        templateValues.put("generatedAt", LocalDateTime.now());
        templateValues.put("data", beneficiaryBudget);
        return renderService.renderPDF("beneficiary_budget", templateValues);
    }

    @Override
    public void closeAll() {
        Set<BeneficiaryBudget> beneficiaryBudgets = beneficiaryBudgetRepository.findAllByStatusIdAndClosedAtIsNull(NOT_PAYED.getId());
        Status payed = this.getUtils().getGenericsEntityReference(Status.class, PAYED.getId());
        beneficiaryBudgets.forEach(b -> {
            b.setStatus(payed);
            b.setClosedAt(LocalDateTime.now());
        });
        beneficiaryBudgetRepository.saveAll(beneficiaryBudgets);
    }

    private BeneficiaryBudget createOrRetrieveNotPayedBudget(Beneficiary beneficiary) {
        Company company = beneficiary.getCompany();
        return beneficiaryBudgetRepository.findByBeneficiaryIdAndCompanyIdAndStatusId
                (beneficiary.getId(), company.getId(), StatusReference.NOT_PAYED.getId())
                .orElseGet(() -> {
                    BeneficiaryBudget newBudget = new BeneficiaryBudget();
                    newBudget.setBeneficiary(beneficiary);
                    newBudget.setCompany(company);
                    Status notPayed = new Status();
                    notPayed.setId(StatusReference.NOT_PAYED.getId());
                    newBudget.setStatus(notPayed);
                    newBudget.setTotal(new BigDecimal(0));
                    return beneficiaryBudgetRepository.save(newBudget);
                });
    }

}
