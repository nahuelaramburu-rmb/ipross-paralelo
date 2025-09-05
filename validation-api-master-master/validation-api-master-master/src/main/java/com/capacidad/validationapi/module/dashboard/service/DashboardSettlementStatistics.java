package com.capacidad.validationapi.module.dashboard.service;

import com.capacidad.validationapi.module.dashboard.dto.FilteredTendency;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface DashboardSettlementStatistics {

    FilteredTendency practitionerSettlementsRanking(JsonNode params);

    List<XYPoint> settlementsSumGroupedByDate(JsonNode params);

    Tendency settlementsSum(JsonNode params);

    List<KeyValueReport> settlementsGroupedByStatus(JsonNode params);

}
