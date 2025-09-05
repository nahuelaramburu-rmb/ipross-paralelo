package com.capacidad.validationapi.module.budget.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BudgetItemProjection extends BaseProjection<Long> {

    @JsonIgnore
    BaseProjection<Long> getMedicalAuthorization();

    LocalDateTime getCreatedAt();

    BigDecimal getChargeSubtotal();

    BigDecimal getChargeUnitPrice();

    Integer getQuantity();

    @JsonIgnore
    NomenclatorProjection.Minor getNomenclator();

}
