package com.capacidad.identityservice.model;

public enum StateReference {
    CONFIRMED(1L),
    UNCONFIRMED(2L),
    DISABLED(3L);

    private long id;

    StateReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
