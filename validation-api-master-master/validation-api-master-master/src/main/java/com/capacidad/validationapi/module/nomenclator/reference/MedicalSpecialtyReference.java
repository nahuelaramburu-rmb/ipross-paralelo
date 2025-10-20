package com.capacidad.validationapi.module.nomenclator.reference;

public enum MedicalSpecialtyReference {
    ALL(63L);

    private final long id;

    MedicalSpecialtyReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
