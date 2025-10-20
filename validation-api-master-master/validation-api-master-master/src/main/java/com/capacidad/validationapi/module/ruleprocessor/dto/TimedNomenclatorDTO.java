package com.capacidad.validationapi.module.ruleprocessor.dto;

import com.capacidad.validationapi.module.base.dto.IdAndNameDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
public class TimedNomenclatorDTO implements RuleProperty {

    @NotNull
    @Valid
    private IdAndNameDTO<Long> nomenclator;

    @NotNull
    private LocalTime timeFrom;

    @NotNull
    private LocalTime timeTo;

}
