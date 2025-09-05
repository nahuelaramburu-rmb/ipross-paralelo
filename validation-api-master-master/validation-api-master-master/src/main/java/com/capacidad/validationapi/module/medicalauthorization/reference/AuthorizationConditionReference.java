package com.capacidad.validationapi.module.medicalauthorization.reference;

import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationCondition;

public enum AuthorizationConditionReference {
    USAGE_RATE_EXCEEDED(1L),
    TRANSIT(2L),
    MAXIMUM_EXCEEDED(3L),
    MONETARY_EXCEEDED(4L),
    CONTRACT_EXCESS(5L);

    private final long id;

    AuthorizationConditionReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public AuthorizationCondition getInstance() {
        AuthorizationCondition authorizationCondition = new AuthorizationCondition();
        authorizationCondition.setId(getId());
        return authorizationCondition;
    }
}
