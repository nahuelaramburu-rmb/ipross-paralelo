package com.capacidad.validationapi.module.location.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface AddressProjection extends BaseProjection<Long> {

    String getDistrict();

    String getStreet();

    Integer getStreetNumber();

    String getApartment();

    CityProjection getCity();

    interface CityProjection {
        Long getId();

        String getName();

        String getPostalCode();

        ProvinceProjection getProvince();
    }

    interface ProvinceProjection {
        Long getId();

        String getName();

        IdAndNameOnlyProjection getCountry();
    }

}
