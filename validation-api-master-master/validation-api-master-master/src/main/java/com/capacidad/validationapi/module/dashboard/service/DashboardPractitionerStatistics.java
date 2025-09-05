package com.capacidad.validationapi.module.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface DashboardPractitionerStatistics {

    long practitionersCount(JsonNode params);

}
