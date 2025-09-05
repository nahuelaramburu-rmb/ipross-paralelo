package com.capacidad.validationapi.module.medicalauthorization.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class MedicalAuthorizationDiagnosisDTO extends BaseDTO<Long> {

    @Size(max = 1000)
    private String diagnosis;

    @Valid
    private IdDTO<Long> disease;

}
