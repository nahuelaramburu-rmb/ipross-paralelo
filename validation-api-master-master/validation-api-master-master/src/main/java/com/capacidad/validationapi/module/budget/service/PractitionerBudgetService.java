package com.capacidad.validationapi.module.budget.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

public interface PractitionerBudgetService extends BaseService<PractitionerBudget, IdDTO<Long>, Long> {

    Optional<PractitionerBudget> calculateBudget(MedicalAuthorizationItem medicalAuthorizationItem);

    Optional<PractitionerBudget> calculateBudget(MedicalAuthorization medicalAuthorization);

    boolean belongsToPractitioner(long budgetId);

    boolean belongsToMedicalCenter(long budgetId);

    ByteArrayOutputStream generateReceipt(long budgetId) throws ObjectNotValidException, ObjectNotFoundException;

}
