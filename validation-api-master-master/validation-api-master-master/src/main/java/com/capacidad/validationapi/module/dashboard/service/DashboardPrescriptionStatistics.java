package com.capacidad.validationapi.module.dashboard.service;

import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface DashboardPrescriptionStatistics {

    Tendency prescriptionsCount(JsonNode params);

    List<XYPoint> prescriptionsCountGroupedByDate(JsonNode params);

    List<KeyValueReport> prescriptionsCountGroupedByStatus(JsonNode params);

}
