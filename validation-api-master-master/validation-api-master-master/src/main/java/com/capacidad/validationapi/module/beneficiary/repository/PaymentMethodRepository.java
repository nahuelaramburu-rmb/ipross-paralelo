package com.capacidad.validationapi.module.beneficiary.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface PaymentMethodRepository extends ExtendedJpaRepository<PaymentMethod, Long> {
}
