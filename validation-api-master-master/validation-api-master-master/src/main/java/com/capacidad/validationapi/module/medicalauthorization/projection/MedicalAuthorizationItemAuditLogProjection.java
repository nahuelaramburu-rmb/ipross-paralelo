package com.capacidad.validationapi.module.medicalauthorization.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.util.Set;

public interface MedicalAuthorizationItemAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getStatus();

    Integer getQuantity();

    Set<MedicalAuthorizationFailureProjection> getFailures();

    String getResolution();

    Boolean getSettled();

}
