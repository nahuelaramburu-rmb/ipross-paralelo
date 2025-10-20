package com.capacidad.validationapi.module.organization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.dashboard.dto.*;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.dashboard.service.DashboardCommand;
import com.capacidad.validationapi.module.dashboard.service.impl.BaseDashboardCommand;
import com.capacidad.validationapi.module.organization.repository.OrganizationMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.organization.repository.OrganizationSettlementDashboardRepository;
import com.capacidad.validationapi.module.organization.service.OrganizationDashboard;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.AMOUNT_FILTER;

@Service
public class OrganizationDashboardServiceImpl extends BaseDashboardCommand implements DashboardCommand, OrganizationDashboard {

    private final ContractMediator contractMediator;
    private final OrganizationMedicalAuthorizationDashboardRepository organizationMedAuthDashboardRepository;
    private final OrganizationSettlementDashboardRepository settlementDashboardRepository;
    private final DashboardUtils dashboardUtils;

    @Autowired
    public OrganizationDashboardServiceImpl(ContractMediator contractMediator,
                                            OrganizationMedicalAuthorizationDashboardRepository organizationMedAuthDashboardRepository,
                                            OrganizationSettlementDashboardRepository settlementDashboardRepository,
                                            DashboardUtils dashboardUtils) {
        this.contractMediator = contractMediator;
        this.organizationMedAuthDashboardRepository = organizationMedAuthDashboardRepository;
        this.settlementDashboardRepository = settlementDashboardRepository;
        this.dashboardUtils = dashboardUtils;
    }

    @Override
    public Tendency authorizationsCount(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return organizationMedAuthDashboardRepository
                .countAllByCreatedAtBetween(parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        contracts,
                        resourceId);
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByStatus(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return organizationMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByStatus(parsedDates.get(0),
                        parsedDates.get(1),
                        contracts,
                        resourceId);
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByPractitioners(JsonNode params) {
        return Collections.emptyList();
    }

    @Override
    public FilteredTendencyGraph authorizationsCountGroupedByGender(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<XYPoint> xyPoints = organizationMedAuthDashboardRepository
                .authorizationsGraphGroupedByGender(parsedDates.get(0), parsedDates.get(1), contracts, resourceId);
        List<Tendency> tendencies = organizationMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByGender
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3),
                                contracts,
                                resourceId);
        return new FilteredTendencyGraph(tendencies, xyPoints);
    }

    @Override
    public FilteredTendencyGraph authorizationsCountGroupedByAge(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<XYPoint> xyPoints = dashboardUtils.buildXYPointListFromTuple(organizationMedAuthDashboardRepository
                .authorizationsGraphGroupedByAge(parsedDates.get(0), parsedDates.get(1), contracts, resourceId));
        List<Tendency> tendencies = dashboardUtils.buildTendencyListFromTuple(organizationMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByAge
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3),
                                contracts,
                                resourceId));
        return new FilteredTendencyGraph(tendencies, xyPoints);
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedByCity(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return organizationMedAuthDashboardRepository
                .authorizationsGraphGroupedByCity(parsedDates.get(0),
                        parsedDates.get(1),
                        contracts,
                        resourceId,
                        PageRequest.of(0, amount));
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedBySpecialty(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        int amount = params.get(AMOUNT_FILTER).asInt();
        long specialtyId = dashboardUtils.parseSpecialtyIdOrDefault(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        List<XYPoint> allPoints = organizationMedAuthDashboardRepository
                .authorizationsGraphGroupedBySpecialty(parsedDates.get(0),
                        parsedDates.get(1),
                        specialtyId,
                        contracts,
                        resourceId);
        return dashboardUtils.buildReducedXYPointList(allPoints, amount);
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedByDate(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return organizationMedAuthDashboardRepository
                .authorizationsGraphGroupedByDate(parsedDates.get(0),
                        parsedDates.get(1),
                        contracts,
                        resourceId);
    }

    @Override
    public FilteredTendency practitionerSettlementsRanking(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        List<Tendency> tendencies = settlementDashboardRepository
                .practitionerSettlementsRankingContracts
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3),
                                contracts,
                                resourceId,
                                PageRequest.of(0, amount));
        return new FilteredTendency(tendencies);
    }

    @Override
    public List<XYPoint> settlementsSumGroupedByDate(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return settlementDashboardRepository
                .settlementsGraphGroupedByDateContracts(parsedDates.get(0),
                        parsedDates.get(1),
                        contracts,
                        resourceId);
    }

    @Override
    public Tendency settlementsSum(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return settlementDashboardRepository
                .settlementsAcumGroupedByDateContracts(parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        contracts,
                        resourceId);
    }

    @Override
    public List<KeyValueReport> settlementsGroupedByStatus(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        Set<Contract> contracts = getOrganizationAuthContracts();
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return settlementDashboardRepository
                .settlementsClosedAtBetweenContractsGroupedByStatus(parsedDates.get(0),
                        parsedDates.get(1),
                        contracts,
                        resourceId);
    }

    private Set<Contract> getOrganizationAuthContracts() {
        try {
            return contractMediator.findAllAuthOrganizationAndRelatedContracts();
        } catch (ObjectNotFoundException e) {
            return Collections.emptySet();
        }
    }

}
