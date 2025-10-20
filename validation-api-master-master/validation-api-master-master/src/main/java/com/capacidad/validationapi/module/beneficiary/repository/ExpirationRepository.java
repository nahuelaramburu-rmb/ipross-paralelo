package com.capacidad.validationapi.module.beneficiary.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.beneficiary.model.Expiration;
import com.capacidad.validationapi.module.beneficiary.projection.ExpirationProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@TenantFilter
@Repository
public interface ExpirationRepository extends ExtendedJpaRepository<Expiration, Long> {

    boolean existsByExpirationDateGreaterThanEqual(LocalDateTime now);

    Page<ExpirationProjection> findAllProjectedByBeneficiaryId(Long beneficiaryId, Pageable pageable);

    Optional<ExpirationProjection.BeneficiaryId> findBeneficiaryIdProjectionById(long expirationId);

}
