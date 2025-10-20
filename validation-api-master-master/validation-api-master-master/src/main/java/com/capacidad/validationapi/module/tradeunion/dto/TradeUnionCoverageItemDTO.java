package com.capacidad.validationapi.module.tradeunion.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.general.model.Period;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class TradeUnionCoverageItemDTO extends BaseDTO<Long> {

    @NotNull
    private Period period;

    @NotNull
    @Positive
    private Integer quantity;

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> nomenclator;

    @NotNull
    @Positive
    @Max(100)
    private BigDecimal percentage;

}
