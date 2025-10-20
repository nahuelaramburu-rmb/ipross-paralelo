package com.capacidad.validationapi.module.general.reference;

public enum StatusScopeReference {
    GENERAL(1L),
    VALIDATION(2L),
    BENEFICIARY(3L),
    SETTLEMENT(4L),
    BUDGET(5L),
    BATCH(6L),
    PROCEDURE(7L),
    PRESCRIPTION(8L),
    PRE_MEDICAL_AUTHORIZATION(9L);

    private final long id;

    StatusScopeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
