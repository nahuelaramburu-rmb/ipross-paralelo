package com.capacidad.validationapi.module.medicalcoverage.projection;

import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.model.Gender;

import java.math.BigDecimal;

public interface MedicalCoverageItemAuditLogProjection extends AuditLogProjection<Long> {

    IdAndNameOnlyProjection getRestrictionType();

    Gender getGender();

    Integer getAgeFrom();

    Integer getAgeTo();

    Integer getAwaitDays();

    Integer getFixedMaxQuantity();

    Integer getFixedMaxDays();

    Integer getFreeMaxQuantity();

    Integer getFreeMaxDays();

    IdAndNameOnlyProjection getChargeType();

    BigDecimal getChargeValue();

    Boolean getAuditRequired();

}
