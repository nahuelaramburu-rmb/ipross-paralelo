package com.capacidad.validationapi.module.person.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface IdTypeProjection extends IdAndNameOnlyProjection {

    String getAlias();

}
