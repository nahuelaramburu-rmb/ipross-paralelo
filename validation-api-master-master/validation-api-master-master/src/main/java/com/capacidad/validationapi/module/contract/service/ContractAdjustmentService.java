package com.capacidad.validationapi.module.contract.service;

import com.capacidad.validationapi.module.contract.dto.ContractAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.projection.ContractAdjustmentProjection;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractAdjustmentService extends BaseContractAdjustmentService<ContractAdjustment, ContractAdjustmentDTO> {

    void applyContractAdjustments(MedicalAuthorizationItem medicalAuthorizationItem);

    Page<ContractAdjustmentProjection> findAllContractAdjustments(Pageable pageable, String search);

}
