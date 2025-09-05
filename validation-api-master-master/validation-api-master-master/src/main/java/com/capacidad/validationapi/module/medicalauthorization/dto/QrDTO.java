package com.capacidad.validationapi.module.medicalauthorization.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class QrDTO {

    @NotEmpty
    private String encryptedQr;

}
