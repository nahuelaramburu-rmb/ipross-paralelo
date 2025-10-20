package com.capacidad.validationapi.module.location.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.model.Province;
import org.springframework.stereotype.Repository;

import java.util.List;

@TenantFilter(active = false)
@Repository
public interface ProvinceRepository extends ExtendedJpaRepository<Province, Long> {

    List<IdAndNameOnlyProjection> findProjectedByCountryId(Long countryId);

}
