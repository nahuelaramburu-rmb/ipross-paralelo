package com.capacidad.validationapi.module.audittray.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationItemProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface AuditHistoryItemProjection extends BaseProjection<Long> {

    @Override
    @JsonProperty("auditHistoryItemId")
    Long getId();

    @JsonIgnore
    MedicalAuthorizationItemProjection getMedicalAuthorizationItem();

    AuditTrayProjection.Minor getAuditTray();

}
