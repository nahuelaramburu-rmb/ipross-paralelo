package com.capacidad.validationapi.module.beneficiary.dto;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.location.dto.AddressDTO;
import com.capacidad.validationapi.module.person.dto.PersonDTO;
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
public class BeneficiaryDTO extends PersonDTO {

    @NotBlank
    private String beneficiaryCode;

    @Valid
    private IdDTO<Long> beneficiaryCategory;

    @NotNull
    @Valid
    private IdDTO<Long> paymentMethod;

    @Valid
    private IdDTO<Long> company;

    @NotEmpty
    @Valid
    private Set<BeneficiaryInsurancePlanDTO> beneficiaryInsurancePlans;

    @NotNull
    private AddressDTO address;

}
