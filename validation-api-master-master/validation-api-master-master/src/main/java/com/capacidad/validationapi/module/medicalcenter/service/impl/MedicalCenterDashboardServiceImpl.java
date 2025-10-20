package com.capacidad.validationapi.module.medicalcenter.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.budget.model.Budget;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.budget.repository.PractitionerBudgetDashboardRepository;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.dashboard.dto.*;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.dashboard.service.DashboardCommand;
import com.capacidad.validationapi.module.dashboard.service.impl.BaseDashboardCommand;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.repository.MedicalCenterMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.medicalcenter.repository.MedicalCenterSettlementDashboardRepository;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterDashboard;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.AMOUNT_FILTER;

@Log4j2
@Service
public class MedicalCenterDashboardServiceImpl extends BaseDashboardCommand implements DashboardCommand, MedicalCenterDashboard {

    private final ContractMediator contractMediator;
    private final MedicalCenterService medicalCenterService;
    private final MedicalCenterMedicalAuthorizationDashboardRepository medCenMedAuthDashboardRepository;
    private final PractitionerBudgetDashboardRepository practitionerBudgetDashboardRepository;
    private final MedicalCenterSettlementDashboardRepository settlementDashboardRepository;
    private final DashboardUtils dashboardUtils;

    @Autowired
    public MedicalCenterDashboardServiceImpl(ContractMediator contractMediator,
                                             MedicalCenterService medicalCenterService,
                                             MedicalCenterMedicalAuthorizationDashboardRepository medCenMedAuthDashboardRepository,
                                             PractitionerBudgetDashboardRepository practitionerBudgetDashboardRepository,
                                             MedicalCenterSettlementDashboardRepository settlementDashboardRepository,
                                             DashboardUtils dashboardUtils) {
        this.contractMediator = contractMediator;
        this.medicalCenterService = medicalCenterService;
        this.medCenMedAuthDashboardRepository = medCenMedAuthDashboardRepository;
        this.practitionerBudgetDashboardRepository = practitionerBudgetDashboardRepository;
        this.settlementDashboardRepository = settlementDashboardRepository;
        this.dashboardUtils = dashboardUtils;
    }

    @Override
    public Tendency authorizationsCount(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return medCenMedAuthDashboardRepository
                .countAllByCreatedAtBetween(parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId);
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByStatus(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return medCenMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByStatus(parsedDates.get(0), parsedDates.get(1), resourceId);
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByPractitioners(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return medCenMedAuthDashboardRepository
                .authorizationsCountGroupedByPractitioners
                        (parsedDates.get(0), parsedDates.get(1), resourceId);
    }

    @Override
    public FilteredTendencyGraph authorizationsCountGroupedByGender(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        List<XYPoint> xyPoints = medCenMedAuthDashboardRepository
                .authorizationsGraphGroupedByGender(parsedDates.get(0), parsedDates.get(1), resourceId);
        List<Tendency> tendencies = medCenMedAuthDashboardRepository
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
        List<XYPoint> xyPoints = dashboardUtils.buildXYPointListFromTuple(medCenMedAuthDashboardRepository
                .authorizationsGraphGroupedByAge(parsedDates.get(0), parsedDates.get(1), resourceId));
        List<Tendency> tendencies = dashboardUtils.buildTendencyListFromTuple(medCenMedAuthDashboardRepository
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
        return medCenMedAuthDashboardRepository
                .authorizationsGraphGroupedByCity(parsedDates.get(0), parsedDates.get(1), resourceId, PageRequest.of(0, amount));
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedBySpecialty(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        long specialtyId = dashboardUtils.parseSpecialtyIdOrDefault(params);
        List<XYPoint> allPoints = medCenMedAuthDashboardRepository
                .authorizationsGraphGroupedBySpecialty(parsedDates.get(0), parsedDates.get(1), specialtyId, resourceId);
        return dashboardUtils.buildReducedXYPointList(allPoints, amount);
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedByDate(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return medCenMedAuthDashboardRepository
                .authorizationsGraphGroupedByDate(parsedDates.get(0), parsedDates.get(1), resourceId);
    }

    @Override
    public FilteredTendency practitionerSettlementsRanking(JsonNode params) {
        Set<Contract> contracts = contractMediator.findAllAuthMedicalCenterContracts();
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        List<Tendency> tendencies = settlementDashboardRepository
                .practitionerSettlementsRankingContracts
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3),
                                contracts,
                                PageRequest.of(0, amount));
        return new FilteredTendency(tendencies);
    }

    @Override
    public List<XYPoint> settlementsSumGroupedByDate(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = contractMediator.findAllAuthMedicalCenterContracts();
        return settlementDashboardRepository
                .settlementsGraphGroupedByDateContracts(parsedDates.get(0), parsedDates.get(1), contracts);
    }

    @Override
    public Tendency settlementsSum(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = contractMediator.findAllAuthMedicalCenterContracts();
        return settlementDashboardRepository
                .settlementsAcumGroupedByDateContracts(parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        contracts);
    }

    @Override
    public List<KeyValueReport> settlementsGroupedByStatus(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = contractMediator.findAllAuthMedicalCenterContracts();
        return settlementDashboardRepository
                .settlementsClosedAtBetweenContractsGroupedByStatus(parsedDates.get(0), parsedDates.get(1), contracts);
    }

    @Override
    public BigDecimal budgetSum(JsonNode params) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<PractitionerBudget> budgetList = practitionerBudgetDashboardRepository
                .findAllByStatusIdAndMedicalCenterResourceId(StatusReference.NOT_PAYED.getId(), resourceId);
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
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return practitionerBudgetDashboardRepository
                .budgetSumGroupedByPractitioners(StatusReference.NOT_PAYED.getId(), resourceId);
    }

    @Override
    public long practitionersCount(JsonNode params) {
        try {
            MedicalCenter medicalCenter = medicalCenterService.getAuthMedicalCenter();
            return medicalCenter.getPractitioners().size();
        } catch (ObjectNotFoundException e) {
            log.error("{} - practitionersCount - Auth Medical Center not found: {}", this.getClass(), e.getMessage());
        }
        return 0;
    }
}
