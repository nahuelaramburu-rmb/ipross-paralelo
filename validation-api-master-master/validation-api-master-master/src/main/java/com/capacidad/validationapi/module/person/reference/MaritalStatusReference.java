package com.capacidad.validationapi.module.person.reference;

public enum MaritalStatusReference {
    SINGLE(2L);

    private final long id;

    MaritalStatusReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
