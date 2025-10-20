package com.capacidad.validationapi.module.dashboard.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.dashboard.dto.ReportRequest;
import com.capacidad.validationapi.module.dashboard.dto.ReportResult;
import com.capacidad.validationapi.module.dashboard.service.DashboardCommand;
import com.capacidad.validationapi.module.medicalauthorization.service.impl.HighRankingMedicalAuthorizationDashboardServiceImpl;
import com.capacidad.validationapi.module.medicalcenter.service.impl.MedicalCenterDashboardServiceImpl;
import com.capacidad.validationapi.module.organization.service.impl.OrganizationDashboardServiceImpl;
import com.capacidad.validationapi.module.practitioner.service.impl.PractitionerDashboardServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Log4j2
public class DashboardInvoker {

    private final ApplicationContext applicationContext;

    @Autowired
    public DashboardInvoker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<ReportResult> execute(List<ReportRequest> reportRequest) {
        DashboardCommand dashboardCommand = resolveDashboardCommand();
        if (dashboardCommand == null)
            return Collections.emptyList();
        return dashboardCommand.execute(reportRequest);
    }

    private DashboardCommand resolveDashboardCommand() {
        if (SecurityUtils.isHighRankingAuthority())
            return (DashboardCommand) applicationContext.getBean(StringUtils.uncapitalize(HighRankingMedicalAuthorizationDashboardServiceImpl.class.getSimpleName()));
        if (SecurityUtils.isOrganization())
            return (DashboardCommand) applicationContext.getBean(StringUtils.uncapitalize(OrganizationDashboardServiceImpl.class.getSimpleName()));
        if (SecurityUtils.isMedicalCenter())
            return (DashboardCommand) applicationContext.getBean(StringUtils.uncapitalize(MedicalCenterDashboardServiceImpl.class.getSimpleName()));
        if (SecurityUtils.isPractitioner())
            return (DashboardCommand) applicationContext.getBean(StringUtils.uncapitalize(PractitionerDashboardServiceImpl.class.getSimpleName()));
        return null;
    }

}
