package com.capacidad.validationapi.module.calendar.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

public interface CalendarEventProjection extends BaseProjection<Long> {
    String getTitle();

    Integer getMonth();

    Integer getDay();
}
