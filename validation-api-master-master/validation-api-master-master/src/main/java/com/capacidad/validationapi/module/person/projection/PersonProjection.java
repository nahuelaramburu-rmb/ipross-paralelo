package com.capacidad.validationapi.module.person.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.person.model.Gender;

import java.time.LocalDate;

public interface PersonProjection extends BaseProjection<Long> {

    String getName();

    String getLastName();

    Long getIdNumber();

    IdTypeProjection getIdType();

    LocalDate getBirthDate();

    Long getWorkIdNumber();

    Gender getGender();

    String getCreatedBy();

    String getModifiedBy();

    interface Minor extends BaseProjection<Long> {

        String getName();

        String getLastName();

        Long getIdNumber();

        IdTypeProjection getIdType();

    }

}
