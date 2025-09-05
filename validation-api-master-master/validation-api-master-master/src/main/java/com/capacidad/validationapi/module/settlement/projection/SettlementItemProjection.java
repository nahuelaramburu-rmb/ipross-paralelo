package com.capacidad.validationapi.module.settlement.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SettlementItemProjection extends BaseProjection<Long> {

    @JsonIgnore
    IdAndNameOnlyProjection getMedicalCenter();

    @JsonIgnore
    MedicalAuthorizationProjection.IdCreationDateBeneficiary getMedicalAuthorization();

    @JsonIgnore
    NomenclatorProjection.Minor getNomenclator();

    Integer getQuantity();

    BigDecimal getSubtotal();

    BigDecimal getChargeUnitPrice();

    BigDecimal getUnitPrice();

    LocalDateTime getCreatedAt();

    Boolean getRefundable();

}
