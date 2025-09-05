package com.capacidad.validationapi.module.contract.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class MaximumAdjustmentDTO extends ContractAdjustmentDTO {

    @NotNull
    private Long threshold;

}
