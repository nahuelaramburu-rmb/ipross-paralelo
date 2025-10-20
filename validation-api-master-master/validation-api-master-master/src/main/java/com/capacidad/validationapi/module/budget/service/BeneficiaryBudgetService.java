package com.capacidad.validationapi.module.budget.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.budget.model.BeneficiaryBudget;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

public interface BeneficiaryBudgetService extends BaseService<BeneficiaryBudget, IdDTO<Long>, Long> {

    Optional<BeneficiaryBudget> calculateBudget(MedicalAuthorizationItem medicalAuthorizationItem);

    Optional<BeneficiaryBudget> calculateBudget(MedicalAuthorization medicalAuthorization);

    boolean belongsToBeneficiary(long budgetId);

    ByteArrayOutputStream generateReceipt(long budgetId) throws ObjectNotValidException, ObjectNotFoundException;

    void closeAll();

}
