package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.projection.MedicalCenterContractProjection;
import com.capacidad.validationapi.module.contract.projection.OrganizationContractProjection;
import com.capacidad.validationapi.module.contract.projection.PractitionerContractProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ContractMediator {

    Page<OrganizationContractProjection> findAllAuthOrganizationContracts(String search, Pageable pageable);

    Page<MedicalCenterContractProjection> findAllAuthMedicalCenterContracts(String search, Pageable pageable);

    Set<Contract> findAllAuthMedicalCenterContracts();

    Set<Contract> findAllAuthOrganizationAndRelatedContracts() throws ObjectNotFoundException;

    Set<Contract> findAllAuthContracts();

    Page<PractitionerContractProjection> findAllAuthPractitionerContracts(String search, Pageable pageable);

}
