package com.capacidad.validationapi.module.nomenclator.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@TenantFilter(active = false)
@Repository
public interface MedicalPracticeRepository extends ExtendedJpaRepository<MedicalPractice, Long> {

    List<IdAndNameOnlyProjection> findAllProjectedByNameContainingIgnoreCase(String name);

    Optional<MedicalPractice> findById(Long medicalPracticeId);

}
