package com.capacidad.validationapi.module.nomenclator.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@TenantFilter
@Repository
public interface NomenclatorRepository extends ExtendedJpaRepository<Nomenclator, Long> {

    Page<NomenclatorProjection.Minor> findAllProjectedBy(Pageable pageable);

    List<NomenclatorProjection.Minor> findAllByNomenclatorCodeContainingIgnoreCaseOrMedicalPracticeNameContainingIgnoreCase
            (String param1, String param2);

    List<NomenclatorProjection.Minor> findAllByMedicalPracticeMedicalSpecialtiesId(long medicalSpecialtyId);

    Page<NomenclatorProjection.Minor> findAllProjectedAndPaginatedByAuditTraysId(long auditTrayId, Pageable pageable);

    Optional<Nomenclator> findByNomenclatorCode(String nomenclatorCode);

}
