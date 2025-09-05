package com.capacidad.validationapi.module.medicalauthorization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@TenantFilter
@Repository
public interface MedicalAuthorizationRepository extends BaseMedicalAuthorizationRepository<MedicalAuthorization, Long> {

    boolean existsByIdAndMedicalCenterResourceId(Long id, UUID resourceId);

    boolean existsByIdAndPractitionerResourceId(Long id, UUID resourceId);

    boolean existsByIdAndBeneficiaryResourceId(Long medicalAuthorizationId, UUID resourceId);

    boolean existsByIdAndBeneficiaryFamilyId(Long medicalAuthorizationId, UUID familyId);

    Optional<BaseProjection<Long>> findByMedicalAuthorizationItemsId(Long medicalAuthorizationItemId);

    boolean existsByIdAndContractIn(Long medicalAuthorizationId, Collection<Contract> contracts);

    List<MedicalAuthorization> findAllByMedicalCenterAndAuthorizationTypeIdAndCreatedAtBetweenAndStatusIdNotInAndIdIsNot
            (MedicalCenter medicalCenter, Long validationTypeId, LocalDateTime from, LocalDateTime to, Collection<Long> statusIds, long id);

}
