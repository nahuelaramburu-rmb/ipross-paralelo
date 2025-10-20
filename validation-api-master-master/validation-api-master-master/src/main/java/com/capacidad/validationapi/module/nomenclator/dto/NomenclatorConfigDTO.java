package com.capacidad.validationapi.module.nomenclator.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class NomenclatorConfigDTO extends BaseDTO<Long> {

    @NotNull
    private Boolean reportRequired;

    @NotNull
    private Long expirationDays;

    @NotNull
    private Integer maxInTransaction;

}
