package com.capacidad.validationapi.module.premedicalauthorization.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class PreMedicalAuthorizationCodeDTO extends BaseDTO<Long> {

    @NotEmpty
    private String code;

}
