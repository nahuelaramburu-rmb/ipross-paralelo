package com.capacidad.validationapi.module.premedicalauthorization.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class PreMedicalAuthorizationItemDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> nomenclator;

    @PositiveOrZero
    private BigDecimal chargeUnitPrice;

    @Positive
    private Integer quantity;

}
