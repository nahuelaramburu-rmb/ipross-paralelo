package com.capacidad.validationapi.misc;

import com.capacidad.validationapi.module.general.model.Period;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

public final class DateUtils {

    private DateUtils() {

    }

    public static LocalDateTime resolvePeriodDateFrom(Period period) {
        switch (period) {
            case DAILY:
                return LocalDate.now().atTime(0, 0, 0, 0);
            case WEEKLY:
                return LocalDate.now().with(DayOfWeek.MONDAY).atTime(0, 0, 0, 0);
            case MONTHLY:
                return LocalDate.now().with(firstDayOfMonth()).atTime(0, 0, 0, 0);
            default:
                return LocalDate.now().with(firstDayOfYear()).atTime(0, 0, 0, 0);
        }
    }

    public static LocalDateTime resolvePeriodDateTo(Period period) {
        switch (period) {
            case DAILY:
                return LocalDate.now().atTime(0, 0, 0, 0).plusDays(1);
            case WEEKLY:
                return LocalDate.now().atTime(0, 0, 0, 0).plusWeeks(1);
            case MONTHLY:
                return LocalDate.now().atTime(0, 0, 0, 0).plusMonths(1);
            default:
                return LocalDate.now().atTime(0, 0, 0, 0).plusYears(1);
        }
    }

    public static long resolvePeriodDiff(LocalDate dateFrom, LocalDate dateTo, Period period) {
        switch (period) {
            case DAILY:
                return ChronoUnit.DAYS.between(dateFrom, dateTo);
            case WEEKLY:
                return ChronoUnit.WEEKS.between(dateFrom, dateTo);
            case MONTHLY:
                return ChronoUnit.MONTHS.between(dateFrom, dateTo);
            default:
                long diff = ChronoUnit.YEARS.between(dateFrom, dateTo);
                return diff == 0 ? 1 : diff;
        }
    }

}
