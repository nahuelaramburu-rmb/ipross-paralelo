package com.capacidad.validationapi.module.contract.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.general.model.Period;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class ContractAdjustmentDTO extends BaseDTO<Long> {

    @Valid
    private IdDTO<Long> region;

    @Valid
    private IdDTO<Long> city;

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> nomenclator;

    @NotNull
    private Period period;

    @Valid
    private IdDTO<Long> restrictionType;

    @NotNull
    private ContractAdjustmentScope scope;

}
