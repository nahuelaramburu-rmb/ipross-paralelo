package com.capacidad.validationapi.module.procedure.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.procedure.model.ProcedureResolution;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class ProcedureResolutionDTO extends BaseDTO<Long> {

    @NotNull
    private ProcedureResolution resolution;

    @FutureOrPresent
    private LocalDate expiration;

    private String reason;

}
