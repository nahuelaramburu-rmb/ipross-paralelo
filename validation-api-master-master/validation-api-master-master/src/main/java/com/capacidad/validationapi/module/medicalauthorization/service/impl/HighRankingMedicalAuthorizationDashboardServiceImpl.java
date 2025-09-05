package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference;
import com.capacidad.validationapi.module.dashboard.dto.*;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.dashboard.service.DashboardCommand;
import com.capacidad.validationapi.module.dashboard.service.impl.BaseDashboardCommand;
import com.capacidad.validationapi.module.medicalauthorization.repository.HighRankingMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.medicalauthorization.repository.HighRankingSettlementDashboardRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.HighRankingDashboard;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionDashboardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.PAYCHECK;
import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.AMOUNT_FILTER;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;

@Service
@Log4j2
public class HighRankingMedicalAuthorizationDashboardServiceImpl extends BaseDashboardCommand implements DashboardCommand, HighRankingDashboard {

    private final HighRankingMedicalAuthorizationDashboardRepository highRankingMedAuthDashboardRepository;
    private final HighRankingSettlementDashboardRepository settlementDashboardRepository;
    private final PrescriptionDashboardRepository prescriptionDashboardRepository;
    private final DashboardUtils dashboardUtils;

    @Autowired
    public HighRankingMedicalAuthorizationDashboardServiceImpl(HighRankingMedicalAuthorizationDashboardRepository highRankingMedAuthDashboardRepository,
                                                               HighRankingSettlementDashboardRepository settlementDashboardRepository,
                                                               DashboardUtils dashboardUtils,
                                                               PrescriptionDashboardRepository prescriptionDashboardRepository) {
        this.highRankingMedAuthDashboardRepository = highRankingMedAuthDashboardRepository;
        this.settlementDashboardRepository = settlementDashboardRepository;
        this.prescriptionDashboardRepository = prescriptionDashboardRepository;
        this.dashboardUtils = dashboardUtils;
    }

    @Override
    public Tendency authorizationsCount(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return highRankingMedAuthDashboardRepository
                .countAllByCreatedAtBetween(parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3));
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByStatus(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return highRankingMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByStatus(parsedDates.get(0), parsedDates.get(1));
    }

    @Override
    public List<KeyValueReport> authorizationsCountGroupedByPractitioners(JsonNode params) {
        return Collections.emptyList();
    }

    @Override
    public FilteredTendencyGraph authorizationsCountGroupedByGender(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        List<XYPoint> xyPoints = highRankingMedAuthDashboardRepository
                .authorizationsGraphGroupedByGender(parsedDates.get(0), parsedDates.get(1));
        List<Tendency> tendencies = highRankingMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByGender
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3));
        return new FilteredTendencyGraph(tendencies, xyPoints);
    }

    @Override
    public FilteredTendencyGraph authorizationsCountGroupedByAge(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        List<XYPoint> xyPoints = dashboardUtils.buildXYPointListFromTuple(highRankingMedAuthDashboardRepository
                .authorizationsGraphGroupedByAge(parsedDates.get(0), parsedDates.get(1)));
        List<Tendency> tendencies = dashboardUtils.buildTendencyListFromTuple(highRankingMedAuthDashboardRepository
                .countAllByCreatedAtBetweenGroupedByAge
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3)));
        return new FilteredTendencyGraph(tendencies, xyPoints);
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedByCity(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        return highRankingMedAuthDashboardRepository
                .authorizationsGraphGroupedByCity(parsedDates.get(0), parsedDates.get(1), PageRequest.of(0, amount));
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedBySpecialty(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        long specialtyId = dashboardUtils.parseSpecialtyIdOrDefault(params);
        List<XYPoint> allPoints = highRankingMedAuthDashboardRepository
                .authorizationsGraphGroupedBySpecialty(parsedDates.get(0), parsedDates.get(1), specialtyId);
        return dashboardUtils.buildReducedXYPointList(allPoints, amount);
    }

    @Override
    public List<XYPoint> authorizationsCountGroupedByDate(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return highRankingMedAuthDashboardRepository
                .authorizationsGraphGroupedByDate(parsedDates.get(0), parsedDates.get(1));
    }

    @Override
    public FilteredTendency practitionerSettlementsRanking(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        int amount = params.get(AMOUNT_FILTER).asInt();
        List<Tendency> tendencies = settlementDashboardRepository
                .practitionerSettlementsRanking
                        (parsedDates.get(0),
                                parsedDates.get(1),
                                parsedDates.get(2),
                                parsedDates.get(3),
                                PageRequest.of(0, amount));
        return new FilteredTendency(tendencies);
    }

    @Override
    public List<XYPoint> settlementsSumGroupedByDate(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return settlementDashboardRepository
                .settlementsGraphGroupedByDate(parsedDates.get(0), parsedDates.get(1));
    }

    @Override
    public Tendency settlementsSum(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return settlementDashboardRepository
                .settlementsAcumGroupedByDate(parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3));
    }

    @Override
    public List<KeyValueReport> settlementsGroupedByStatus(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return settlementDashboardRepository
                .settlementsClosedAtBetweenGroupedByStatus(parsedDates.get(0), parsedDates.get(1));
    }

    @Override
    public Tendency prescriptionsCount(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return prescriptionDashboardRepository
                .countAllByCreatedAtBetween(parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3));
    }

    @Override
    public List<XYPoint> prescriptionsCountGroupedByDate(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return prescriptionDashboardRepository
                .prescriptionsGraphGroupedByDate(parsedDates.get(0), parsedDates.get(1));
    }

    @Override
    public List<KeyValueReport> prescriptionsCountGroupedByStatus(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return prescriptionDashboardRepository
                .countAllByCreatedAtBetweenGroupedByStatus(parsedDates.get(0), parsedDates.get(1));
    }

    @Override
    public Tendency paycheckChargesTendency(JsonNode params) {
        return chargesTendency(params, PAYCHECK);
    }

    private Tendency chargesTendency(JsonNode params, PaymentMethodReference paymentMethodReference) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return highRankingMedAuthDashboardRepository
                .sumAllChargesByCreatedAtBetween(parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        VALIDATION_APPROVED.getId(),
                        paymentMethodReference.getId());
    }

    @Override
    public Tendency voluntaryChargesTendency(JsonNode params) {
        return chargesTendency(params, VOLUNTARY);
    }

    @Override
    public List<KeyValueReport> paycheckChargesSumGroupedByCompanies(JsonNode params) {
        List<LocalDateTime> parsedDates = dashboardUtils.parseDates(params);
        return highRankingMedAuthDashboardRepository
                .sumAllChargesByCreatedAtBetweenGroupedByCompany(parsedDates.get(0),
                        parsedDates.get(1),
                        VALIDATION_APPROVED.getId(),
                        PAYCHECK.getId());
    }

}
