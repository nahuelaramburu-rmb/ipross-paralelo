package com.capacidad.validationapi.module.practitioner.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;

@Setter
@Getter
@NoArgsConstructor
public class MedicalSpecialtyDTO extends BaseDTO<Long> {
    @Valid
    public IdDTO<Long> id;
}
