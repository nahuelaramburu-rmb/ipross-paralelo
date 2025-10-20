package com.capacidad.validationapi.module.procedure.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class FileTagDTO extends BaseDTO<Long> {

    @NotEmpty
    private String filename;

    @NotEmpty
    private String tag;

}
