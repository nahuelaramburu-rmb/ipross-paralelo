package com.capacidad.validationapi.module.location.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface CountryProjection extends IdAndNameOnlyProjection {

    Integer getPhoneCode();

}
