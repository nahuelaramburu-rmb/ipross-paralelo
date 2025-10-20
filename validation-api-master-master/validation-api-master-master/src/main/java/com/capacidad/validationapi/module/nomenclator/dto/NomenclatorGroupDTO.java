package com.capacidad.validationapi.module.nomenclator.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class NomenclatorGroupDTO extends BaseDTO<Long> {

    @NotBlank
    @Size(min = 1, max = 35)
    private String name;

    @NotBlank
    @Size(min = 1, max = 200)
    private String description;

    @NotEmpty
    @Valid
    private Set<IdDTO<Long>> nomenclators;

}
