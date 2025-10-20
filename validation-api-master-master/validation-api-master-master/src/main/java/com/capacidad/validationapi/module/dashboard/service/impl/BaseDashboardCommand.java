package com.capacidad.validationapi.module.dashboard.service.impl;

import com.capacidad.validationapi.module.dashboard.dto.ReportRequest;
import com.capacidad.validationapi.module.dashboard.dto.ReportResult;
import com.capacidad.validationapi.module.dashboard.service.DashboardCommand;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public abstract class BaseDashboardCommand implements DashboardCommand {

    @Override
    public List<ReportResult> execute(List<ReportRequest> reportRequests) {
        List<ReportResult> results = new ArrayList<>();
        for (ReportRequest report : reportRequests) {
            execute(report).ifPresent(obj -> {
                ReportResult reportResult = new ReportResult();
                reportResult.setContent(obj);
                reportResult.setName(report.getName());
                reportResult.setFilters(report.getFilters());
                results.add(reportResult);
            });
        }
        return results;
    }

    private Optional<Object> execute(ReportRequest reportRequest) {
        try {
            return invoke(reportRequest);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.error("Error trying to execute Dashboard Command ({}): {}", reportRequest.getName(), e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<Object> invoke(ReportRequest reportRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = this.getClass().getMethod(reportRequest.getName(), JsonNode.class);
        return Optional.ofNullable(method.invoke(this, reportRequest.getFilters()));
    }

}
