package com.capacidad.validationapi.module.organization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.dashboard.dto.*;
import com.capacidad.validationapi.module.dashboard.misc.DashboardUtils;
import com.capacidad.validationapi.module.nomenclator.reference.MedicalSpecialtyReference;
import com.capacidad.validationapi.module.organization.repository.OrganizationMedicalAuthorizationDashboardRepository;
import com.capacidad.validationapi.module.organization.repository.OrganizationSettlementDashboardRepository;
import com.capacidad.validationapi.module.person.model.Gender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
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
import java.util.*;

import static com.capacidad.validationapi.module.dashboard.misc.DashboardConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationDashboardServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID resourceId = UUID.randomUUID();

    @Mock
    private DashboardUtils dashboardUtils;

    @Mock
    private OrganizationMedicalAuthorizationDashboardRepository organizationMedicalAuthorizationDashboardRepository;

    @Mock
    private ContractMediator contractMediator;

    @Mock
    private OrganizationSettlementDashboardRepository settlementDashboardRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @InjectMocks
    private OrganizationDashboardServiceImpl organizationDashboardService;

    @Before
    public void init() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(resourceId);
    }

    @After
    public void destroy() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testAuthorizationsCountReturnsEmptyTendencyWhenNoContracts() throws ObjectNotFoundException {
        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenThrow(new ObjectNotFoundException(""));
        when(organizationMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetween
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), Collections.emptySet(), resourceId))
                .thenReturn(new Tendency());

        Tendency result = organizationDashboardService.authorizationsCount(filters);

        assertThat(result.getCurrentValue()).isNull();
        assertThat(result.getPreviousValue()).isNull();
    }

    @Test
    public void testAuthorizationsCountReturnsValidTendencyWhenContracts() throws ObjectNotFoundException {
        ObjectNode filters = buildFilters();
        List<LocalDateTime> parsedDates = parseDates();

        Set<Contract> contracts = Collections.singleton(new Contract());

        Tendency tendency = new Tendency(500L, 400L);

        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(organizationMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetween
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), contracts, resourceId))
                .thenReturn(tendency);

        Tendency result = organizationDashboardService.authorizationsCount(filters);

        assertThat(result).isEqualTo(tendency);
    }

    @Test
    public void testAuthorizationsCountGroupedByStatusReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        Set<Contract> contracts = Collections.singleton(new Contract());

        ObjectNode filters = buildFilters();

        List<KeyValueReport> expected = mock(List.class);

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(organizationMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetweenGroupedByStatus
                (parsedDates.get(0), parsedDates.get(1), contracts, resourceId)).thenReturn(expected);

        List<KeyValueReport> result = organizationDashboardService.authorizationsCountGroupedByStatus(filters);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testAuthorizationsCountGroupedByGenderReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        Set<Contract> contracts = Collections.singleton(new Contract());

        String genderKey = Gender.MASCULINO.name();

        XYPoint xyPoint = new XYPoint(genderKey, "10/10/1019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        Tendency tendency = new Tendency(genderKey, 10L, 5L);
        List<Tendency> tendencies = Collections.singletonList(tendency);

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(organizationMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByGender
                (parsedDates.get(0), parsedDates.get(1), contracts, resourceId)).thenReturn(points);
        when(organizationMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetweenGroupedByGender
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), contracts, resourceId)).thenReturn(tendencies);

        FilteredTendencyGraph result = organizationDashboardService.authorizationsCountGroupedByGender(filters);

        assertThat(result.getKeys()).contains(genderKey);
        assertThat(result.getTendencies().get(genderKey)).isEqualTo(tendency);
        assertThat(result.getGraphs().get(genderKey)).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedByAgeReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();

        Set<Contract> contracts = Collections.singleton(new Contract());

        String ageKey = "10-19";

        XYPoint xyPoint = new XYPoint(ageKey, "10/10/1019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        Tendency tendency = new Tendency(ageKey, 10L, 5L);
        List<Tendency> tendencies = Collections.singletonList(tendency);

        List<Tuple> rawResult1 = mock(List.class);
        List<Tuple> rawResult2 = mock(List.class);

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(dashboardUtils.buildXYPointListFromTuple(rawResult1)).thenReturn(points);
        when(dashboardUtils.buildTendencyListFromTuple(rawResult2)).thenReturn(tendencies);
        when(organizationMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByAge
                (parsedDates.get(0), parsedDates.get(1), contracts, resourceId)).thenReturn(rawResult1);
        when(organizationMedicalAuthorizationDashboardRepository.countAllByCreatedAtBetweenGroupedByAge
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), contracts, resourceId)).thenReturn(rawResult2);

        FilteredTendencyGraph result = organizationDashboardService.authorizationsCountGroupedByAge(filters);

        assertThat(result.getKeys()).contains(ageKey);
        assertThat(result.getTendencies().get(ageKey)).isEqualTo(tendency);
        assertThat(result.getGraphs().get(ageKey)).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedByCityReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contracts = Collections.singleton(new Contract());

        XYPoint xyPoint = new XYPoint("City", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);
        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(organizationMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByCity
                (parsedDates.get(0), parsedDates.get(1), contracts, resourceId, PageRequest.of(0, filters.get(AMOUNT_FILTER).asInt()))).thenReturn(points);

        List<XYPoint> result = organizationDashboardService.authorizationsCountGroupedByCity(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedBySpecialtyReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contracts = Collections.singleton(new Contract());

        XYPoint xyPoint = new XYPoint("Specialty", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        long specialtyId = MedicalSpecialtyReference.ALL.getId();

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(dashboardUtils.parseSpecialtyIdOrDefault(filters)).thenReturn(specialtyId);
        when(organizationMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedBySpecialty
                (parsedDates.get(0), parsedDates.get(1), specialtyId, contracts, resourceId)).thenReturn(points);
        when(dashboardUtils.buildReducedXYPointList(points, filters.get(AMOUNT_FILTER).asInt())).thenReturn(points);

        List<XYPoint> result = organizationDashboardService.authorizationsCountGroupedBySpecialty(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testAuthorizationsCountGroupedByDateReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contracts = Collections.singleton(new Contract());

        XYPoint xyPoint = new XYPoint("10/10/2019", 5);
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(organizationMedicalAuthorizationDashboardRepository.authorizationsGraphGroupedByDate
                (parsedDates.get(0), parsedDates.get(1), contracts, resourceId)).thenReturn(points);

        List<XYPoint> result = organizationDashboardService.authorizationsCountGroupedByDate(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testPractitionerSettlementsRankingReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contracts = Collections.singleton(new Contract());

        String tendencyKey = "Practitioner";
        Tendency tendency = new Tendency(tendencyKey, new BigDecimal("1234.5"), new BigDecimal("32123.5"));
        List<Tendency> tendencies = Collections.singletonList(tendency);

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contracts);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.practitionerSettlementsRankingContracts
                (parsedDates.get(0),
                        parsedDates.get(1),
                        parsedDates.get(2),
                        parsedDates.get(3),
                        contracts,
                        resourceId,
                        PageRequest.of(
                                0,
                                filters.get(AMOUNT_FILTER).asInt()))).thenReturn(tendencies);

        FilteredTendency result = organizationDashboardService.practitionerSettlementsRanking(filters);

        assertThat(result.getTendencies().get(tendencyKey)).isEqualTo(tendency);
    }

    @Test
    public void testSettlementsSumGroupedByDateReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contractSet = Collections.singleton(new Contract());

        XYPoint xyPoint = new XYPoint("10/10/2019", new BigDecimal("12312.5"));
        List<XYPoint> points = Collections.singletonList(xyPoint);

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contractSet);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsGraphGroupedByDateContracts
                (parsedDates.get(0), parsedDates.get(1), contractSet, resourceId)).thenReturn(points);

        List<XYPoint> result = organizationDashboardService.settlementsSumGroupedByDate(filters);

        assertThat(result).isEqualTo(points);
    }

    @Test
    public void testSettlementsSumReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contractSet = Collections.singleton(new Contract());

        Tendency tendency = new Tendency(new BigDecimal("12312.45"), new BigDecimal("1212212.34"));

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contractSet);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsAcumGroupedByDateContracts
                (parsedDates.get(0), parsedDates.get(1), parsedDates.get(2), parsedDates.get(3), contractSet, resourceId)).thenReturn(tendency);

        Tendency result = organizationDashboardService.settlementsSum(filters);

        assertThat(result).isEqualTo(tendency);
    }

    @Test
    public void testSettlementsClosedCountReturnsValidResult() throws ObjectNotFoundException {
        List<LocalDateTime> parsedDates = parseDates();

        ObjectNode filters = buildFilters();
        filters.put(AMOUNT_FILTER, 10);

        Set<Contract> contractSet = Collections.singleton(new Contract());

        List<KeyValueReport> expected = Collections.emptyList();

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contractSet);
        when(dashboardUtils.parseDates(filters)).thenReturn(parsedDates);
        when(settlementDashboardRepository.settlementsClosedAtBetweenContractsGroupedByStatus
                (parsedDates.get(0), parsedDates.get(1), contractSet, resourceId)).thenReturn(expected);

        List<KeyValueReport> result = organizationDashboardService.settlementsGroupedByStatus(filters);

        assertThat(result).isEqualTo(expected);
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
