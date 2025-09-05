package com.capacidad.validationapi.module.beneficiary.dto;

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
public class IdBeneficiaryDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> idType;

    @NotNull
    @Positive
    private Long idNumber;

}
