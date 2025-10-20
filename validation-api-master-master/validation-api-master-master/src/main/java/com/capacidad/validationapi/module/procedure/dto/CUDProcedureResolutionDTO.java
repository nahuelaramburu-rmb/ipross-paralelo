package com.capacidad.validationapi.module.procedure.dto;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class CUDProcedureResolutionDTO extends ProcedureResolutionDTO {
    @Valid
    private Set<IdDTO<Long>> diagnosis;
}
