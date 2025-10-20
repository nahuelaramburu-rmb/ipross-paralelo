package com.capacidad.validationapi.module.medicalcenter.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.projection.AddressProjection;

import java.util.UUID;

public interface MedicalCenterProjection extends BaseProjection<Long>, IdAndNameOnlyProjection {

    AddressProjection getAddress();

    UUID getResourceId();

    interface IdNameAndAddressProjection extends BaseProjection<Long> {

        String getName();

        AddressProjection getAddress();

    }

}
