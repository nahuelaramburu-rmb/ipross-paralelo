package com.capacidad.validationapi.module.ruleprocessor.dto;

import com.capacidad.validationapi.module.base.dto.IdAndNameDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class MaxAmountRegionAndNomenclatorSetDTO implements RuleProperty {

    @NotNull
    @Positive
    private Long maxAmount;

    @NotNull
    @PositiveOrZero
    private Long maxUnsecured;

    @Valid
    private IdAndNameDTO<Long> region;

    @Valid
    private Set<IdAndNameDTO<Long>> nomenclators = new HashSet<>();


}
