package com.capacidad.validationapi.module.insuranceplan.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class InsurancePlanDTO extends BaseDTO<Long> {

    @NotBlank
    private String name;

    @NotNull
    @Valid
    private IdDTO<Long> insurancePlanType;

    @NotNull
    @Positive
    private Integer priority;

}
