package com.capacidad.validationapi.module.contract.service;

import com.capacidad.validationapi.module.contract.dto.PractitionerContractDTO;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import com.capacidad.validationapi.module.contract.projection.PractitionerContractProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PractitionerContractService extends BaseContractService<PractitionerContract, PractitionerContractDTO> {

    Page<PractitionerContractProjection> findAllAuthPractitionerContracts(String search, Pageable pageable);

    boolean existByIdAndAuthPractitioner(long contractId);

}
