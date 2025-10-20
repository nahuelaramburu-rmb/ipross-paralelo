package com.capacidad.validationapi.module.medicalauthorization.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.math.BigDecimal;
import java.util.Set;

public interface MedicalAuthorizationAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getStatus();

    BigDecimal getChargeTotal();

    String getCancellationReason();

    Boolean getAudited();

    Set<MedicalAuthorizationFailureProjection> getFailures();

    BaseProjection<Long> getRating();

    IdAndNameOnlyProjection getDisease();

    String getDiagnosis();

}
