package com.capacidad.validationapi.module.contract.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class ContractItemDTO extends BaseDTO<Long> {

    @NotNull
    @Positive
    private BigDecimal value;

    @NotNull
    private Boolean refundable;

    @Valid
    private IdDTO<Long> practitionerCategory;

}
