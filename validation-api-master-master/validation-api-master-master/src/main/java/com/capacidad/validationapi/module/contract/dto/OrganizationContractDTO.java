package com.capacidad.validationapi.module.contract.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class OrganizationContractDTO extends ContractDTO {

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> organization;

}
