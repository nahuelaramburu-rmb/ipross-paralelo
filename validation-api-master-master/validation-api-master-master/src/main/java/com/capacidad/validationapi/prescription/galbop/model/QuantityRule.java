package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QuantityRule {

    private int sizeId;

    private int maxInLine;

    private int maxInPrescription;

}
