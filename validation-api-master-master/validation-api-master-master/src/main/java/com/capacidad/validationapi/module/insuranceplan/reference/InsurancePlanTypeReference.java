package com.capacidad.validationapi.module.insuranceplan.reference;

public enum InsurancePlanTypeReference {
    NORMAL(1L),
    SPECIAL(2L);
    private final long id;

    InsurancePlanTypeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
