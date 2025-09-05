package com.capacidad.validationapi.module.ruleprocessor.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class TimedNomenclatorSetDTO implements RuleProperty {

    @Valid
    @NotEmpty
    private List<TimedNomenclatorDTO> timedNomenclators = new ArrayList<>();

}
