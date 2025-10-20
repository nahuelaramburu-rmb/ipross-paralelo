package com.capacidad.validationapi.module.medicalauthorization.dto;

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
public class OTPMedicalAuthorizationDTO extends MedicalAuthorizationDTO {

    @NotEmpty
    private String otp;

    @NotNull
    @Valid
    private IdDTO<Long> beneficiary;

}
