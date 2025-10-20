package com.capacidad.validationapi.module.ruleprocessor.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class MaxAmountOnlyDTO implements RuleProperty {

    @NotNull
    @Positive
    private Long maxAmount;

}
