package com.capacidad.validationapi.module.organization.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.location.dto.AddressDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class OrganizationDTO extends BaseDTO<Long> {

    @NotBlank
    private String name;

    @NotNull
    private AddressDTO address;

    @Valid
    private IdDTO<Long> region;

}
