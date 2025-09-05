package com.capacidad.validationapi.module.contract.service;

import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

public interface ContractCalculationStrategy {

    void calculateAuthorizationItemPrice(ContractItem contractItem, MedicalAuthorizationItem medicalAuthorizationItem);

}
