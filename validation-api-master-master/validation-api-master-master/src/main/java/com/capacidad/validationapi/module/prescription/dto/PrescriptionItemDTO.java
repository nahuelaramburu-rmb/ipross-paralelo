package com.capacidad.validationapi.module.prescription.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.medicine.dto.MedicineDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class PrescriptionItemDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private MedicineDTO medicine;

    @NotNull
    private Integer quantity;

    private String dailyDosage;

    @NotNull
    @Valid
    private IdDTO<Long> disease;

    private Integer treatmentDays;

}
