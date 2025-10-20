package com.capacidad.validationapi.module.beneficiary.dto;

import com.capacidad.validationapi.module.person.model.IdType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class BeneficiaryVerificationDTO {

    private long idNumber;
    private IdType idType;
    private LocalDate birthDate;
    private String beneficiaryCode;

}
