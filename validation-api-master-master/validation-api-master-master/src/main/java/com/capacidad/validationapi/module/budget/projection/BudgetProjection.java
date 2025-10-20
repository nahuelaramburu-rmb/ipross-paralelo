package com.capacidad.validationapi.module.budget.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BudgetProjection extends BaseProjection<Long> {

    BigDecimal getTotal();

    IdAndNameOnlyProjection getStatus();

    LocalDateTime getClosedAt();

    LocalDateTime getCreatedAt();

}
