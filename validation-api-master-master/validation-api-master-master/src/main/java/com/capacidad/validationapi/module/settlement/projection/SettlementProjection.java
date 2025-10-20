package com.capacidad.validationapi.module.settlement.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SettlementProjection extends BaseProjection<Long> {

    BigDecimal getTotal();

    LocalDateTime getOpenedAt();

    LocalDateTime getClosedAt();

    IdAndNameOnlyProjection getStatus();

    @JsonIgnore
    PractitionerProjection.Minor getPractitioner();

    @JsonIgnore
    IdAndNameOnlyProjection getContract();

}
