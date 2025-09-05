package com.capacidad.validationapi.module.prescription.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class BulkPrescriptionDTO {

    @NotEmpty
    @Valid
    private Set<PrescriptionDTO> prescriptions;

}
