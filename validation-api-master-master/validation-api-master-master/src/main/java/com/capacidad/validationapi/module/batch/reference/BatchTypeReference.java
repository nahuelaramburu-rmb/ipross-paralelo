package com.capacidad.validationapi.module.batch.reference;

public enum BatchTypeReference {
    DISABILITY(1L),
    PATHOLOGY(2L),
    GENERIC(3L);

    private final long id;

    BatchTypeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
