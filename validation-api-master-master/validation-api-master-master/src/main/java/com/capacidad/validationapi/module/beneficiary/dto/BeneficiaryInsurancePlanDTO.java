package com.capacidad.validationapi.module.beneficiary.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryInsurancePlanDTO extends BaseDTO<Long> {

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> insurancePlan;

    @FutureOrPresent
    private LocalDate expirationDate;

    @Positive
    private Integer priority;

}
