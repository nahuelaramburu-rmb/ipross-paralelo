package com.capacidad.validationapi.module.nomenclator.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class NomenclatorDTO extends BaseDTO<Long> {

    @NotNull
    @Positive
    private Long nomenclatorCode;

    @NotNull
    @Valid
    private IdDTO<Long> medicalPractice;

    @NotNull
    @Valid
    private IdDTO<Long> medicalPracticeArea;

    @NotNull
    @Valid
    private IdDTO<Long> medicalPracticeType;

    @NotNull
    @Valid
    private NomenclatorConfigDTO nomenclatorConfig;

}
