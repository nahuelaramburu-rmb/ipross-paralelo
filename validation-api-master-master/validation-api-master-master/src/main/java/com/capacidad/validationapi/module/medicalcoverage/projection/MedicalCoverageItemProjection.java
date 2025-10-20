package com.capacidad.validationapi.module.medicalcoverage.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.capacidad.validationapi.module.person.model.Gender;

import java.math.BigDecimal;

public interface MedicalCoverageItemProjection extends BaseProjection<Long> {

    IdAndNameOnlyProjection getRestrictionType();

    NomenclatorProjection.Minor getNomenclator();

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

    interface Parent extends BaseProjection<Long> {

        MedicalCoverageProjection.Reduced getMedicalCoverage();

    }

}
