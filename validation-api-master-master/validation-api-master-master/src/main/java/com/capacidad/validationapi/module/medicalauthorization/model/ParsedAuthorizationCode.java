package com.capacidad.validationapi.module.medicalauthorization.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class ParsedAuthorizationCode {

    private final Long idNumber;

    private final Integer idTypeId;

    private final LocalDateTime generationDate;

    private final Integer validationType;

}
