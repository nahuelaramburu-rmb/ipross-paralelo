package com.capacidad.validationapi.module.premedicalauthorization.dto;

import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.projection.PersonProjection;
import com.capacidad.validationapi.module.premedicalauthorization.projection.PreMedicalAuthorizationItemProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PreMedicalAuthorizationQrResponseDTO {

    private String code;

    private BeneficiaryProjection beneficiary;

    private PersonProjection petitioner;

    private IdAndNameOnlyProjection status;

    private String qr;

    @JsonProperty("medicalAuthorizationItems")
    private Set<PreMedicalAuthorizationItemProjection> preMedicalAuthorizationItems;

}
