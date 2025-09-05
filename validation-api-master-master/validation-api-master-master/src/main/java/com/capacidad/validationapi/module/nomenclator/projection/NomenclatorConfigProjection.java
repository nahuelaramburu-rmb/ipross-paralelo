package com.capacidad.validationapi.module.nomenclator.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

public interface NomenclatorConfigProjection extends BaseProjection<Long> {

    Boolean getReportRequired();

    Integer getExpirationDays();

    Integer getMaxInTransaction();

}
