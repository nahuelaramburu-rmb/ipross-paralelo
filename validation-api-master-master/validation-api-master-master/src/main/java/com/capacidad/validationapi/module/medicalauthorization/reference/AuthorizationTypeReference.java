package com.capacidad.validationapi.module.medicalauthorization.reference;

import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;

public enum AuthorizationTypeReference {
    AUTHORIZATION_TYPE_MANUAL_CODE(1L),
    AUTHORIZATION_TYPE_MANUAL_ID_NUMBER(2L),
    AUTHORIZATION_TYPE_AUTOMATIC_QR(3L),
    AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION(4L),
    AUTHORIZATION_TYPE_MANUAL_MAGSTRIPE(5L);

    private final long id;

    AuthorizationTypeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public AuthorizationType getInstance() {
        AuthorizationType authorizationType = new AuthorizationType();
        authorizationType.setId(getId());
        return authorizationType;
    }
}
