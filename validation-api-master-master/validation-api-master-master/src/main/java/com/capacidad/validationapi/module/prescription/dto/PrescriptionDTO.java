package com.capacidad.validationapi.module.prescription.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PrescriptionDTO extends BaseDTO<Long> {

    //Tenant validated in service
    @NotNull
    @Valid
    private IdDTO<Long> beneficiary;

    //Tenant validated in service
    @NotNull
    @Valid
    private IdDTO<Long> practitioner;

    private String transactionKey;

    private Set<Long> exchangeId = new HashSet<>();

    @NotEmpty
    @Valid
    private Set<PrescriptionItemDTO> prescriptionItems;

    @NotEmpty
    private String observations;

}
