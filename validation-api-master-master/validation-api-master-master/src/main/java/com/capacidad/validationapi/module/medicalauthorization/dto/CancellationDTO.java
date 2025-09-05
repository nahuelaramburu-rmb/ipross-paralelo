package com.capacidad.validationapi.module.medicalauthorization.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class CancellationDTO extends BaseDTO<Long> {

    @NotBlank
    @Size(min = 1, max = 200)
    private String cancellationReason;

}
