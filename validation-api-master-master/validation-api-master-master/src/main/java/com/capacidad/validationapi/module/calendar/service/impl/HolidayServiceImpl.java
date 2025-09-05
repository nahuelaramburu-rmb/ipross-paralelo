package com.capacidad.validationapi.module.calendar.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.calendar.dto.HolidayDTO;
import com.capacidad.validationapi.module.calendar.model.CalendarEvent;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.calendar.model.Holiday;
import com.capacidad.validationapi.module.calendar.projection.HolidayProjection;
import com.capacidad.validationapi.module.calendar.repository.HolidayRepository;
import com.capacidad.validationapi.module.calendar.service.CalendarEventService;
import com.capacidad.validationapi.module.calendar.service.HolidayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayServiceImpl extends BaseServiceImpl<Holiday, HolidayDTO, Long> implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final CalendarEventService eventService;

    @Autowired
    public HolidayServiceImpl(HolidayRepository holidayRepository,
                              CalendarEventService eventService) {
        super(holidayRepository);
        this.holidayRepository = holidayRepository;
        this.eventService = eventService;
    }

    @Override
    public void validate(Holiday holiday) throws ObjectNotFoundException, ObjectNotValidException {
        CalendarEvent event = eventService.findById(holiday.getEvent().getId());
        if (holiday.getDate() == null) {
            if (eventDoesNotContainYearOrMonth(event))
                throw new ObjectNotValidException("calendar.invalidEventDefaultDates");
            var holidayDate = LocalDate.of(LocalDate.now().getYear(), event.getMonth(), event.getDay());
            holiday.setDate(holidayDate);
        }
        if (StringUtils.isBlank(holiday.getTitle()))
            holiday.setTitle(event.getTitle());
        holiday.setEvent(event);
    }

    private boolean eventDoesNotContainYearOrMonth(CalendarEvent event) {
        return event.getDay() == null || event.getMonth() == null;
    }

    @Override
    public List<HolidayProjection> find(Integer year, Integer month) throws ObjectNotValidException {
        List<LocalDate> calculatedDates = calculateDates(year, month);
        if (calculatedDates.size() != 2)
            throw new ObjectNotValidException("calendar.invalidCalculatedDates");
        return this.holidayRepository.findAllByDateBetween(calculatedDates.get(0), calculatedDates.get(1));
    }

    protected List<LocalDate> calculateDates(Integer y, Integer m) throws ObjectNotValidException {
        Optional<Integer> year = Optional.ofNullable(y);
        Optional<Integer> month = Optional.ofNullable(m);
        if (areYearAndMonthEmpty(year, month))
            throw new ObjectNotValidException("calendar.emptyYearAndMonth");
        List<LocalDate> dates = new ArrayList<>();
        if (!areValuesInRange(year, month))
            throw new ObjectNotValidException("calendar.valuesOutOfRange");
        if (areYearAndMonthPresent(year, month)) {
            var startDate = LocalDate.of(year.get(), month.get(), 1);
            dates.add(startDate);
            dates.add(LocalDate.of(year.get(), month.get(), startDate.lengthOfMonth()));
        }
        if (isYearOnly(year, month)) {
            dates.add(LocalDate.of(year.get(), 1, 1));
            dates.add(LocalDate.of(year.get(), 12, 31));
        }
        if (isMonthOnly(year, month)) {
            var startDate = LocalDate.now().withMonth(month.get()).withDayOfMonth(1);
            dates.add(startDate);
            dates.add(LocalDate.of(startDate.getYear(), month.get(), startDate.lengthOfMonth()));
        }
        return dates;
    }

    private boolean areYearAndMonthEmpty(Optional<Integer> year, Optional<Integer> month) {
        return year.isEmpty() && month.isEmpty();
    }

    private boolean areValuesInRange(Optional<Integer> year, Optional<Integer> month) {
        if (year.isPresent() && year.get() < 1900)
            return false;
        return (month.isEmpty() || (month.get() <= 12 && month.get() >= 1));
    }

    private boolean isYearOnly(Optional<Integer> year, Optional<Integer> month) {
        return year.isPresent() && month.isEmpty();
    }

    private boolean isMonthOnly(Optional<Integer> year, Optional<Integer> month) {
        return year.isEmpty() && month.isPresent();
    }

    private boolean areYearAndMonthPresent(Optional<Integer> year, Optional<Integer> month) {
        return year.isPresent() && month.isPresent();
    }

    @Override
    public Optional<CalendarEventType> isDateHolidayOrWeekend(LocalDate date) {
        Optional<Holiday> holiday = holidayRepository.findByDate(date);
        if (holiday.isPresent())
            return Optional.of(CalendarEventType.HOLIDAY);
        if (isWeekend(date))
            return Optional.of(CalendarEventType.WEEKEND);
        return Optional.empty();
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

}
