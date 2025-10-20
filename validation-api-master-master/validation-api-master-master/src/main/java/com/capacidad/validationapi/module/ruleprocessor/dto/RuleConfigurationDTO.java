package com.capacidad.validationapi.module.ruleprocessor.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class RuleConfigurationDTO extends BaseDTO<Long> {

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> ruleRef;

    @NotNull
    private Boolean active;

    @NotNull
    private Boolean applyToBatch;

    private IdDTO<Long> contract;

    private JsonNode data;

    @Valid
    private IdDTO<Long> restrictionType;

}
