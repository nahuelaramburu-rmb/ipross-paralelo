package com.capacidad.validationapi.module.dashboard.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class ReportRequest {

    @NotEmpty
    private String name;

    @NotNull
    private JsonNode filters;

}
