package com.capacidad.validationapi.module.procedure.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class MessageDTO extends BaseDTO<Long> {

    @Size(max = 1000)
    @NotEmpty
    private String text;

}
