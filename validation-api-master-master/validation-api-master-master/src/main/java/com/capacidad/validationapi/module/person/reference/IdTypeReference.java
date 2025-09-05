package com.capacidad.validationapi.module.person.reference;

import com.capacidad.validationapi.module.person.model.IdType;

public enum IdTypeReference {
    TEMPORARY_ID(6L),
    ID(1L);

    private final long id;

    IdTypeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public IdType getInstance() {
        IdType idType = new IdType();
        idType.setId(id);
        return idType;
    }

}
