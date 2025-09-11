package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.capacidad.identityservice.model.dto.base.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class UpdateApplicationUserContextDTO extends BaseDTO<Long> {

    @Valid
    private UpdateApplicationUserDTO user;

    @Valid
    private IdDTO<Long> permissionSuggestion;

    @Valid
    private Set<IdDTO<Long>> permissionGroups;

}
