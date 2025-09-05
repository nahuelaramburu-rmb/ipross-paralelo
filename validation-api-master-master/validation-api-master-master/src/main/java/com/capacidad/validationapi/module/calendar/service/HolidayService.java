package com.capacidad.validationapi.module.calendar.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.calendar.dto.HolidayDTO;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.calendar.model.Holiday;
import com.capacidad.validationapi.module.calendar.projection.HolidayProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayService extends BaseService<Holiday, HolidayDTO, Long> {

    List<HolidayProjection> find(Integer year, Integer month) throws ObjectNotValidException;

    Optional<CalendarEventType> isDateHolidayOrWeekend(LocalDate date);

}
