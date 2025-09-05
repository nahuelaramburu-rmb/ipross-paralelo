package com.capacidad.validationapi.module.beneficiary.dto;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.location.dto.AddressDTO;
import com.capacidad.validationapi.module.person.dto.EmptyIdPersonDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryRelativeDTO extends EmptyIdPersonDTO {

    @NotBlank
    private String beneficiaryCode;

    @NotNull
    @Valid
    private IdDTO<Long> relationshipType;

    @Valid
    private IdDTO<Long> beneficiaryCategory;

    @NotEmpty
    @Valid
    private Set<BeneficiaryInsurancePlanDTO> beneficiaryInsurancePlans;

    @Valid
    private AddressDTO address;

}
