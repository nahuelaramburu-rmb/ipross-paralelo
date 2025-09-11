package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class NameDTO extends BaseDTO<Long> {

    @NotEmpty
    private String name;

}
