package com.capacidad.validationapi.module.person.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.person.model.PhoneType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class PhoneDTO extends BaseDTO<Long> {


    @Positive
    private Integer countryCode;

    @Positive
    private Integer areaCode;

    @NotNull
    @Positive
    private Long phoneNumber;

    private PhoneType phoneType;

}
