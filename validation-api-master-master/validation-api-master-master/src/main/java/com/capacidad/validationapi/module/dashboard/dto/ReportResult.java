package com.capacidad.validationapi.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ReportResult {

    private String name;
    private Object content;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JsonNode filters;

}
