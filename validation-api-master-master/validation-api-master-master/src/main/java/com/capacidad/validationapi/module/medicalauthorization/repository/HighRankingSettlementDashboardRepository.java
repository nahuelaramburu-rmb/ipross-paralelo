package com.capacidad.validationapi.module.medicalauthorization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@TenantFilter
public interface HighRankingSettlementDashboardRepository extends ExtendedJpaRepository<Settlement, Long> {

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(concat(p.lastName,', ',p.name,' (',idt.alias,') ', p.idNumber), " +
            "sum(s.total), (select sum(s2.total) from Settlement s2 inner join s2.practitioner p2 " +
            "where (s2.closedAt between ?3 and ?4) and p2.id = p.id)) " +
            "from Settlement s " +
            "inner join s.practitioner p " +
            "inner join p.idType idt " +
            "where (s.closedAt between ?1 and ?2) " +
            "group by concat(p.lastName,', ',p.name,' (',idt.alias,') ', p.idNumber), p.id " +
            "order by sum(s.total) desc")
    List<Tendency> practitionerSettlementsRanking
            (LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo, Pageable pageable);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.KeyValueReport(s.name, count(st)) " +
            "from Settlement st " +
            "inner join st.status s " +
            "where ((st.closedAt between ?1 and ?2) or (st.closedAt is null and st.createdAt between ?1 and ?2)) " +
            "group by s.name")
    List<KeyValueReport> settlementsClosedAtBetweenGroupedByStatus
            (LocalDateTime from, LocalDateTime to);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(" +
            "function('to_char', function('date_tz', s.closedAt, 'UTC'), 'dd/MM/yyyy'), sum(s.total)) " +
            "from Settlement s " +
            "where s.closedAt between ?1 and ?2 " +
            "group by function('date_tz', s.closedAt, 'UTC') " +
            "order by function('date_tz', s.closedAt, 'UTC')")
    List<XYPoint> settlementsGraphGroupedByDate(LocalDateTime from, LocalDateTime to);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(" +
            "sum(case when s.closedAt between ?1 and ?2 then s.total else 0 end), " +
            "sum(case when s.closedAt between ?3 and ?4 then s.total else 0 end)) " +
            "from Settlement s")
    Tendency settlementsAcumGroupedByDate(LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo);

}
