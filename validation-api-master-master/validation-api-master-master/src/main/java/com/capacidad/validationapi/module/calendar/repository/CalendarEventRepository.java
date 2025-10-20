package com.capacidad.validationapi.module.calendar.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.calendar.model.CalendarEvent;

public interface CalendarEventRepository extends ExtendedJpaRepository<CalendarEvent, Long> {
}
