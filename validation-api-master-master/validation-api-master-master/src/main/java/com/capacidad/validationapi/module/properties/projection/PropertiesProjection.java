package com.capacidad.validationapi.module.properties.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.model.Period;

import java.util.Map;

public interface PropertiesProjection extends BaseProjection<Long> {

    Integer getPreAuthorizationMaxDays();

    Map<String, Object> getMappings();

    String getPrescriptionService();

    Period getPrescriptionExpirationPeriod();

    Integer getBeneficiaryMinAccountAge();

    Integer getHolderBeneficiaryMinAge();

}
