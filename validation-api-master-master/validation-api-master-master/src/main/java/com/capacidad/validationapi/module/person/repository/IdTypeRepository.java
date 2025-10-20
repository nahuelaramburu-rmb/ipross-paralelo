package com.capacidad.validationapi.module.person.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.person.model.IdType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@TenantFilter(active = false)
@Repository
public interface IdTypeRepository extends ExtendedJpaRepository<IdType, Long> {

    Optional<IdType> findByNameOrAliasIgnoreCase(String name, String alias);

}
