package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.MonetaryAdjustment;
import org.springframework.stereotype.Repository;

@Repository
@TenantFilter
public interface MonetaryAdjustmentRepository extends BaseContractAdjustmentRepository<MonetaryAdjustment, Long> {
}
