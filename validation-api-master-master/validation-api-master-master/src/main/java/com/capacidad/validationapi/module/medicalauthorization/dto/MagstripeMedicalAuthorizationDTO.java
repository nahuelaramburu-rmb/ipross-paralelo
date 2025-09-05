package com.capacidad.validationapi.module.medicalauthorization.dto;

import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryCodeDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class MagstripeMedicalAuthorizationDTO extends MedicalAuthorizationDTO {

    @NotNull
    @Valid
    private BeneficiaryCodeDTO beneficiary;

}
