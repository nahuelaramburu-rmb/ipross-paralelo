package com.capacidad.validationapi.module.medicalauthorization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@TenantFilter
public interface MedicalAuthorizationItemRepository extends ExtendedJpaRepository<MedicalAuthorizationItem, Long> {

    Set<MedicalAuthorizationItem> findAllByIdInAndSettledIsFalseAndMedicalAuthorizationPractitionerIdAndMedicalAuthorizationContractId(Collection<Long> medicalAuthorizationIds, Long practitionerId, Long contractId);

    @Query(value = "select it from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization as medAuth " +
            "where it.nomenclator.id = ?1 " +
            "and medAuth.beneficiary.id = ?2 " +
            "and it.status.id = ?3 " +
            "and (it.createdAt between ?4 and ?5)")
    List<MedicalAuthorizationItem> findAllByBatchItemAndStatusInPeriod(long nomenclatorId,
                                                                       long beneficiaryId,
                                                                       long statusId,
                                                                       LocalDateTime from,
                                                                       LocalDateTime to);

    @Query(value = "select it from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization as medAuth " +
            "where medAuth.contract.id=?1 " +
            "and it.nomenclator.id=?2 " +
            "and (medAuth.authorizationCondition.id<>?3 or medAuth.authorizationCondition.id is null) " +
            "and medAuth.city.id=?4 " +
            "and (it.createdAt between ?5 and ?6) " +
            "and it.status.id = ?7 " +
            "and it.id <> ?8")
    List<MedicalAuthorizationItem> findAllByContractAndNomenclatorAndCityIgnoringAuthorizationConditionInPeriod
            (long contractId,
             long nomenclatorId,
             long authorizationConditionId,
             long cityId,
             LocalDateTime from,
             LocalDateTime to,
             long statusId,
             long id);

    @Query(value = "select it from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization as medAuth " +
            "where medAuth.contract.id=?1 " +
            "and it.nomenclator.id=?2 " +
            "and (medAuth.authorizationCondition.id<>?3 or medAuth.authorizationCondition.id is null) " +
            "and medAuth.city.id=?4 " +
            "and (it.createdAt between ?5 and ?6) " +
            "and it.status.id = ?7 " +
            "and medAuth.practitioner.id=?8 " +
            "and it.id <> ?9")
    List<MedicalAuthorizationItem> findAllByContractAndPractitionerAndNomenclatorAndCityIgnoringAuthorizationConditionInPeriod
            (long contractId,
             long nomenclatorId,
             long authorizationConditionId,
             long cityId,
             LocalDateTime from,
             LocalDateTime to,
             long statusId,
             long practitionerId,
             long id);

    @Query(value = "select it from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization as medAuth " +
            "where medAuth.contract.id=?1 " +
            "and it.nomenclator.id=?2 " +
            "and (medAuth.authorizationCondition.id<>?3 or medAuth.authorizationCondition.id is null) " +
            "and medAuth.city.id in ?4 " +
            "and (it.createdAt between ?5 and ?6) " +
            "and it.status.id = ?7 " +
            "and it.id <> ?8")
    List<MedicalAuthorizationItem> findAllByContractAndNomenclatorAndCitiesIgnoringAuthorizationConditionInPeriod
            (long contractId,
             long nomenclatorId,
             long authorizationConditionId,
             Collection<Long> cityIds,
             LocalDateTime from,
             LocalDateTime to,
             long statusId,
             long id);

    @Query(value = "select it from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization as medAuth " +
            "where medAuth.contract.id=?1 " +
            "and it.nomenclator.id=?2 " +
            "and (medAuth.authorizationCondition.id<>?3 or medAuth.authorizationCondition.id is null) " +
            "and medAuth.city.id in ?4 " +
            "and (it.createdAt between ?5 and ?6) " +
            "and it.status.id = ?7 " +
            "and medAuth.practitioner.id=?8 " +
            "and it.id <> ?9")
    List<MedicalAuthorizationItem> findAllByContractAndPractitionerAndNomenclatorAndCitiesIgnoringAuthorizationConditionInPeriod
            (long contractId,
             long nomenclatorId,
             long authorizationConditionId,
             Collection<Long> cityIds,
             LocalDateTime from,
             LocalDateTime to,
             long statusId,
             long practitionerId,
             long id);

    default List<MedicalAuthorizationItem> findAllByNomenclatorBeneficiaryPractitionerStatusIdAndPeriod(Nomenclator nomenclator, Beneficiary beneficiary, Practitioner practitioner, LocalDateTime from, long statusId, long id) {
        return findAllByNomenclatorAndMedicalAuthorizationBeneficiaryAndMedicalAuthorizationPractitionerAndCreatedAtBetweenAndStatusIdAndIdIsNot
                (nomenclator,
                        beneficiary,
                        practitioner,
                        from,
                        LocalDateTime.now(),
                        statusId,
                        id);
    }

    List<MedicalAuthorizationItem> findAllByNomenclatorAndMedicalAuthorizationBeneficiaryAndMedicalAuthorizationPractitionerAndCreatedAtBetweenAndStatusIdAndIdIsNot
            (Nomenclator nomenclator,
             Beneficiary beneficiary,
             Practitioner practitioner,
             LocalDateTime dateFrom,
             LocalDateTime dateTo,
             long statusId,
             long id);

    List<MedicalAuthorizationItem> findAllByNomenclatorIdAndMedicalAuthorizationBeneficiaryIdAndCreatedAtBetweenAndStatusIdAndIdIsNot
            (long nomenclatorId,
             long beneficiaryId,
             LocalDateTime dateFrom,
             LocalDateTime dateTo,
             long statusId,
             long id);

    List<MedicalAuthorizationItem> findAllByMedicalAuthorizationBeneficiaryIdAndNomenclatorIdAndStatusIdAndIdIsNot(long beneficiaryId, long nomenclatorId, long statusId, long id);

    @Query(value = "select it from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization as medAuth " +
            "inner join it.medicalCoverageItem as medCovItem " +
            "inner join medCovItem.medicalCoverage as medCov " +
            "inner join medCov.insurancePlan as insPlan " +
            "where medAuth.beneficiary.id =?1 " +
            "and it.nomenclator.id = ?2 " +
            "and it.status.id = ?3 " +
            "and it.chargeSubtotal = 0 " +
            "and insPlan.id = ?4 " +
            "and (it.createdAt between ?5 and ?6) ")
    List<MedicalAuthorizationItem> findAllFreeMedAuthItemsByBeneficiaryNomenclatorStatusPlanBetweenDates(
            long beneficiaryId,
            long nomenclatorId,
            long statusId,
            long planId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    );
}
