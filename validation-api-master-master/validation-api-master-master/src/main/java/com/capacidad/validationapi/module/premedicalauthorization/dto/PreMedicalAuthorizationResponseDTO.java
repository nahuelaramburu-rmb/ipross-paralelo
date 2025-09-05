package com.capacidad.validationapi.module.premedicalauthorization.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.ByteArrayOutputStream;

@NoArgsConstructor
@Getter
@Setter
public class PreMedicalAuthorizationResponseDTO {

    ByteArrayOutputStream outputStream;

    String code;

}
