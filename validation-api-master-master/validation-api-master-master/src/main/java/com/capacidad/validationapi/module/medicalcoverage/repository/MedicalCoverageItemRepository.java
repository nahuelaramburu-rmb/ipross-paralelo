package com.capacidad.validationapi.module.medicalcoverage.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@TenantFilter
public interface MedicalCoverageItemRepository extends ExtendedJpaRepository<MedicalCoverageItem, Long> {

    Optional<MedicalCoverageItem> findByMedicalCoverageIdAndNomenclatorId(Long medicalCoverageId, Long nomenclatorId);

    Optional<MedicalCoverageItemProjection> findProjectedByMedicalCoverageIdAndNomenclatorId(Long medicalCoverageId, Long nomenclatorId);

    Page<MedicalCoverageItemProjection> findAllProjectedByMedicalCoverageId(Long medicalCoverageId, Pageable pageable);

    Set<MedicalCoverageItemProjection> findAllByMedicalCoverageIdAndNomenclatorIdIn(Long medicalCoverageId, Set<Long> nomenclatorIds);

}
