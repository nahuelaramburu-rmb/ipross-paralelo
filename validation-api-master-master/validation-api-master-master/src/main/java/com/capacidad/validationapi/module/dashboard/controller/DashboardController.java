package com.capacidad.validationapi.module.dashboard.controller;

import com.capacidad.validationapi.module.dashboard.dto.ReportRequest;
import com.capacidad.validationapi.module.dashboard.dto.ReportResult;
import com.capacidad.validationapi.module.dashboard.service.impl.DashboardInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_DASHBOARDS;

@RestController
@RequestMapping(value = ENDPOINT_DASHBOARDS)
public class DashboardController {

    private final DashboardInvoker dashboardInvoker;

    @Autowired
    public DashboardController(DashboardInvoker dashboardInvoker) {
        this.dashboardInvoker = dashboardInvoker;
    }

    @PostMapping
    public ResponseEntity<List<ReportResult>> renderReports(@RequestBody @NotEmpty @Valid List<ReportRequest> reports) {
        return ResponseEntity.ok(dashboardInvoker.execute(reports));
    }

}
