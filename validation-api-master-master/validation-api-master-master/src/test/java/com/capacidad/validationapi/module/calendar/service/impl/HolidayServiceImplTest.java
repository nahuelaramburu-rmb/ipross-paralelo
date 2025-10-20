package com.capacidad.validationapi.module.calendar.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.calendar.model.CalendarEvent;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.calendar.model.Holiday;
import com.capacidad.validationapi.module.calendar.projection.HolidayProjection;
import com.capacidad.validationapi.module.calendar.repository.HolidayRepository;
import com.capacidad.validationapi.module.calendar.service.CalendarEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HolidayServiceImplTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private CalendarEventService calendarEventService;

    @Spy
    @InjectMocks
    private HolidayServiceImpl holidayService;

    @Test
    public void testValidateFailsWhenNullDateAndDefaultMonthAndDayAlsoNull() throws ObjectNotFoundException {
        var holiday = new Holiday();
        var event = new CalendarEvent();
        event.setId(1L);
        holiday.setEvent(event);

        when(calendarEventService.findById(event.getId())).thenReturn(event);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> holidayService.validate(holiday));

        assertThat(exception.getMessage()).isEqualTo("calendar.invalidEventDefaultDates");
    }

    @Test
    public void testValidateDoNotFailsWhenNullDateAndValidDefaultMonthAndDay() throws ObjectNotFoundException, ObjectNotValidException {
        var holiday = new Holiday();
        var event = new CalendarEvent();
        event.setId(1L);
        event.setTitle("defaultTitle");
        event.setMonth(6);
        event.setDay(28);
        holiday.setEvent(event);

        when(calendarEventService.findById(event.getId())).thenReturn(event);

        holidayService.validate(holiday);

        assertThat(holiday.getDate()).isEqualTo(LocalDate.of(LocalDate.now().getYear(), event.getMonth(), event.getDay()));
        assertThat(holiday.getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    public void testValidateDoNotOverridesPropsWhenSpecified() throws ObjectNotFoundException, ObjectNotValidException {
        var holiday = new Holiday();
        holiday.setDate(LocalDate.now());
        holiday.setTitle("myTitle");
        var event = new CalendarEvent();
        event.setId(1L);
        event.setTitle("defaultTitle");
        event.setMonth(6);
        event.setDay(28);
        holiday.setEvent(event);

        when(calendarEventService.findById(event.getId())).thenReturn(event);

        holidayService.validate(holiday);

        assertThat(holiday.getDate()).isEqualTo(LocalDate.now());
        assertThat(holiday.getTitle()).isNotEqualTo(event.getTitle());
    }


    @Test
    public void testCalculateDatesThrowsExceptionWhenYearAndMonthNull() {
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> holidayService.calculateDates(null, null));

        assertThat(exception.getMessage()).isEqualTo("calendar.emptyYearAndMonth");
    }

    @Test
    public void testCalculateDatesThrowsExceptionWhenYearOutOfRange() {
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> holidayService.calculateDates(1500, null));

        assertThat(exception.getMessage()).isEqualTo("calendar.valuesOutOfRange");
    }

    @Test
    public void testCalculateDatesThrowsExceptionWhenMonthLessThan1() {
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> holidayService.calculateDates(2021, 0));

        assertThat(exception.getMessage()).isEqualTo("calendar.valuesOutOfRange");
    }

    @Test
    public void testCalculateDatesThrowsExceptionWhenMonthBiggerThan12() {
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> holidayService.calculateDates(2021, 13));

        assertThat(exception.getMessage()).isEqualTo("calendar.valuesOutOfRange");
    }

    @Test
    public void testCalculateDatesReturnValidDatesWhenYearOnly() throws ObjectNotValidException {
        List<LocalDate> dates = holidayService.calculateDates(2021, null);

        assertThat(dates.get(0)).isEqualTo(LocalDate.of(2021, 1, 1));
        assertThat(dates.get(1)).isEqualTo(LocalDate.of(2021, 12, 31));
    }

    @Test
    public void testCalculateDatesReturnValidDatesWhenMonthOnly() throws ObjectNotValidException {
        List<LocalDate> dates = holidayService.calculateDates(null, 5);

        LocalDate startDate = LocalDate.now().withMonth(5).withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withMonth(5).withDayOfMonth(startDate.lengthOfMonth());
        assertThat(dates.get(0)).isEqualTo(startDate);
        assertThat(dates.get(1)).isEqualTo(endDate);
    }

    @Test
    public void testCalculateDatesReturnValidDatesWhenMonthAndYear() throws ObjectNotValidException {
        List<LocalDate> dates = holidayService.calculateDates(2021, 5);

        LocalDate startDate = LocalDate.of(2021, 5, 1);
        LocalDate endDate = LocalDate.of(2021, 5, 31);
        assertThat(dates.get(0)).isEqualTo(startDate);
        assertThat(dates.get(1)).isEqualTo(endDate);
    }

    @Test
    public void testFindThrowsExceptionWhenUnexpectedListFromCalculateDates() throws ObjectNotValidException {
        doReturn(Collections.emptyList()).when(holidayService).calculateDates(2021, 5);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> holidayService.find(2021, 5));

        assertThat(exception.getMessage()).isEqualTo("calendar.invalidCalculatedDates");
    }

    @Test
    public void testFindExecutesSuccessfullyWhenValidCalculatedDates() throws ObjectNotValidException {
        List<LocalDate> calculatedDates = new ArrayList<>();
        calculatedDates.add(LocalDate.now());
        calculatedDates.add(LocalDate.now().plusMonths(1));

        List<HolidayProjection> expectedResult = new ArrayList<>();

        doReturn(calculatedDates).when(holidayService).calculateDates(2021, 5);

        when(holidayRepository.findAllByDateBetween(calculatedDates.get(0), calculatedDates.get(1))).thenReturn(expectedResult);

        List<HolidayProjection> result = holidayService.find(2021, 5);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void testIsDateHolidayOrWeekendReturnsEmptyWhenNoHolidayNorWeekend() {
        LocalDate date = LocalDate.of(2021, 5, 28);

        when(holidayRepository.findByDate(date)).thenReturn(Optional.empty());

        Optional<CalendarEventType> eventType = holidayService.isDateHolidayOrWeekend(date);

        assertThat(eventType).isEmpty();
    }

    @Test
    public void testIsDateHolidayOrWeekendReturnsWeekendWhenSaturday() {
        LocalDate date = LocalDate.of(2021, 5, 29);

        when(holidayRepository.findByDate(date)).thenReturn(Optional.empty());

        Optional<CalendarEventType> eventType = holidayService.isDateHolidayOrWeekend(date);

        assertThat(eventType).isEqualTo(Optional.of(CalendarEventType.WEEKEND));
    }

    @Test
    public void testIsDateHolidayOrWeekendReturnsHolidayWhenFound() {
        LocalDate date = LocalDate.of(2021, 5, 29);

        when(holidayRepository.findByDate(date)).thenReturn(Optional.of(new Holiday()));

        Optional<CalendarEventType> eventType = holidayService.isDateHolidayOrWeekend(date);

        assertThat(eventType).isEqualTo(Optional.of(CalendarEventType.HOLIDAY));
    }

}
