package com.capacidad.validationapi.module.calendar.service.impl;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.calendar.model.CalendarEvent;
import com.capacidad.validationapi.module.calendar.projection.CalendarEventProjection;
import com.capacidad.validationapi.module.calendar.repository.CalendarEventRepository;
import com.capacidad.validationapi.module.calendar.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarEventServiceImpl extends BaseServiceImpl<CalendarEvent, BaseDTO<Long>, Long> implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    @Autowired
    public CalendarEventServiceImpl(CalendarEventRepository calendarEventRepository) {
        super(calendarEventRepository);
        this.calendarEventRepository = calendarEventRepository;
    }

    @Override
    public List<CalendarEventProjection> findAllEvents() {
        return calendarEventRepository.findAllProjectedBy(CalendarEventProjection.class);
    }
}
