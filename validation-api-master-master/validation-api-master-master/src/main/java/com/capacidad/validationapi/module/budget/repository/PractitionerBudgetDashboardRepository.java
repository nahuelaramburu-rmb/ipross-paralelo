package com.capacidad.validationapi.module.budget.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@TenantFilter
public interface PractitionerBudgetDashboardRepository extends ExtendedJpaRepository<PractitionerBudget, Long> {

    List<PractitionerBudget> findAllByStatusIdAndPractitionerResourceId(Long statusId, UUID resourceId);

    List<PractitionerBudget> findAllByStatusIdAndMedicalCenterResourceId(Long statusId, UUID resourceId);

    @Query(value = "select new com.capacidad.validationapi.module.dashboard.dto.KeyValueReport" +
            "(p.id, sum(b.total)) " +
            "from PractitionerBudget b " +
            "inner join b.practitioner p " +
            "inner join b.medicalCenter m " +
            "inner join b.status s " +
            "where s.id = ?1 and m.resourceId = ?2 " +
            "group by p.id")
    List<KeyValueReport> budgetSumGroupedByPractitioners(Long statusId, UUID resourceId);

}
