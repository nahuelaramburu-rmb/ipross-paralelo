package com.capacidad.validationapi.module.person.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.projection.AddressProjection;
import com.capacidad.validationapi.module.person.model.PhoneType;

public interface PersonDetailProjection extends BaseProjection<Long> {

    IdAndNameOnlyProjection getStudies();

    IdAndNameOnlyProjection getMaritalStatus();

    IdAndNameOnlyProjection getOccupation();

    AddressProjection getAddress();

    String getEmail();

    PhoneProjection getPhone();

    interface PhoneProjection extends BaseProjection<Long> {
        Integer getAreaCode();

        Long getPhoneNumber();

        PhoneType getPhoneType();
    }

}
