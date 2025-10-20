package com.capacidad.validationapi.module.location.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.projection.RegionProjection;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@TenantFilter
public interface RegionRepository extends ExtendedJpaRepository<Region, Long> {

    Set<RegionProjection> findAllProjectedByNameContainingIgnoreCase(String name);

    boolean existsByIdAndCities(Long regionId, City city);

}
