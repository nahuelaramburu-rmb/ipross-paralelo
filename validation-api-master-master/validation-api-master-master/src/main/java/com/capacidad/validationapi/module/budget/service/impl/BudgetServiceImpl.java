package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.projection.BudgetProjection;
import com.capacidad.validationapi.module.budget.repository.BudgetItemRepository;
import com.capacidad.validationapi.module.budget.repository.BudgetRepository;
import com.capacidad.validationapi.module.budget.service.BeneficiaryBudgetService;
import com.capacidad.validationapi.module.budget.service.BudgetService;
import com.capacidad.validationapi.module.budget.service.PractitionerBudgetService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.NOT_PAYED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PAYED;

@Service
public class BudgetServiceImpl extends BaseServiceImpl<Budget, IdDTO<Long>, Long> implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final BeneficiaryBudgetService beneficiaryBudgetService;
    private final PractitionerBudgetService practitionerBudgetService;

    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             BudgetItemRepository budgetItemRepository,
                             BeneficiaryBudgetService beneficiaryBudgetService,
                             PractitionerBudgetService practitionerBudgetService) {
        super(budgetRepository);
        this.budgetRepository = budgetRepository;
        this.budgetItemRepository = budgetItemRepository;
        this.beneficiaryBudgetService = beneficiaryBudgetService;
        this.practitionerBudgetService = practitionerBudgetService;
    }

    @Override
    public void calculateBudget(MedicalAuthorizationItem medicalAuthorizationItem) {
        practitionerBudgetService.calculateBudget(medicalAuthorizationItem);
        beneficiaryBudgetService.calculateBudget(medicalAuthorizationItem);
    }

    @Override
    public void calculateBudget(MedicalAuthorization medicalAuthorization) {
        practitionerBudgetService.calculateBudget(medicalAuthorization);
        beneficiaryBudgetService.calculateBudget(medicalAuthorization);
    }

    @Override
    public void calculateBeneficiaryBudget(MedicalAuthorization medicalAuthorization) {
        beneficiaryBudgetService.calculateBudget(medicalAuthorization);
    }

    @Override
    public void removeFromBudget(MedicalAuthorization medicalAuthorization) {
        Set<BudgetItem> budgetItems = budgetItemRepository.findAllByMedicalAuthorizationId(medicalAuthorization.getId());
        if (!budgetItems.isEmpty()) {
            budgetItems.forEach(throwingConsumer(bi -> {
                Budget budget = bi.getBudget();
                if (budget.getStatus().getId().equals(StatusReference.PAYED.getId()))
                    throw new ObjectNotValidException("budget.alreadyPayed");
            }));
            Set<Budget> relatedBudgets = budgetItems.stream()
                    .map(b -> {
                        Budget budget = b.getBudget();
                        budget.setTotal(budget.getTotal().subtract(b.getChargeSubtotal()).setScale(2, RoundingMode.HALF_UP));
                        return budget;
                    })
                    .collect(Collectors.toUnmodifiableSet());
            budgetItemRepository.deleteAll(budgetItems);
            budgetRepository.saveAll(relatedBudgets);
        }
    }

    @Override
    public BudgetProjection closeBudget(long budgetId) throws ObjectNotFoundException, ObjectNotValidException {
        Budget budget = this.findById(budgetId);
        if (!budget.getStatus().getId().equals(NOT_PAYED.getId()))
            throw new ObjectNotValidException("budget.invalidStatus", String.valueOf(budgetId));
        Status payed = this.getUtils().getGenericsEntityReference(Status.class, PAYED.getId());
        budget.setStatus(payed);
        budget.setClosedAt(LocalDateTime.now());
        Budget updatedBudget = budgetRepository.save(budget);
        return this.getProjectionFactory().createProjection(BudgetProjection.class, updatedBudget);
    }

}
