package com.capacidad.validationapi.module.location.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

import java.util.Set;

public interface RegionProjection extends BaseProjection<Long> {

    String getName();

    Set<IdAndNameOnlyProjection> getCities();

}
