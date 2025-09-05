package com.capacidad.validationapi.module.medicalcoverage.reference;

import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionType;

public enum RestrictionTypeReference {
    REJECTION(2L),
    AUDIT(1L);

    private final long id;

    RestrictionTypeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public RestrictionType getInstance() {
        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(getId());
        return restrictionType;
    }
}
