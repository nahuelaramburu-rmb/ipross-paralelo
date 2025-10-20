package com.capacidad.validationapi.module.contract.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class MonetaryAdjustmentDTO extends ContractAdjustmentDTO {

    @NotNull
    private BigDecimal threshold;

}
