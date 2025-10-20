package com.capacidad.validationapi.module.premedicalauthorization.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class PrePopulatedPreMedicalAuthorizationItemDTO extends BaseDTO<Long> {


    @NotEmpty
    private String nomenclatorCode;

    @PositiveOrZero
    private BigDecimal chargeUnitPrice;

    @Positive
    private Integer quantity;

}
