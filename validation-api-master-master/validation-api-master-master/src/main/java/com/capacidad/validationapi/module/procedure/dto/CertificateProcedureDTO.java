package com.capacidad.validationapi.module.procedure.dto;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class CertificateProcedureDTO extends ProcedureDTO {

    @NotNull
    @Valid
    private IdDTO<Long> certificateType;

}
