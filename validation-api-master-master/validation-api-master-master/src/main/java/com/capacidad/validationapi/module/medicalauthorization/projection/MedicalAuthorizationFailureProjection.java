package com.capacidad.validationapi.module.medicalauthorization.projection;

import com.capacidad.validationapi.module.medicalauthorization.model.FailureType;
import com.fasterxml.jackson.databind.JsonNode;

public interface MedicalAuthorizationFailureProjection {

    String getName();

    String getAllowed();

    String getCurrent();

    FailureType getFailureType();

    JsonNode getExtra();

}
