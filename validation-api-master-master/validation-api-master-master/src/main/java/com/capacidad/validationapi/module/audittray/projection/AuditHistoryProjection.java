package com.capacidad.validationapi.module.audittray.projection;

import com.capacidad.validationapi.module.audittray.config.AuditHistoryMinorSerializer;
import com.capacidad.validationapi.module.audittray.model.AuditTrayEvent;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Set;

public interface AuditHistoryProjection extends BaseProjection<Long> {

    AuditTrayProjection.Minor getAuditTray();

    AuditTrayProjection.Minor getFromAuditTray();

    AuditorProjection getFromAuditor();

    String getReason();

    AuditTrayEvent getEvent();

    AuditorProjection getAuditor();

    @JsonIgnore
    MedicalAuthorizationProjection getMedicalAuthorization();

    @JsonIgnore
    Set<AuditHistoryItemProjection> getHistoryItems();

    @JsonSerialize(using = AuditHistoryMinorSerializer.class)
    interface Minor extends BaseProjection<Long> {

        AuditTrayProjection.Minor getAuditTray();

        AuditTrayProjection.Minor getFromAuditTray();

        AuditorProjection getFromAuditor();

        String getReason();

        AuditTrayEvent getEvent();

        MedicalAuthorizationProjection.IdStatusAndCreationDate getMedicalAuthorization();
    }

}
