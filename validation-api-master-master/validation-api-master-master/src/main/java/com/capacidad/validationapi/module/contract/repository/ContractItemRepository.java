package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import org.springframework.stereotype.Repository;

@Repository
@TenantFilter
public interface ContractItemRepository extends BaseContractItemRepository<ContractItem, Long> {
}
