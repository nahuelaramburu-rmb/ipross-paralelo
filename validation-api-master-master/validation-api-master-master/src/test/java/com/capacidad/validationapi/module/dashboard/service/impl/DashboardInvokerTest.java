package com.capacidad.validationapi.module.dashboard.service.impl;

import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.dashboard.dto.ReportRequest;
import com.capacidad.validationapi.module.dashboard.dto.ReportResult;
import com.capacidad.validationapi.module.dashboard.service.DashboardCommand;
import com.capacidad.validationapi.module.medicalauthorization.service.impl.HighRankingMedicalAuthorizationDashboardServiceImpl;
import com.capacidad.validationapi.module.medicalcenter.service.impl.MedicalCenterDashboardServiceImpl;
import com.capacidad.validationapi.module.organization.service.impl.OrganizationDashboardServiceImpl;
import com.capacidad.validationapi.module.practitioner.service.impl.PractitionerDashboardServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DashboardInvokerTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private DashboardCommand dashboardCommand;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @InjectMocks
    private DashboardInvoker dashboardInvoker;

    @Test
    public void testExecuteReturnsEmptyWhenNoExecutorFound() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_BENEFICIARY_INSTANCE));

        List<ReportResult> result = dashboardInvoker.execute(new ArrayList<>());

        assertThat(result).isEmpty();

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testExecuteReturnsValueWhenHighRankingDashboardCommand() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        when(applicationContext.getBean(StringUtils.uncapitalize(HighRankingMedicalAuthorizationDashboardServiceImpl.class.getSimpleName()))).thenReturn(dashboardCommand);

        List<ReportRequest> reportRequests = mock(List.class);

        dashboardInvoker.execute(reportRequests);

        verify(dashboardCommand, times(1)).execute(reportRequests);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testExecuteReturnsValueWhenOrganizationDashboardCommand() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ORGANIZATION_INSTANCE));
        when(applicationContext.getBean(StringUtils.uncapitalize(OrganizationDashboardServiceImpl.class.getSimpleName()))).thenReturn(dashboardCommand);

        List<ReportRequest> reportRequests = mock(List.class);

        dashboardInvoker.execute(reportRequests);

        verify(dashboardCommand, times(1)).execute(reportRequests);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testExecuteReturnsValueWhenMedicalCenterDashboardCommand() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE));
        when(applicationContext.getBean(StringUtils.uncapitalize(MedicalCenterDashboardServiceImpl.class.getSimpleName()))).thenReturn(dashboardCommand);

        List<ReportRequest> reportRequests = mock(List.class);

        dashboardInvoker.execute(reportRequests);

        verify(dashboardCommand, times(1)).execute(reportRequests);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testExecuteReturnsValueWhenPractitionerDashboardCommand() {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_PRACTITIONER_INSTANCE));
        when(applicationContext.getBean(StringUtils.uncapitalize(PractitionerDashboardServiceImpl.class.getSimpleName()))).thenReturn(dashboardCommand);

        List<ReportRequest> reportRequests = mock(List.class);

        dashboardInvoker.execute(reportRequests);

        verify(dashboardCommand, times(1)).execute(reportRequests);

        SecurityContextHolder.setContext(defaultContext);
    }

}
