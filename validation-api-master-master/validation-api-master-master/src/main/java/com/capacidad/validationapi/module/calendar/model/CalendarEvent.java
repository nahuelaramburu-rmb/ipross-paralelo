package com.capacidad.validationapi.module.calendar.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "calendar_event")
@NoArgsConstructor
@Getter
@Setter
public class CalendarEvent extends BaseEntity<Long> {

    @Column(unique = true, nullable = false)
    private String title;

    @Column
    private Integer day;

    @Column
    private Integer month;

}

