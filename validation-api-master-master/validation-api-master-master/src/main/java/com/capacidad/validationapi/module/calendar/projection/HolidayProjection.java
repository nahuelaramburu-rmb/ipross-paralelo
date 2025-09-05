package com.capacidad.validationapi.module.calendar.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

import java.time.LocalDate;

public interface HolidayProjection extends BaseProjection<Long> {

    String getTitle();

    LocalDate getDate();

}
