package com.capacidad.validationapi.module.organization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@TenantFilter
public interface OrganizationSettlementDashboardRepository extends ExtendedJpaRepository<Settlement, Long> {

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(concat(p.lastName,', ',p.name,' (',idt.alias,') ', p.idNumber), " +
            "sum(s.total), (select sum(s2.total) from Settlement s2 inner join s2.practitioner p2 " +
            "where (s2.closedAt between ?3 and ?4) and s2.contract in ?5 and p2.id = p.id)) " +
            "from Settlement s " +
            "inner join s.practitioner p " +
            "inner join p.idType idt " +
            "inner join p.medicalRegistrations pmr " +
            "inner join pmr.organization org " +
            "where (s.closedAt between ?1 and ?2) and s.contract in ?5 and org.resourceId = ?6 " +
            "group by concat(p.lastName,', ',p.name,' (',idt.alias,') ', p.idNumber), p.id " +
            "order by sum(s.total) desc")
    List<Tendency> practitionerSettlementsRankingContracts
            (LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo, Set<Contract> contracts, UUID resourceId, Pageable pageable);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.KeyValueReport(s.name, count(st)) " +
            "from Settlement st " +
            "inner join st.status s " +
            "inner join st.practitioner p " +
            "inner join p.medicalRegistrations mrg " +
            "inner join mrg.organization org " +
            "where ((st.closedAt between ?1 and ?2) or (st.closedAt is null and st.createdAt between ?1 and ?2)) and st.contract in ?3 and org.resourceId = ?4 " +
            "group by s.name")
    List<KeyValueReport> settlementsClosedAtBetweenContractsGroupedByStatus
            (LocalDateTime from, LocalDateTime to, Set<Contract> contracts, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.Tendency(" +
            "sum(case when s.closedAt between ?1 and ?2 then s.total else 0 end), " +
            "sum(case when s.closedAt between ?3 and ?4 then s.total else 0 end)) " +
            "from Settlement s " +
            "inner join s.practitioner p " +
            "inner join p.medicalRegistrations mrg " +
            "inner join mrg.organization org " +
            "where s.contract in ?5 and org.resourceId = ?6")
    Tendency settlementsAcumGroupedByDateContracts(LocalDateTime from, LocalDateTime to, LocalDateTime prevFrom, LocalDateTime prevTo, Set<Contract> contracts, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.XYPoint(" +
            "function('to_char', function('date_tz', s.closedAt, 'UTC'), 'dd/MM/yyyy'), sum(s.total)) " +
            "from Settlement s " +
            "inner join s.practitioner p " +
            "inner join p.medicalRegistrations mrg " +
            "inner join mrg.organization org " +
            "where (s.closedAt between ?1 and ?2) and s.contract in ?3 and org.resourceId = ?4 " +
            "group by function('date_tz', s.closedAt, 'UTC') " +
            "order by function('date_tz', s.closedAt, 'UTC')")
    List<XYPoint> settlementsGraphGroupedByDateContracts(LocalDateTime from, LocalDateTime to, Set<Contract> contracts, UUID resourceId);

}
