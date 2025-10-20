package com.capacidad.validationapi.module.budget.config.security;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.budget.service.BeneficiaryBudgetService;
import com.capacidad.validationapi.module.budget.service.PractitionerBudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BudgetChecker {

    private final PractitionerBudgetService practitionerBudgetService;
    private final BeneficiaryBudgetService beneficiaryBudgetService;

    @Autowired
    public BudgetChecker(PractitionerBudgetService practitionerBudgetService,
                         BeneficiaryBudgetService beneficiaryBudgetService) {
        this.practitionerBudgetService = practitionerBudgetService;
        this.beneficiaryBudgetService = beneficiaryBudgetService;
    }

    public boolean hasAccessToBeneficiaryBudget(long budgetId) {
        if (SecurityUtils.isBeneficiary())
            return beneficiaryBudgetService.belongsToBeneficiary(budgetId);
        return SecurityUtils.isHighRankingAuthority();
    }

    public boolean hasAccessToPractitionerBudget(long budgetId) {
        if (SecurityUtils.isPractitioner())
            return practitionerBudgetService.belongsToPractitioner(budgetId);
        if (SecurityUtils.isMedicalCenter())
            return practitionerBudgetService.belongsToMedicalCenter(budgetId);
        return SecurityUtils.isHighRankingAuthority();
    }

}
