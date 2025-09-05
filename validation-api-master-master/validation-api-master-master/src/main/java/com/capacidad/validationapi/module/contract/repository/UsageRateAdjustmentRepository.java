package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.UsageRateAdjustment;
import org.springframework.stereotype.Repository;

@Repository
@TenantFilter
public interface UsageRateAdjustmentRepository extends BaseContractAdjustmentRepository<UsageRateAdjustment, Long> {
}
