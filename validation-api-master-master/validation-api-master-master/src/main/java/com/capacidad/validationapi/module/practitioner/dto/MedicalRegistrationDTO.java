package com.capacidad.validationapi.module.practitioner.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class MedicalRegistrationDTO extends BaseDTO<Long> {

    private String registrationCode;

    @NotNull
    @Valid
    private IdDTO<Long> organization;

}
