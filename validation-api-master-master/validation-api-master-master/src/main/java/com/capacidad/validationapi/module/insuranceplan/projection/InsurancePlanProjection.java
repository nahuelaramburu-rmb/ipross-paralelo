package com.capacidad.validationapi.module.insuranceplan.projection;


import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface InsurancePlanProjection extends BaseProjection<Long>, IdAndNameOnlyProjection {

    IdAndNameOnlyProjection getInsurancePlanType();

    Integer getPriority();

}
