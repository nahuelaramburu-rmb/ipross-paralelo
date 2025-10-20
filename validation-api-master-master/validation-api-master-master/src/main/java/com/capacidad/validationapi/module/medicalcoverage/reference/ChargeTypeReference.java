package com.capacidad.validationapi.module.medicalcoverage.reference;

import com.capacidad.validationapi.module.medicalcoverage.model.ChargeType;

public enum ChargeTypeReference {
    FIXED_VALUE(1L),
    PERCENTAGE(2L);

    private final long id;

    ChargeTypeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public ChargeType getInstance() {
        ChargeType chargeType = new ChargeType();
        chargeType.setId(getId());
        return chargeType;
    }

}
