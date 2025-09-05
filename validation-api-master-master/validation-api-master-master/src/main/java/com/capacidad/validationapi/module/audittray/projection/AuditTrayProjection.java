package com.capacidad.validationapi.module.audittray.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.projection.AddressProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;

import java.util.Set;
import java.util.UUID;

public interface AuditTrayProjection extends BaseProjection<Long> {

    String getName();

    String getPurpose();

    UUID getResourceId();

    IdAndNameOnlyProjection getRegion();

    AddressProjection.CityProjection getCity();

    String getColor();

    interface Minor extends BaseProjection<Long> {
        String getName();

        UUID getResourceId();
    }

    interface Extended extends AuditTrayProjection {
        Set<AuditorProjection> getAuditors();

        Set<NomenclatorProjection.Minor> getNomenclators();
    }
}
