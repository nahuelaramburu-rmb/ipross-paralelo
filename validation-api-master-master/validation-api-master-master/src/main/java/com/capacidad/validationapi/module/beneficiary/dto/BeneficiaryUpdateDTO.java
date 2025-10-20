package com.capacidad.validationapi.module.beneficiary.dto;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryUpdateDTO extends BeneficiaryDTO {

    @Valid
    private IdDTO<Long> relationshipType;

}
