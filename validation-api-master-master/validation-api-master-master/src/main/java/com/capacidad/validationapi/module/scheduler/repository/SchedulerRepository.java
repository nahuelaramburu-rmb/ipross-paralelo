package com.capacidad.validationapi.module.scheduler.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.scheduler.model.SchedulerJobInfo;
import org.springframework.stereotype.Repository;

@TenantFilter(active = false)
@Repository
public interface SchedulerRepository extends ExtendedJpaRepository<SchedulerJobInfo, Long> {
}
