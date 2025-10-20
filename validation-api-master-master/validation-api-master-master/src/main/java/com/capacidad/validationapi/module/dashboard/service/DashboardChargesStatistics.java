package com.capacidad.validationapi.module.dashboard.service;

import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.capacidad.validationapi.module.dashboard.dto.Tendency;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface DashboardChargesStatistics {

    Tendency paycheckChargesTendency(JsonNode params);

    Tendency voluntaryChargesTendency(JsonNode params);

    List<KeyValueReport> paycheckChargesSumGroupedByCompanies(JsonNode params);

}
