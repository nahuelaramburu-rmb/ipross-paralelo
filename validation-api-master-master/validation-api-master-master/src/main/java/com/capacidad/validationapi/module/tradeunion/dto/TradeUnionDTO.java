package com.capacidad.validationapi.module.tradeunion.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.location.dto.AddressDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class TradeUnionDTO extends BaseDTO<Long> {

    @NotBlank
    @Size(min = 1, max = 255)
    private String name;

    @NotNull
    @Valid
    private AddressDTO address;

    @NotNull
    private Boolean includesFamilyGroup = false;

    @NotNull
    private Boolean autoSettlement = false;

    @NotNull
    @Range(min = 1, max = 31)
    private Integer dayOfSettlement;

}
