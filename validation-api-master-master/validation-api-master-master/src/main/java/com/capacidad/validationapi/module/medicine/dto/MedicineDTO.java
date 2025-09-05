package com.capacidad.validationapi.module.medicine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class MedicineDTO {

    @NotNull
    private long exchangeId;

    @NotNull
    private long exchangePlanId;

    @NotNull
    private String product;

    @NotNull
    private String recommendation;

    private String presentation;

    private Integer units;

    private BigDecimal concentration;

    private Long concentrationTypeId;

    private Long productTypeId;

    private Long unitsTypeId;

    private Long drugId;

    private Long sizeId;

    @NotNull
    private BigDecimal authorizedDosage;

}
