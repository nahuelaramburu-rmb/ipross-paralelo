package com.capacidad.validationapi.module.location.dto;

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
public class AddressDTO extends BaseDTO<Long> {

    private String district;

    private String street;

    @Positive
    private Integer streetNumber;

    private String apartment;

    @NotNull
    @Valid
    private IdDTO<Long> city;

}
