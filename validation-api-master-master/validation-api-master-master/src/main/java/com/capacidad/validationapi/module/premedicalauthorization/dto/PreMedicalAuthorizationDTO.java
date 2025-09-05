package com.capacidad.validationapi.module.premedicalauthorization.dto;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PreMedicalAuthorizationDTO extends BasePreMedicalAuthorizationDTO {

    @NotNull
    @Valid
    private IdDTO<Long> beneficiary;

    @Valid
    private IdDTO<Long> petitioner;

    @NotEmpty
    @Valid
    private Set<PreMedicalAuthorizationItemDTO> preMedicalAuthorizationItems;

}
