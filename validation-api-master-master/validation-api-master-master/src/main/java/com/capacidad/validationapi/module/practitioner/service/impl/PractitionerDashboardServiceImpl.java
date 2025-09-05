package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.budget.repository.PractitionerBudgetDashboardRepository;
import com.capacidad.validationapi.module.dashboard.dto.FilteredTendencyGraph;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.dashboard.service.DashboardCommand;
import com.capacidad.validationapi.module.dashboard.service.impl.BaseDashboardCommand;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.practitioner.repository.PractitionerMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.practitioner.service.PractitionerDashboard;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionDashboardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.AMOUNT_FILTER;

@Service
public class PractitionerDashboardServiceImpl extends BaseDashboardCommand implements DashboardCommand, PractitionerDashboard {

    private final PractitionerMedicalAuthorizationDashboardRepository practitionerMedAuthDashboardRepository;
    private final PractitionerBudgetDashboardRepository practitionerBudgetDashboardRepository;
    private final PrescriptionDashboardRepository prescriptionDashboardRepository;
    private final DashboardUtils dashboardUtils;

    @Autowired
    public PractitionerDashboardServiceImpl(PractitionerMedicalAuthorizationDashboardRepository practitionerMedAuthDashboardRepository,
                                            PractitionerBudgetDashboardRepository practitionerBudgetDashboardRepository,
                                            PrescriptionDashboardRepository prescriptionDashboardRepository,
                                            DashboardUtils dashboardUtils) {
        this.practitionerBudgetDashboardRepository = practitionerBudgetDashboardRepository;
        this.practitionerMedAuthDashboardRepository = practitionerMedAuthDashboardRepository;
        this.prescriptionDashboardRepository = prescriptionDashboardRepository;
        this.dashboardUtils = dashboardUtils;
    }

    @Override
    public Tendency authorizationsCount(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return practitionerMedAuthDashboardRepository
                .countAllByCreatedAtBetween(parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId);
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByStatus(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return practitionerMedAuthDashboardRepository
                .countAllByCreatedAtBetweenAndStatus(parsedDates.get(0), parsedDates.get(1), resourceId);
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByPractitioners(JsonNode params) {
        return Collections.emptyList();
    }

    @Override
    public FilteredTendencyGraph authorizationsCountGroupedByGender(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        List<XYPoint> xyPoints = practitionerMedAuthDashboardRepository
                .authorizationsGraphGroupedByGender(parsedDates.get(0), parsedDates.get(1), resourceId);
        List<Tendency> tendencies = practitionerMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByGender
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3),
                                resourceId);
        return new FilteredTendencyGraph(tendencies, xyPoints);
    }

    @Override
    public FilteredTendencyGraph authorizationsCountGroupedByAge(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        List<XYPoint> xyPoints = dashboardUtils.buildXYPointListFromTuple(practitionerMedAuthDashboardRepository
                .authorizationsGraphGroupedByAge(parsedDates.get(0), parsedDates.get(1), resourceId));
        List<Tendency> tendencies = dashboardUtils.buildTendencyListFromTuple(practitionerMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByAge
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3),
                                resourceId));
        return new FilteredTendencyGraph(tendencies, xyPoints);
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedByCity(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        return practitionerMedAuthDashboardRepository
                .authorizationsGraphGroupedByCity(parsedDates.get(0), parsedDates.get(1), resourceId, PageRequest.of(0, amount));
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedBySpecialty(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        long specialtyId = dashboardUtils.parseSpecialtyIdOrDefault(params);
        List<XYPoint> allPoints = practitionerMedAuthDashboardRepository
                .authorizationsGraphGroupedBySpecialty(parsedDates.get(0), parsedDates.get(1), specialtyId, resourceId);
        return dashboardUtils.buildReducedXYPointList(allPoints, amount);
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedByDate(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return practitionerMedAuthDashboardRepository
                .authorizationsGraphGroupedByDate(parsedDates.get(0), parsedDates.get(1), resourceId);
    }

    @Override
    public BigDecimal budgetSum(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<PractitionerBudget> budgetList = practitionerBudgetDashboardRepository
                .findAllByStatusIdAndPractitionerResourceId(StatusReference.NOT_PAYED.getId(), resourceId);
        if (!budgetList.isEmpty()) {
            List<BigDecimal> totals = budgetList.stream()
                    .map(Budget::getTotal)
                    .collect(Collectors.toUnmodifiableList());
            return totals.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return new BigDecimal(0);
    }

    @Override
    public List<KeyValueReport> budgetSumGroupedByPractitioners(JsonNode params) {
        return Collections.emptyList();
    }

    @Override
    public Tendency prescriptionsCount(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return prescriptionDashboardRepository
                .countAllByCreatedAtBetweenPractitioner(parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        resourceId);
    }

    @Override
    public List<XYPoint> prescriptionsCountGroupedByDate(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return prescriptionDashboardRepository
                .prescriptionsGraphGroupedByDatePractitioner(parsedDates.get(0),
                        parsedDates.get(1),
                        resourceId);
    }

    @Override
    public List<KeyValueReport> prescriptionsCountGroupedByStatus(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return prescriptionDashboardRepository
                .countAllByCreatedAtBetweenGroupedByStatusPractitioner(parsedDates.get(0), parsedDates.get(1), resourceId);
    }
}
