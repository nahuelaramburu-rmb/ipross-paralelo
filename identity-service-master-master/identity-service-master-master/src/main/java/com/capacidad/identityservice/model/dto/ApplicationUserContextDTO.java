package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.capacidad.identityservice.model.dto.base.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class ApplicationUserContextDTO extends BaseDTO<Long> {

    @Valid
    private RoleDTO role;

    @NotNull
    @Valid
    private ApplicationUserDTO user;

    @Valid
    private IdDTO<Long> permissionSuggestion;

    @Valid
    private Set<IdDTO<Long>> permissionGroups;

}
