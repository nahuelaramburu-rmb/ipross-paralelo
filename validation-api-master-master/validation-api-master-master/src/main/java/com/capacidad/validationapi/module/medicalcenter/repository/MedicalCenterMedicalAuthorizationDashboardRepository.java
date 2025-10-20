package com.capacidad.validationapi.module.medicalcenter.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@TenantFilter
public interface MedicalCenterMedicalAuthorizationDashboardRepository extends JpaRepository<BaseEntity<Long>, Long> {

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(" +
            "sum(case when m.createdAt between ?1 and ?2 then 1 else 0 end), " +
            "sum(case when m.createdAt between ?3 and ?4 then 1 else 0 end)) " +
            "from MedicalAuthorization m " +
            "inner join m.medicalCenter med " +
            "where med.resourceId = ?5")
    Tendency countAllByCreatedAtBetween
            (LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.KeyValueReport(s.name, count(m)) " +
            "from MedicalAuthorization m " +
            "inner join m.status s " +
            "inner join m.medicalCenter med " +
            "where (m.createdAt between ?1 and ?2) and med.resourceId = ?3 " +
            "group by s.name")
    List<KeyValueReport> countAllByCreatedAtBetweenGroupedByStatus(LocalDateTime from, LocalDateTime to, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.KeyValueReport(p.id, count(m)) " +
            "from MedicalAuthorization m " +
            "inner join m.medicalCenter med " +
            "inner join m.practitioner p " +
            "where (m.createdAt between ?1 and ?2) and med.resourceId = ?3 " +
            "group by p.id")
    List<KeyValueReport> authorizationsCountGroupedByPractitioners(LocalDateTime from, LocalDateTime to, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(b.gender, " +
            "sum(case when m.createdAt between ?1 and ?2 then 1 else 0 end), " +
            "sum(case when m.createdAt between ?3 and ?4 then 1 else 0 end)) " +
            "from MedicalAuthorization m " +
            "inner join m.beneficiary b " +
            "inner join m.medicalCenter med " +
            "where med.resourceId = ?5 " +
            "group by b.gender " +
            "order by b.gender")
    List<Tendency> countAllByCreatedAtBetweenGroupedByGender
            (LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(b.gender, function('to_char', function('date_tz', m.createdAt, 'UTC'), 'dd/MM/yyyy'), count(m))" +
            "from MedicalAuthorization m " +
            "inner join m.beneficiary b " +
            "inner join m.medicalCenter med " +
            "where (m.createdAt between ?1 and ?2) and med.resourceId = ?3 " +
            "group by b.gender, function('date_tz', m.createdAt, 'UTC') " +
            "order by b.gender, function('date_tz', m.createdAt, 'UTC')")
    List<XYPoint> authorizationsGraphGroupedByGender(LocalDateTime from, LocalDateTime to, UUID resourceId);

    @Query(value = "select case " +
            "when function('date_part', 'year', function('age', b.birthDate)) <= 9 then '0-9' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 10 and 19 then '10-19' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 20 and 29 then '20-29' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 30 and 39 then '30-39' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 40 and 49 then '40-49' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 50 and 59 then '50-59' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 60 and 69 then '60-69' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 70 and 79 then '70-79' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 80 and 89 then '80-89' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 90 and 99 then '90-99' " +
            "else '+99' end as key, sum(case when m.createdAt between ?1 and ?2 then 1 else 0 end) as curr, sum(case when m.createdAt between ?3 and ?4 then 1 else 0 end) as prev " +
            "from MedicalAuthorization m " +
            "inner join m.beneficiary b " +
            "inner join m.medicalCenter med " +
            "where med.resourceId = ?5 " +
            "group by key " +
            "order by key")
    List<Tuple> countAllByCreatedAtBetweenGroupedByAge
            (LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo, UUID resourceId);

    @Query(value = "select (case " +
            "when function('date_part', 'year', function('age', b.birthDate)) <= 9 then '0-9' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 10 and 19 then '10-19' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 20 and 29 then '20-29' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 30 and 39 then '30-39' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 40 and 49 then '40-49' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 50 and 59 then '50-59' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 60 and 69 then '60-69' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 70 and 79 then '70-79' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 80 and 89 then '80-89' " +
            "when function('date_part', 'year', function('age', b.birthDate)) between 90 and 99 then '90-99' " +
            "else '+99' end) as key, function('to_char', function('date_tz', m.createdAt, 'UTC'), 'dd/MM/yyyy') as x, count(m) as y " +
            "from MedicalAuthorization m " +
            "inner join m.beneficiary b " +
            "inner join m.medicalCenter med " +
            "where (m.createdAt between ?1 and ?2) and med.resourceId = ?3 " +
            "group by key, function('date_tz', m.createdAt, 'UTC') " +
            "order by key, function('date_tz', m.createdAt, 'UTC')")
    List<Tuple> authorizationsGraphGroupedByAge
            (LocalDateTime from, LocalDateTime to, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(c.name, count(m)) " +
            "from MedicalAuthorization m " +
            "inner join m.city c " +
            "inner join m.medicalCenter med " +
            "where (m.createdAt between ?1 and ?2) and med.resourceId = ?3 " +
            "group by c.name " +
            "order by count(m) desc")
    List<XYPoint> authorizationsGraphGroupedByCity(LocalDateTime from, LocalDateTime to, UUID resourceId, Pageable pageable);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(mp.name, count(it)) " +
            "from MedicalAuthorizationItem it " +
            "inner join it.nomenclator n " +
            "inner join n.medicalPractice mp " +
            "inner join mp.medicalSpecialties ms " +
            "inner join it.medicalAuthorization m " +
            "inner join m.medicalCenter med " +
            "where (it.createdAt between ?1 and ?2) and ms.id = ?3 and med.resourceId = ?4 " +
            "group by mp.name " +
            "order by mp.name")
    List<XYPoint> authorizationsGraphGroupedBySpecialty(LocalDateTime from, LocalDateTime to, Long specialtyId, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(" +
            "function('to_char', function('date_tz', m.createdAt, 'UTC'), 'dd/MM/yyyy'), count(m)) " +
            "from MedicalAuthorization m " +
            "inner join m.medicalCenter med " +
            "where (m.createdAt between ?1 and ?2) and med.resourceId = ?3 " +
            "group by function('date_tz', m.createdAt, 'UTC') " +
            "order by function('date_tz', m.createdAt, 'UTC')")
    List<XYPoint> authorizationsGraphGroupedByDate(LocalDateTime from, LocalDateTime to, UUID resourceId);

}
