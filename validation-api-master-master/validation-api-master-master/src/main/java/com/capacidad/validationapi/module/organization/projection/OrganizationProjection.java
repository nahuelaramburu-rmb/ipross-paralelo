package com.capacidad.validationapi.module.organization.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.projection.AddressProjection;

import java.util.UUID;

public interface OrganizationProjection extends BaseProjection<Long>, IdAndNameOnlyProjection {

    AddressProjection getAddress();

    IdAndNameOnlyProjection getRegion();

    UUID getResourceId();

}
