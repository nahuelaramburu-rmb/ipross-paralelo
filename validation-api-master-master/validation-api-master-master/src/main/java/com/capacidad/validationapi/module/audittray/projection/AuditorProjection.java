package com.capacidad.validationapi.module.audittray.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

import java.util.UUID;

public interface AuditorProjection extends BaseProjection<Long> {

    String getUsername();

    String getDisplayName();

    UUID getSub();

}
