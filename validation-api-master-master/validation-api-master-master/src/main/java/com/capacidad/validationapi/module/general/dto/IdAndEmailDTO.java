package com.capacidad.validationapi.module.general.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class IdAndEmailDTO extends BaseDTO<Long> {

    @Positive
    @NotNull
    private Long idNumber;

    @NotBlank
    private String idType;

    @NotBlank
    private String email;

}
