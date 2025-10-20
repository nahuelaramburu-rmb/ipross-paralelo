package com.capacidad.validationapi.module.general.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class NameDTO extends BaseDTO<Long> {

    @NotBlank
    @Size(max = 50)
    private String name;

}
