package com.capacidad.validationapi.module.medicalcenter.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.budget.repository.PractitionerBudgetDashboardRepository;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.dashboard.dto.*;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.repository.MedicalCenterMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.medicalcenter.repository.MedicalCenterSettlementDashboardRepository;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.nomenclator.reference.MedicalSpecialtyReference;
import com.capacidad.validationapi.module.person.model.Gender;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalCenterDashboardServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SecurityContext securityContext;

    @Mock
    private MedicalCenterService medicalCenterService;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private DashboardUtils dashboardUtils;

    @Mock
    private MedicalCenterMedicalAuthorizationDashboardRepository medCenMedAuthDashboardRepository;

    @Mock
    private ContractMediator contractMediator;

    @Mock
    private MedicalCenterSettlementDashboardRepository settlementDashboardRepository;

    @Mock
    private PractitionerBudgetDashboardRepository practitionerBudgetDashboardRepository;

    @Spy
    @InjectMocks
    private MedicalCenterDashboardServiceImpl medicalCenterDashboardService;

    @Test
    public void testExecuteDoNothingIfEmptyReportRequests() {
        List<ReportRequest> reportRequests = Collections.emptyList();
        List<ReportResult> result = medicalCenterDashboardService.execute(reportRequests);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void testExecuteReturnsZeroWhenNullFiltersAndError() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setName("budgetSum");
        reportRequest.setFilters(null);

        UUID resourceId = UUID.randomUUID();

        when(practitionerBudgetDashboardRepository.findAllByStatusIdAndMedicalCenterResourceId
                (StatusReference.NOT_PAYED.getId(), resourceId)).thenReturn(Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<ReportResult> result = medicalCenterDashboardService.execute(Collections.singletonList(reportRequest));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get(0).getContent()).isEqualTo(new BigDecimal(0));

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testExecuteAuthorizationsCountReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setName("authorizationsCount");

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        reportRequest.setFilters(filters);

        UUID resourceId = UUID.randomUUID();

        Tendency tendency = new Tendency();

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);
        when(medCenMedAuthDashboardRepository.countAllByCreatedAtBetween
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId))
                .thenReturn(tendency);
        when(dashboardUtils.parseDates(reportRequest.getFilters())).thenReturn(parsedDates);

        List<ReportResult> result = medicalCenterDashboardService.execute(Collections.singletonList(reportRequest));

        verify(medicalCenterDashboardService, times(1)).authorizationsCount(any(JsonNode.class));
        assertThat((Tendency) result.get(0).getContent()).isEqualTo(tendency);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testExecutePractitionersCountReturnsValidResult() throws ObjectNotFoundException {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setName("practitionersCount");
        reportRequest.setFilters(objectMapper.createObjectNode());

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.getPractitioners().add(new Practitioner());
        medicalCenter.getPractitioners().add(new Practitioner());

        when(medicalCenterService.getAuthMedicalCenter()).thenReturn(medicalCenter);

        List<ReportResult> result = medicalCenterDashboardService.execute(Collections.singletonList(reportRequest));

        verify(medicalCenterDashboardService, times(1)).practitionersCount(reportRequest.getFilters());
        assertThat((long) result.get(0).getContent()).isEqualTo(medicalCenter.getPractitioners().size());
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
        when(medCenMedAuthDashboardRepository.countAllByCreatedAtBetweenGroupedByStatus
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(expected);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<KeyValueReport> result = medicalCenterDashboardService.authorizationsCountGroupedByStatus(filters);

        assertThat(result).isEqualTo(expected);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testAuthorizationsCountGroupedByPractitionersReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        UUID resourceId = UUID.randomUUID();

        List<KeyValueReport> expected = mock(List.class);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(medCenMedAuthDashboardRepository.authorizationsCountGroupedByPractitioners
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(expected);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<KeyValueReport> result = medicalCenterDashboardService.authorizationsCountGroupedByPractitioners(filters);

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
        when(medCenMedAuthDashboardRepository.authorizationsGraphGroupedByGender
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(points);
        when(medCenMedAuthDashboardRepository.countAllByCreatedAtBetweenGroupedByGender
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId)).thenReturn(tendencies);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        FilteredTendencyGraph result = medicalCenterDashboardService.authorizationsCountGroupedByGender(filters);

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
        when(medCenMedAuthDashboardRepository.authorizationsGraphGroupedByAge
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(rawResult1);
        when(medCenMedAuthDashboardRepository.countAllByCreatedAtBetweenGroupedByAge
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), resourceId)).thenReturn(rawResult2);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        FilteredTendencyGraph result = medicalCenterDashboardService.authorizationsCountGroupedByAge(filters);

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
        when(medCenMedAuthDashboardRepository.authorizationsGraphGroupedByCity
                (parsedDates.get(0), parsedDates.get(1), resourceId, PageRequest.of(0, filters.get(AMOUNT_FILTER).asInt()))).thenReturn(points);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<XYPoint> result = medicalCenterDashboardService.authorizationsCountGroupedByCity(filters);

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
        when(medCenMedAuthDashboardRepository.authorizationsGraphGroupedBySpecialty
                (parsedDates.get(0), parsedDates.get(1), specialtyId, resourceId)).thenReturn(points);
        when(dashboardUtils.buildReducedXYPointList(points, filters.get(AMOUNT_FILTER).asInt())).thenReturn(points);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<XYPoint> result = medicalCenterDashboardService.authorizationsCountGroupedBySpecialty(filters);

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
        when(medCenMedAuthDashboardRepository.authorizationsGraphGroupedByDate
                (parsedDates.get(0), parsedDates.get(1), resourceId)).thenReturn(points);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<XYPoint> result = medicalCenterDashboardService.authorizationsCountGroupedByDate(filters);

        assertThat(result).isEqualTo(points);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testPractitionerSettlementsRankingReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contractSet = new HashSet<>();
        contractSet.add(new Contract());

        String tendencyKey = "Practitioner";
        Tendency tendency = new Tendency(tendencyKey, new BigDecimal("1234.5"), new BigDecimal("32123.5"));
        List<Tendency> tendencies = Collections.singletonList(tendency);

        when(contractMediator.findAllAuthMedicalCenterContracts()).thenReturn(contractSet);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.practitionerSettlementsRankingContracts
                (parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        contractSet,
                        PageRequest.of(0,
                                filters.get(AMOUNT_FILTER).asInt()))).thenReturn(tendencies);

        FilteredTendency result = medicalCenterDashboardService.practitionerSettlementsRanking(filters);

        assertThat(result.getTendencies().get(tendencyKey)).isEqualTo(tendency);
    }

    @Test
    public void testSettlementsSumGroupedByDateReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contractSet = new HashSet<>();
        contractSet.add(new Contract());

        XYPoint xyPoint = new XYPoint("10/10/2019", new BigDecimal("12312.5"));
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(contractMediator.findAllAuthMedicalCenterContracts()).thenReturn(contractSet);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsGraphGroupedByDateContracts
                (parsedDates.get(0), parsedDates.get(1), contractSet)).thenReturn(points);

        List<XYPoint> result = medicalCenterDashboardService.settlementsSumGroupedByDate(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testSettlementsSumReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contractSet = new HashSet<>();
        contractSet.add(new Contract());

        Tendency tendency = new Tendency(new BigDecimal("12312.45"), new BigDecimal("1212212.34"));

        when(contractMediator.findAllAuthMedicalCenterContracts()).thenReturn(contractSet);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsAcumGroupedByDateContracts
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), contractSet)).thenReturn(tendency);

        Tendency result = medicalCenterDashboardService.settlementsSum(filters);

        assertThat(result).isEqualTo(tendency);
    }

    @Test
    public void testSettlementsClosedCountReturnsValidResult() {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contractSet = new HashSet<>();
        contractSet.add(new Contract());

        List<KeyValueReport> expected = Collections.emptyList();

        when(contractMediator.findAllAuthMedicalCenterContracts()).thenReturn(contractSet);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsClosedAtBetweenContractsGroupedByStatus
                (parsedDates.get(0), parsedDates.get(1), contractSet)).thenReturn(expected);

        List<KeyValueReport> result = medicalCenterDashboardService.settlementsGroupedByStatus(filters);

        assertThat(result).isEqualTo(expected);
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

        when(practitionerBudgetDashboardRepository.findAllByStatusIdAndMedicalCenterResourceId
                (StatusReference.NOT_PAYED.getId(), resourceId)).thenReturn(budgets);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        BigDecimal result = medicalCenterDashboardService.budgetSum(objectMapper.createObjectNode());

        assertThat(result).isEqualTo(practitionerBudget.getTotal().add(practitionerBudget1.getTotal()));

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testBudgetSumGroupedByPractitionersReturnsValidResult() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        KeyValueReport keyValueReport = new KeyValueReport(1L, new BigDecimal("12312.45"));
        List<KeyValueReport> keyValueReports = Collections.singletonList(keyValueReport);

        UUID resourceId = UUID.randomUUID();

        when(practitionerBudgetDashboardRepository.budgetSumGroupedByPractitioners
                (StatusReference.NOT_PAYED.getId(), resourceId)).thenReturn(keyValueReports);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);

        List<KeyValueReport> result = medicalCenterDashboardService.budgetSumGroupedByPractitioners(objectMapper.createObjectNode());

        assertThat(result).isEqualTo(keyValueReports);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testPractitionersCountReturnsZeroOnError() throws ObjectNotFoundException {
        when(medicalCenterService.getAuthMedicalCenter()).thenThrow(new ObjectNotFoundException(""));

        long result = medicalCenterDashboardService.practitionersCount(objectMapper.createObjectNode());

        assertThat(result).isEqualTo(0);
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
