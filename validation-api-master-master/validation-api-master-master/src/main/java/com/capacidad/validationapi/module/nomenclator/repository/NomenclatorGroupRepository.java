package com.capacidad.validationapi.module.nomenclator.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorGroup;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorGroupProjection;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@TenantFilter
public interface NomenclatorGroupRepository extends ExtendedJpaRepository<NomenclatorGroup, Long> {

    List<NomenclatorGroupProjection.Extended> findAllByNameContainingIgnoreCase(String name);

}
