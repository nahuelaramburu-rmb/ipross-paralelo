package com.capacidad.validationapi.module.calendar.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class HolidayDTO extends BaseDTO<Long> {

    @Size(max = 100)
    private String title;

    private LocalDate date;

    @NotNull
    @Valid
    private IdDTO<Long> event;

}
