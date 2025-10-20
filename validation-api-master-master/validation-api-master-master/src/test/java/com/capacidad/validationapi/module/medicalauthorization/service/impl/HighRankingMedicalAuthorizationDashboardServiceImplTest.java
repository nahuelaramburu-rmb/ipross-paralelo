package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.module.dashboard.dto.*;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.medicalauthorization.repository.HighRankingMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.medicalauthorization.repository.HighRankingSettlementDashboardRepository;
import com.capacidad.validationapi.module.nomenclator.reference.MedicalSpecialtyReference;
import com.capacidad.validationapi.module.person.model.Gender;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionDashboardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HighRankingMedicalAuthorizationDashboardServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HighRankingMedicalAuthorizationDashboardRepository highRankingMedAuthDashboardRepository;

    @Mock
    private HighRankingSettlementDashboardRepository settlementDashboardRepository;

    @Mock
    private PrescriptionDashboardRepository prescriptionDashboardRepository;

    @Mock
    private DashboardUtils dashboardUtils;

    @InjectMocks
    private HighRankingMedicalAuthorizationDashboardServiceImpl highRankingMedicalAuthorizationDashboardService;

    @Test
    public void testAuthorizationsCountReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        Tendency tendency = new Tendency();

        when(highRankingMedAuthDashboardRepository.countAllByCreatedAtBetween
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3)))
                .thenReturn(tendency);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);

        Tendency result = highRankingMedicalAuthorizationDashboardService.authorizationsCount(filters);

        assertThat(result).isEqualTo(tendency);
    }

    @Test
    public void testAuthorizationsCountGroupedByStatusReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        List<KeyValueReport> expected = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(highRankingMedAuthDashboardRepository.countAllByCreatedAtBetweenGroupedByStatus
                (parsedDates.get(0), parsedDates.get(1))).thenReturn(expected);

        List<KeyValueReport> result = highRankingMedicalAuthorizationDashboardService
                .authorizationsCountGroupedByStatus(filters);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testAuthorizationsCountGroupedByGenderReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        String genderKey = Gender.MASCULINO.name();

        XYPoint xyPoint = new XYPoint(genderKey, "10/10/1019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        Tendency tendency = new Tendency(genderKey, 10L, 5L);
        List<Tendency> tendencies = Collections.singletonList(tendency);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(highRankingMedAuthDashboardRepository.authorizationsGraphGroupedByGender
                (parsedDates.get(0), parsedDates.get(1))).thenReturn(points);
        when(highRankingMedAuthDashboardRepository.countAllByCreatedAtBetweenGroupedByGender
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3))).thenReturn(tendencies);

        FilteredTendencyGraph result = highRankingMedicalAuthorizationDashboardService
                .authorizationsCountGroupedByGender(filters);

        assertThat(result.getKeys()).contains(genderKey);
        assertThat(result.getTendencies().get(genderKey)).isEqualTo(tendency);
        assertThat(result.getGraphs().get(genderKey)).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedByAgeReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        String ageKey = "10-19";

        XYPoint xyPoint = new XYPoint(ageKey, "10/10/1019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        Tendency tendency = new Tendency(ageKey, 10L, 5L);
        List<Tendency> tendencies = Collections.singletonList(tendency);

        List<Tuple> rawResult1 = mock(List.class);
        List<Tuple> rawResult2 = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(dashboardUtils.buildXYPointListFromTuple(rawResult1)).thenReturn(points);
        when(dashboardUtils.buildTendencyListFromTuple(rawResult2)).thenReturn(tendencies);
        when(highRankingMedAuthDashboardRepository.authorizationsGraphGroupedByAge
                (parsedDates.get(0), parsedDates.get(1))).thenReturn(rawResult1);
        when(highRankingMedAuthDashboardRepository.countAllByCreatedAtBetweenGroupedByAge
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3))).thenReturn(rawResult2);

        FilteredTendencyGraph result = highRankingMedicalAuthorizationDashboardService
                .authorizationsCountGroupedByAge(filters);

        assertThat(result.getKeys()).contains(ageKey);
        assertThat(result.getTendencies().get(ageKey)).isEqualTo(tendency);
        assertThat(result.getGraphs().get(ageKey)).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedByCityReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        XYPoint xyPoint = new XYPoint("City", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(highRankingMedAuthDashboardRepository.authorizationsGraphGroupedByCity
                (parsedDates.get(0), parsedDates.get(1), PageRequest.of(0, filters.get(AMOUNT_FILTER).asInt()))).thenReturn(points);

        List<XYPoint> result = highRankingMedicalAuthorizationDashboardService
                .authorizationsCountGroupedByCity(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedBySpecialtyReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        XYPoint xyPoint = new XYPoint("Specialty", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        long specialtyId = MedicalSpecialtyReference.ALL.getId();

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(dashboardUtils.parseSpecialtyIdOrDefault(filters)).thenReturn(specialtyId);
        when(highRankingMedAuthDashboardRepository.authorizationsGraphGroupedBySpecialty
                (parsedDates.get(0), parsedDates.get(1), specialtyId)).thenReturn(points);
        when(dashboardUtils.buildReducedXYPointList(points, filters.get(AMOUNT_FILTER).asInt())).thenReturn(points);

        List<XYPoint> result = highRankingMedicalAuthorizationDashboardService
                .authorizationsCountGroupedBySpecialty(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedByDateReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        XYPoint xyPoint = new XYPoint("10/10/2019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(highRankingMedAuthDashboardRepository.authorizationsGraphGroupedByDate
                (parsedDates.get(0), parsedDates.get(1))).thenReturn(points);

        List<XYPoint> result = highRankingMedicalAuthorizationDashboardService
                .authorizationsCountGroupedByDate(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testPractitionerSettlementsRankingReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        String tendencyKey = "Practitioner";
        Tendency tendency = new Tendency(tendencyKey, new BigDecimal("1234.5"), new BigDecimal("32123.5"));
        List<Tendency> tendencies = Collections.singletonList(tendency);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.practitionerSettlementsRanking
                (parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        PageRequest.of(0,
                                filters.get(AMOUNT_FILTER).asInt()))).thenReturn(tendencies);

        FilteredTendency result = highRankingMedicalAuthorizationDashboardService
                .practitionerSettlementsRanking(filters);

        assertThat(result.getTendencies().get(tendencyKey)).isEqualTo(tendency);
    }

    @Test
    public void testSettlementsSumGroupedByDateReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        XYPoint xyPoint = new XYPoint("10/10/2019", new BigDecimal("12312.5"));
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsGraphGroupedByDate
                (parsedDates.get(0), parsedDates.get(1))).thenReturn(points);

        List<XYPoint> result = highRankingMedicalAuthorizationDashboardService
                .settlementsSumGroupedByDate(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testSettlementsSumReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Tendency tendency = new Tendency(new BigDecimal("12312.45"), new BigDecimal("1212212.34"));

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsAcumGroupedByDate
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3))).thenReturn(tendency);

        Tendency result = highRankingMedicalAuthorizationDashboardService
                .settlementsSum(filters);

        assertThat(result).isEqualTo(tendency);
    }

    @Test
    public void testSettlementsClosedCountReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        List<KeyValueReport> expected = Collections.emptyList();

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsClosedAtBetweenGroupedByStatus
                (parsedDates.get(0), parsedDates.get(1))).thenReturn(expected);

        List<KeyValueReport> result = highRankingMedicalAuthorizationDashboardService
                .settlementsGroupedByStatus(filters);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testPrescriptionsCountReturnsValidResult() {
        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        Tendency expected = new Tendency();

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(prescriptionDashboardRepository.countAllByCreatedAtBetween
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3)))
                .thenReturn(expected);

        Tendency result = highRankingMedicalAuthorizationDashboardService.prescriptionsCount(filters);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testPrescriptionsCountGroupedByDateReturnsValidResult() {
        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        List<XYPoint> points = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(prescriptionDashboardRepository.prescriptionsGraphGroupedByDate
                (parsedDates.get(0), parsedDates.get(1)))
                .thenReturn(points);

        List<XYPoint> result = highRankingMedicalAuthorizationDashboardService
                .prescriptionsCountGroupedByDate(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testPrescriptionsCountGroupedByStatusReturnsValidResult() {
        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        List<KeyValueReport> keyValueReports = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(prescriptionDashboardRepository.countAllByCreatedAtBetweenGroupedByStatus
                (parsedDates.get(0), parsedDates.get(1)))
                .thenReturn(keyValueReports);

        List<KeyValueReport> result = highRankingMedicalAuthorizationDashboardService
                .prescriptionsCountGroupedByStatus(filters);

        assertThat(result).isEqualTo(keyValueReports);
    }

    private List<LocalDateTime> parseDates() {
        LocalDateTime from = LocalDate.now().minusMonths(1).atTime(0, 0);
        LocalDateTime to = LocalDate.now().atTime(23, 59, 59);
        Period period = Period.between(from.toLocalDate(), to.toLocalDate());
        LocalDateTime prevFrom = from.minus(period);
        LocalDateTime prevTo = from.minusSeconds(1);

        List<LocalDateTime> parsedDates = new ArrayList<>();
        parsedDates.add(from);
        parsedDates.add(to);
        parsedDates.add(prevFrom);
        parsedDates.add(prevTo);

        return parsedDates;
    }

    private ObjectNode buildFilters() {
        ObjectNode filters = objectMapper.createObjectNode();
        filters.put(DATE_FROM_FILTER, LocalDate.now().minusMonths(1).toString());
        filters.put(DATE_TO_FILTER, LocalDate.now().toString());
        return filters;
    }


}
