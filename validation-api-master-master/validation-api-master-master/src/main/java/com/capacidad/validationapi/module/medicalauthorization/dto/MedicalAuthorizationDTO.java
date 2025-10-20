package com.capacidad.validationapi.module.medicalauthorization.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationCodeDTO;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionMinorDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class MedicalAuthorizationDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> practitioner;

    @Valid
    private IdDTO<Long> medicalCenter;

    @Valid
    private IdDTO<Long> petitioner;

    @Valid
    private IdDTO<Long> disease;

    @Size(max = 1000)
    private String diagnosis;

    @Valid
    private IdDTO<Long> selectedContract;

    private byte[] digitalSignature;

    private PreMedicalAuthorizationCodeDTO preMedicalAuthorization;

    @NotEmpty
    @Valid
    private Set<MedicalAuthorizationItemDTO> medicalAuthorizationItems;

    @Valid
    private Set<PrescriptionMinorDTO> prescriptions = new HashSet<>();

}
