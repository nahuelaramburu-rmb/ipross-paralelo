package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.budget.repository.PractitionerBudgetDashboardRepository;
import com.capacidad.validationapi.module.dashboard.dto.FilteredTendencyGraph;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.nomenclator.reference.MedicalSpecialtyReference;
import com.capacidad.validationapi.module.person.model.Gender;
import com.capacidad.validationapi.module.practitioner.repository.PractitionerMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionDashboardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerDashboardServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private DashboardUtils dashboardUtils;

    @Mock
    private PractitionerMedicalAuthorizationDashboardRepository practitionerMedicalAuthorizationDashboardRepository;

    @Mock
    private PrescriptionDashboardRepository prescriptionDashboardRepository;

    @Mock
    private PractitionerBudgetDashboardRepository practitionerBudgetDashboardRepository;

    @InjectMocks
    private PractitionerDashboardServiceImpl practitionerDashboardService;


    @Test
    public void testAuthorizationsCountReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        UUID resourceId = UUID.randomUUID();

        Tendency tendency = new Tendency();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);
        when(practitionerMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetween
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId))
                .thenReturn(tendency);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);

        Tendency result = practitionerDashboardService.authorizationsCount(filters);

        assertThat(result).isEqualTo(tendency);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testAuthorizationsCountGroupedByStatusReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        UUID resourceId = UUID.randomUUID();

        List<KeyValueReport> expected = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(practitionerMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetweenAndStatus
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(expected);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<KeyValueReport> result = practitionerDashboardService.authorizationsCountGroupedByStatus(filters);

        assertThat(result).isEqualTo(expected);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testAuthorizationsCountGroupedByGenderReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        UUID resourceId = UUID.randomUUID();

        String genderKey = Gender.MASCULINO.name();

        XYPoint xyPoint = new XYPoint(genderKey, "10/10/1019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        Tendency tendency = new Tendency(genderKey, 10L, 5L);
        List<Tendency> tendencies = Collections.singletonList(tendency);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(practitionerMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByGender
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(points);
        when(practitionerMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetweenGroupedByGender
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId)).thenReturn(tendencies);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        FilteredTendencyGraph result = practitionerDashboardService.authorizationsCountGroupedByGender(filters);

        assertThat(result.getKeys()).contains(genderKey);
        assertThat(result.getTendencies().get(genderKey)).isEqualTo(tendency);
        assertThat(result.getGraphs().get(genderKey)).isEqualTo(points);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testAuthorizationsCountGroupedByAgeReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        UUID resourceId = UUID.randomUUID();

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
        when(practitionerMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByAge
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(rawResult1);
        when(practitionerMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetweenGroupedByAge
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId)).thenReturn(rawResult2);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        FilteredTendencyGraph result = practitionerDashboardService.authorizationsCountGroupedByAge(filters);

        assertThat(result.getKeys()).contains(ageKey);
        assertThat(result.getTendencies().get(ageKey)).isEqualTo(tendency);
        assertThat(result.getGraphs().get(ageKey)).isEqualTo(points);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testAuthorizationsCountGroupedByCityReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        UUID resourceId = UUID.randomUUID();

        XYPoint xyPoint = new XYPoint("City", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(practitionerMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByCity
                (parsedDates.get(0), parsedDates.get(1), resourceId, PageRequest.of(0, filters.get(AMOUNT_FILTER).asInt()))).thenReturn(points);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<XYPoint> result = practitionerDashboardService.authorizationsCountGroupedByCity(filters);

        assertThat(result).isEqualTo(points);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testAuthorizationsCountGroupedBySpecialtyReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        UUID resourceId = UUID.randomUUID();

        XYPoint xyPoint = new XYPoint("Specialty", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        long specialtyId = MedicalSpecialtyReference.ALL.getId();

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(dashboardUtils.parseSpecialtyIdOrDefault(filters)).thenReturn(specialtyId);
        when(practitionerMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedBySpecialty
                (parsedDates.get(0), parsedDates.get(1), specialtyId, resourceId)).thenReturn(points);
        when(dashboardUtils.buildReducedXYPointList(points, filters.get(AMOUNT_FILTER).asInt())).thenReturn(points);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<XYPoint> result = practitionerDashboardService.authorizationsCountGroupedBySpecialty(filters);

        assertThat(result).isEqualTo(points);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testAuthorizationsCountGroupedByDateReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        UUID resourceId = UUID.randomUUID();

        XYPoint xyPoint = new XYPoint("10/10/2019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(practitionerMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByDate
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(points);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<XYPoint> result = practitionerDashboardService.authorizationsCountGroupedByDate(filters);

        assertThat(result).isEqualTo(points);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testBudgetSumReturnsValidResultWhenNotEmpty() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        PractitionerBudget practitionerBudget = new PractitionerBudget();
        practitionerBudget.setTotal(new BigDecimal("12331.5"));
        PractitionerBudget practitionerBudget1 = new PractitionerBudget();
        practitionerBudget1.setTotal(new BigDecimal("32123.45"));

        List<PractitionerBudget> budgets = new ArrayList<>();
        budgets.add(practitionerBudget);
        budgets.add(practitionerBudget1);

        UUID resourceId = UUID.randomUUID();

        when(practitionerBudgetDashboardRepository.findAllByStatusIdAndPractitionerResourceId
                (StatusReference.NOT_PAYED.getId(), resourceId)).thenReturn(budgets);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        BigDecimal result = practitionerDashboardService.budgetSum(objectMapper.createObjectNode());

        assertThat(result).isEqualTo(practitionerBudget.getTotal().add(practitionerBudget1.getTotal()));

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testBudgetSumReturnsZeroWhenEmpty() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        UUID resourceId = UUID.randomUUID();

        when(practitionerBudgetDashboardRepository.findAllByStatusIdAndPractitionerResourceId
                (StatusReference.NOT_PAYED.getId(), resourceId)).thenReturn(Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        BigDecimal result = practitionerDashboardService.budgetSum(objectMapper.createObjectNode());

        assertThat(result).isEqualTo(new BigDecimal(0));

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testPrescriptionsCountReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        UUID resourceId = UUID.randomUUID();

        Tendency expected = new Tendency();

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(prescriptionDashboardRepository.countAllByCreatedAtBetweenPractitioner
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId))
                .thenReturn(expected);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        Tendency result = practitionerDashboardService.prescriptionsCount(filters);

        assertThat(result).isEqualTo(expected);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testPrescriptionsCountGroupedByDateReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        UUID resourceId = UUID.randomUUID();

        List<XYPoint> points = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(prescriptionDashboardRepository.prescriptionsGraphGroupedByDatePractitioner
                (parsedDates.get(0), parsedDates.get(1), resourceId))
                .thenReturn(points);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<XYPoint> result = practitionerDashboardService.prescriptionsCountGroupedByDate(filters);

        assertThat(result).isEqualTo(points);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testPrescriptionsCountGroupedByStatusReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        UUID resourceId = UUID.randomUUID();

        List<KeyValueReport> keyValueReports = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(prescriptionDashboardRepository.countAllByCreatedAtBetweenGroupedByStatusPractitioner
                (parsedDates.get(0), parsedDates.get(1), resourceId))
                .thenReturn(keyValueReports);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<KeyValueReport> result = practitionerDashboardService.prescriptionsCountGroupedByStatus(filters);

        assertThat(result).isEqualTo(keyValueReports);

        SecurityContextHolder.setContext(defaultContext);
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
