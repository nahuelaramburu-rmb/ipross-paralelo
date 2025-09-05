package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.contract.dto.OrganizationContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.contract.projection.OrganizationContractProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface OrganizationContractService extends BaseContractService<OrganizationContract, OrganizationContractDTO> {

    Page<OrganizationContractProjection> findAllAuthOrganizationContracts(String search, Pageable pageable);

    boolean existByIdAndAuthOrganization(long contractId);

    Set<Contract> findAllAuthOrganizationAndRelatedContracts() throws ObjectNotFoundException;

}
