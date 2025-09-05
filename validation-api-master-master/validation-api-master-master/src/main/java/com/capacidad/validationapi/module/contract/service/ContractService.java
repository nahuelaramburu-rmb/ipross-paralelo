package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.contract.dto.ContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.projection.ContractProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ContractService extends BaseContractService<Contract, ContractDTO> {

    Page<ContractProjection> findAllContracts(Pageable pageable, String search);

    void calculateAuthorizationItemPrice(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException;

    long getContractItemParentId(long contractItemId) throws ObjectNotFoundException;

    long getAdjustmentContractId(long adjustmentId) throws ObjectNotFoundException;

    Set<IdAndNameOnlyProjection> getPractitionerContracts(long practitionerId);

    Set<ContractProjection> findContractsContaining(String nameOrCode);

}
