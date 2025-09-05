package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class RoleDTO extends BaseDTO<Long> {

    @NotBlank
    private String name;

}
