package com.capacidad.validationapi.module.general.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class StatusUpdateDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> status;

    @NotBlank
    @Size(min = 1, max = 200)
    private String statusUpdateDescription;

}
