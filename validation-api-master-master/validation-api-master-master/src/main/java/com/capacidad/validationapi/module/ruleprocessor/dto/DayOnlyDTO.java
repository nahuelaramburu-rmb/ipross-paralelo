package com.capacidad.validationapi.module.ruleprocessor.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class DayOnlyDTO implements RuleProperty {

    @NotNull
    @Positive
    private Integer days;

}
