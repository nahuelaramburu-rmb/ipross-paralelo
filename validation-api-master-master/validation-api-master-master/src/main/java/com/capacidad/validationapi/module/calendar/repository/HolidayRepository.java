package com.capacidad.validationapi.module.calendar.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.calendar.model.Holiday;
import com.capacidad.validationapi.module.calendar.projection.HolidayProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends ExtendedJpaRepository<Holiday, Long> {
    List<HolidayProjection> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<Holiday> findByDate(LocalDate date);
}
