package com.capacidad.validationapi.module.practitioner.dto;

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
public class PractitionerDTO extends PersonDTO {

    @Valid
    private IdDTO<Long> practitionerCategory;

    @NotBlank
    private String practitionerCode;

    @NotEmpty
    @Valid
    private Set<MedicalRegistrationDTO> medicalRegistrations;

    @NotEmpty
    @Valid
    private Set<IdDTO<Long>> medicalSpecialties;

    @Valid
    private Set<IdDTO<Long>> contracts;

    @NotEmpty
    @Valid
    private Set<IdDTO<Long>> medicalCenters;

    @NotNull
    private AddressDTO address;

}
