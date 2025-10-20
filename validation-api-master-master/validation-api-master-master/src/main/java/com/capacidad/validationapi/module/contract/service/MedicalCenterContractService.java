package com.capacidad.validationapi.module.contract.service;

import com.capacidad.validationapi.module.contract.dto.MedicalCenterContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.MedicalCenterContract;
import com.capacidad.validationapi.module.contract.projection.MedicalCenterContractProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface MedicalCenterContractService extends BaseContractService<MedicalCenterContract, MedicalCenterContractDTO> {

    Page<MedicalCenterContractProjection> findAllAuthMedicalCenterContracts(String search, Pageable pageable);

    Set<Contract> findAllAuthMedicalCenterContract();

    boolean existByIdAndAuthMedicalCenter(long contractId);

}
