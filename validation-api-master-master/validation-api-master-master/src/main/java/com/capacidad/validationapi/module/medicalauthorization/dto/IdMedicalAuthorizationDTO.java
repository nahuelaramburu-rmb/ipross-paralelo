package com.capacidad.validationapi.module.medicalauthorization.dto;

import com.capacidad.validationapi.module.beneficiary.dto.IdBeneficiaryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class IdMedicalAuthorizationDTO extends MedicalAuthorizationDTO {

    @NotNull
    @Valid
    private IdBeneficiaryDTO beneficiary;

}
