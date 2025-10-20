package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.repository.BudgetItemRepository;
import com.capacidad.validationapi.module.budget.service.BudgetItemService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.*;

@Service
public class BudgetItemServiceImpl extends BaseServiceImpl<BudgetItem, IdDTO<Long>, Long> implements BudgetItemService {

    private final BudgetItemRepository budgetItemRepository;

    @Autowired
    public BudgetItemServiceImpl(BudgetItemRepository repository) {
        super(repository);
        this.budgetItemRepository = repository;
    }

    @Override
    public List<BudgetItem> calculateBudgetItems(Budget budget, MedicalAuthorization medicalAuthorization) {
        Set<BudgetItem> newBudgetItems = new HashSet<>();
        for (MedicalAuthorizationItem medicalAuthorizationItem : medicalAuthorization.getMedicalAuthorizationItems()) {
            Optional<BudgetItem> optionalBudgetItem = buildAndSumItem(budget, medicalAuthorizationItem);
            if (optionalBudgetItem.isPresent()) {
                BudgetItem budgetItem = optionalBudgetItem.get();
                budgetItem.setBudget(budget);
                newBudgetItems.add(budgetItem);
            }
        }
        if (newBudgetItems.isEmpty())
            return Collections.emptyList();
        return budgetItemRepository.saveAll(newBudgetItems);
    }

    private BudgetItem buildBudgetItemFromAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        BudgetItem budgetItem = new BudgetItem();
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        budgetItem.setMedicalAuthorization(medicalAuthorization);
        budgetItem.setChargeUnitPrice(medicalAuthorizationItem.getChargeUnitPrice());
        budgetItem.setQuantity(medicalAuthorizationItem.getQuantity());
        budgetItem.setChargeSubtotal(medicalAuthorizationItem.getChargeSubtotal());
        budgetItem.setNomenclator(medicalAuthorizationItem.getNomenclator());
        return budgetItem;
    }

    @Override
    public void addBudgetItem(Budget budget, MedicalAuthorizationItem medicalAuthorizationItem) {
        Optional<BudgetItem> optionalBudgetItem = buildAndSumItem(budget, medicalAuthorizationItem);
        optionalBudgetItem.ifPresent(bi -> {
            budget.getBudgetItems().add(bi);
            budget.associateChildObjects();
        });
    }

    private Optional<BudgetItem> buildAndSumItem(Budget budget, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (!medicalAuthorizationItem.isApproved() ||
                medicalAuthorizationItem.getChargeSubtotal().intValue() == 0)
            return Optional.empty();
        BudgetItem budgetItem = buildBudgetItemFromAuthorizationItem(medicalAuthorizationItem);
        budget.setTotal(budget.getTotal().add(budgetItem.getChargeSubtotal()).setScale(2, RoundingMode.HALF_UP));
        return Optional.of(budgetItem);
    }

}
