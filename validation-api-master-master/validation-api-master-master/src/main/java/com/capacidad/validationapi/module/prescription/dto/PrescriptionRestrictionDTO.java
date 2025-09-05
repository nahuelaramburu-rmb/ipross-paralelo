package com.capacidad.validationapi.module.prescription.dto;

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
public class PrescriptionRestrictionDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> medicalSpecialty;

}
