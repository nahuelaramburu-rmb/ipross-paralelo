package com.capacidad.validationapi.module.budget.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.projection.BudgetProjection;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

public interface BudgetService extends BaseService<Budget, IdDTO<Long>, Long> {

    void calculateBudget(MedicalAuthorizationItem medicalAuthorizationItem);

    void calculateBudget(MedicalAuthorization medicalAuthorization);

    void calculateBeneficiaryBudget(MedicalAuthorization medicalAuthorization);

    void removeFromBudget(MedicalAuthorization medicalAuthorization);

    BudgetProjection closeBudget(long budgetId) throws ObjectNotFoundException, ObjectNotValidException;

}
