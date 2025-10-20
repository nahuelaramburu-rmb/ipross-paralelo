package com.capacidad.validationapi.module.nomenclator.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class MedicalPracticeDTO extends BaseDTO<Long> {

    @Size(max = 500)
    @NotBlank
    private String name;

    @NotEmpty
    @Valid
    private Set<IdDTO<Long>> medicalSpecialties;

}
