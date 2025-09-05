package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.projection.ContractProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@TenantFilter
@Repository
public interface ContractRepository extends BaseContractRepository<Contract, Long> {

    Optional<BaseProjection<Long>> findByContractItemsId(Long contractItemId);

    Optional<BaseProjection<Long>> findByContractAdjustmentsId(Long adjustmentId);

    Set<IdAndNameOnlyProjection> findAllByPractitionersId(Long practitionerId);

    Set<ContractProjection> findAllByNameContainingIgnoreCaseOrContractCodeContainingIgnoreCase(String name, String code);

}
