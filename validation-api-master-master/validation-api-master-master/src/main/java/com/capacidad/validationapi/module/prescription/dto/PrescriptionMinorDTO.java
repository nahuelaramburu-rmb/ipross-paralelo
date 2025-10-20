package com.capacidad.validationapi.module.prescription.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PrescriptionMinorDTO extends BaseDTO<Long> {

    @NotEmpty
    @Valid
    private Set<PrescriptionItemDTO> prescriptionItems;

    private String observations;

    private String transactionKey;

    private Set<Long> exchangeId = new HashSet<>();

}
