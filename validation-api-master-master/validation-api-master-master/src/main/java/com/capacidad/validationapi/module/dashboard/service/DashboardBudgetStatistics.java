package com.capacidad.validationapi.module.dashboard.service;

import com.capacidad.validationapi.module.dashboard.dto.KeyValueReport;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardBudgetStatistics {

    BigDecimal budgetSum(JsonNode params);

    List<KeyValueReport> budgetSumGroupedByPractitioners(JsonNode params);

}
