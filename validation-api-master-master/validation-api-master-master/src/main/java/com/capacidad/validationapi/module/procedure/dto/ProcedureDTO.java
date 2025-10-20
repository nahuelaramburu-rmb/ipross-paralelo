package com.capacidad.validationapi.module.procedure.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class ProcedureDTO extends BaseDTO<Long> {

    @Size(max = 1000)
    @Immutable
    private String description;

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> beneficiary;

}
