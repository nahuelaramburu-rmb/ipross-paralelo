package com.capacidad.validationapi.module.premedicalauthorization.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PrePopulatedPreMedicalAuthorizationDTO extends BasePreMedicalAuthorizationDTO {

    @Positive
    private Long petitionerIdNumber;

    @NotEmpty
    private String beneficiaryCode;

    @NotEmpty
    @Valid
    private Set<PrePopulatedPreMedicalAuthorizationItemDTO> preMedicalAuthorizationItems;

}
