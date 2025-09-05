package com.capacidad.validationapi.module.medicalcoverage.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.person.model.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class MedicalCoverageItemDTO extends BaseDTO<Long> {

    @Valid
    private IdDTO<Long> restrictionType;

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> nomenclator;

    @NotNull
    private Gender gender;

    @PositiveOrZero
    private Integer ageFrom;

    @Positive
    private Integer ageTo;

    @NotNull
    @PositiveOrZero
    private Integer awaitDays;

    @PositiveOrZero
    private Integer fixedMaxQuantity;

    @Positive
    private Integer fixedMaxDays;

    @PositiveOrZero
    private Integer freeMaxQuantity;

    @PositiveOrZero
    private Integer freeMaxDays;

    @NotNull
    private Boolean auditRequired;

    @NotNull
    @Valid
    private IdDTO<Long> chargeType;

    @NotNull
    private BigDecimal chargeValue;

}
