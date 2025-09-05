package com.capacidad.validationapi.module.budget.service;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

import java.util.List;

public interface BudgetItemService extends BaseService<BudgetItem, IdDTO<Long>, Long> {

    List<BudgetItem> calculateBudgetItems(Budget budget, MedicalAuthorization medicalAuthorization);

    void addBudgetItem(Budget budget, MedicalAuthorizationItem medicalAuthorizationItem);

}
