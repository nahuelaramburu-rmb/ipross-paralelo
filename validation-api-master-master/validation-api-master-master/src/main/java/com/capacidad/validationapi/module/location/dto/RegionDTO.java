package com.capacidad.validationapi.module.location.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class RegionDTO extends BaseDTO<Long> {

    @NotEmpty
    private String name;

    @NotEmpty
    @Valid
    private Set<IdDTO<Long>> cities;

}
