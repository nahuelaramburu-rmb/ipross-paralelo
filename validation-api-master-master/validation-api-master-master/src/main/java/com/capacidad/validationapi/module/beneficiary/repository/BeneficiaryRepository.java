package com.capacidad.validationapi.module.beneficiary.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeCompanyReport;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeMonthlyReport;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import com.capacidad.validationapi.module.person.repository.BasePersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@TenantFilter
@Repository
public interface BeneficiaryRepository extends BasePersonRepository<Beneficiary, Long> {

    @TenantFilter(active = false)
    List<BeneficiaryProjection.Verification> findAllProjectedByIdNumberAndIdTypeIdAndBirthDateAndBeneficiaryCode(Long idNumber, Long idTypeId, LocalDate birthDate, String beneficiaryCode);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Beneficiary> findLockedById(long beneficiaryId);

    Optional<BeneficiaryProjection> findProjectedByIdNumberAndIdTypeId(Long idNumber, Long idTypeId);

    Optional<BeneficiaryProjection> findProjectedByBeneficiaryCode(String beneficiaryCode);

    Optional<Beneficiary> findByBeneficiaryCode(String beneficiaryCode);

    Set<Beneficiary> findAllByRelatedBeneficiaryId(long relatedBeneficiaryId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Beneficiary> findLockedByBeneficiaryCode(String beneficiaryCode);

    Optional<PersonDetailProjection> findBeneficiaryProjectedById(Long beneficiaryId);

    Optional<BeneficiaryProjection> findProjectedByResourceId(UUID resourceId);

    Optional<Beneficiary> findByResourceId(UUID resourceId);

    List<BeneficiaryProjection> findAllProjectedByFamilyIdAndIdIsNot(UUID familyId, Long beneficiaryId, Sort sort);

    List<Beneficiary> findAllByFamilyIdAndIdIsNot(UUID familyId, Long beneficiaryId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Beneficiary> findLockedByIdNumberAndIdTypeId(Long idNumber, Long idTypeId);

    Set<Beneficiary> findAllByFamilyId(UUID familyId);

    boolean existsByIdAndResourceId(Long beneficiaryId, UUID resourceId);

    boolean existsByResourceIdAndFamilyId(UUID resourceId, UUID familyId);

    @Query(value = "select new com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeMonthlyReport" +
            "(sum(it.chargeSubtotal), function('MONTH', m.createdAt), function('YEAR', m.createdAt)) " +
            "from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization m " +
            "inner join it.status s " +
            "where m.beneficiary.id = ?1 and s.id = ?2 " +
            "group by m.beneficiary.id, function('MONTH', m.createdAt), function('YEAR', m.createdAt)" +
            "order by function('YEAR', m.createdAt) desc, function('MONTH', m.createdAt) desc")
    Page<BeneficiaryChargeMonthlyReport> sumChargeTotalsGroupedByBeneficiary(Long beneficiaryId, Long statusId, Pageable pageable);

    @Query(value = "select new com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeCompanyReport" +
            "(function('MONTH', m.createdAt), function('YEAR', m.createdAt), c.name, sum(it.chargeSubtotal), pm.name) " +
            "from MedicalAuthorizationItem it " +
            "inner join it.medicalAuthorization m " +
            "inner join it.status s " +
            "inner join m.paymentMethod pm " +
            "left join m.company c " +
            "where m.beneficiary.id = ?1 and s.id = ?2 " +
            "group by m.beneficiary.id, function('MONTH', m.createdAt), function('YEAR', m.createdAt), c.name, pm.name " +
            "order by function('YEAR', m.createdAt) desc, function('MONTH', m.createdAt) desc")
    Page<BeneficiaryChargeCompanyReport> sumChargeTotalsGroupedByBeneficiaryAndCompany(Long beneficiaryId, Long statusId, Pageable pageable);

}
