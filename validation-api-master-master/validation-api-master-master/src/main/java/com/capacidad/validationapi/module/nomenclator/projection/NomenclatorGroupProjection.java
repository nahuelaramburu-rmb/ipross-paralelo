package com.capacidad.validationapi.module.nomenclator.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

import java.util.Set;

public interface NomenclatorGroupProjection extends BaseProjection<Long> {

    String getName();

    String getDescription();

    interface Extended extends NomenclatorGroupProjection {

        Set<NomenclatorProjection.Minor> getNomenclators();

    }

}
