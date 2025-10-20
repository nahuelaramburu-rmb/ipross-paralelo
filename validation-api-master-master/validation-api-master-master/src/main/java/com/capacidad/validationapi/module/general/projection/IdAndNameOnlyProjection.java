package com.capacidad.validationapi.module.general.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

public interface IdAndNameOnlyProjection extends BaseProjection<Long> {

    String getName();

}
