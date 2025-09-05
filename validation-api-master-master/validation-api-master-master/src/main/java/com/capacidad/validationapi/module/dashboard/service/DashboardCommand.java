package com.capacidad.validationapi.module.dashboard.service;

import com.capacidad.validationapi.module.dashboard.dto.ReportRequest;
import com.capacidad.validationapi.module.dashboard.dto.ReportResult;

import java.util.List;

public interface DashboardCommand {

    List<ReportResult> execute(List<ReportRequest> reportRequests);

}
