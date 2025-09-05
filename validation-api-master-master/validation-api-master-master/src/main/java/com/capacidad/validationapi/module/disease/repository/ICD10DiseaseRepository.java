package com.capacidad.validationapi.module.disease.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@TenantFilter(active = false)
public interface ICD10DiseaseRepository extends ExtendedJpaRepository<ICD10Disease, Long> {

    Set<ICD10DiseaseProjection> findTop50ByNameContainingIgnoreCaseOrCodeStartingWithIgnoreCase
            (String name, String code);

}
