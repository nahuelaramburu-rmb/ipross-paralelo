package com.capacidad.validationapi.module.settlement.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class SettlementDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> practitioner;

    @NotNull
    @Valid
    private IdDTO<Long> contract;

}