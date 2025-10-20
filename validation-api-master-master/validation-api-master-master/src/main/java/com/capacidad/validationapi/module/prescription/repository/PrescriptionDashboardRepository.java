package com.capacidad.validationapi.module.prescription.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@TenantFilter
public interface PrescriptionDashboardRepository extends JpaRepository<BaseEntity<Long>, Long> {

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(" +
            "sum(case when p.createdAt between ?1 and ?2 then 1 else 0 end), " +
            "sum(case when p.createdAt between ?3 and ?4 then 1 else 0 end)) " +
            "from Prescription p")
    Tendency countAllByCreatedAtBetween(LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(" +
            "function('to_char', function('date_tz', p.createdAt, 'UTC'), 'dd/MM/yyyy'), count(p)) " +
            "from Prescription p " +
            "where (p.createdAt between ?1 and ?2) " +
            "group by function('date_tz', p.createdAt, 'UTC') " +
            "order by function('date_tz', p.createdAt, 'UTC')")
    List<XYPoint> prescriptionsGraphGroupedByDate(LocalDateTime from, LocalDateTime to);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.KeyValueReport(s.name, count(p)) " +
            "from Prescription p " +
            "inner join p.status s " +
            "where p.createdAt between ?1 and ?2 " +
            "group by s.name")
    List<KeyValueReport> countAllByCreatedAtBetweenGroupedByStatus(LocalDateTime from, LocalDateTime to);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(" +
            "sum(case when p.createdAt between ?1 and ?2 then 1 else 0 end), " +
            "sum(case when p.createdAt between ?3 and ?4 then 1 else 0 end)) " +
            "from Prescription p " +
            "inner join p.practitioner pr " +
            "where pr.resourceId = ?5")
    Tendency countAllByCreatedAtBetweenPractitioner(LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.KeyValueReport(s.name, count(p)) " +
            "from Prescription p " +
            "inner join p.status s " +
            "inner join p.practitioner pr " +
            "where (p.createdAt between ?1 and ?2) and pr.resourceId = ?3 " +
            "group by s.name")
    List<KeyValueReport> countAllByCreatedAtBetweenGroupedByStatusPractitioner(LocalDateTime from, LocalDateTime to, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(" +
            "function('to_char', function('date_tz', p.createdAt, 'UTC'), 'dd/MM/yyyy'), count(p)) " +
            "from Prescription p " +
            "inner join p.practitioner pr " +
            "where (p.createdAt between ?1 and ?2) and pr.resourceId = ?3 " +
            "group by function('date_tz', p.createdAt, 'UTC') " +
            "order by function('date_tz', p.createdAt, 'UTC')")
    List<XYPoint> prescriptionsGraphGroupedByDatePractitioner(LocalDateTime from, LocalDateTime to, UUID resourceId);

}
