package com.capacidad.validationapi.module.calendar.service;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.calendar.model.CalendarEvent;
import com.capacidad.validationapi.module.calendar.projection.CalendarEventProjection;

import java.util.List;

public interface CalendarEventService extends BaseService<CalendarEvent, BaseDTO<Long>, Long> {
    List<CalendarEventProjection> findAllEvents();
}
