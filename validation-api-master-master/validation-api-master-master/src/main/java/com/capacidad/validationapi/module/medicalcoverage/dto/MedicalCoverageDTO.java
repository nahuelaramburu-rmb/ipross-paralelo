package com.capacidad.validationapi.module.medicalcoverage.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class MedicalCoverageDTO extends BaseDTO<Long> {

    @NotEmpty
    private String name;

    @NotNull
    @Valid
    private IdDTO<Long> medicalPracticeArea;

    @Valid
    private IdDTO<Long> region;

    @Valid
    private IdDTO<Long> city;

}
