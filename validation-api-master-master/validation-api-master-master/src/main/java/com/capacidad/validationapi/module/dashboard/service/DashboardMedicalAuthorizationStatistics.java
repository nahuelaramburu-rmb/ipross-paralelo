package com.capacidad.validationapi.module.dashboard.service;

import com.capacidad.validationapi.module.dashboard.dto.FilteredTendencyGraph;
import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.capacidad.validationapi.module.dashboard.dto.XYPoint;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface DashboardMedicalAuthorizationStatistics {

    Tendency authorizationsCount(JsonNode params);

    List<KeyValueReport> authorizationsCountGroupedByStatus(JsonNode params);

    List<KeyValueReport> authorizationsCountGroupedByPractitioners(JsonNode params);

    FilteredTendencyGraph authorizationsCountGroupedByGender(JsonNode params);

    FilteredTendencyGraph authorizationsCountGroupedByAge(JsonNode params);

    List<XYPoint> authorizationsCountGroupedByCity(JsonNode params);

    List<XYPoint> authorizationsCountGroupedBySpecialty(JsonNode params);

    List<XYPoint> authorizationsCountGroupedByDate(JsonNode params);

}
