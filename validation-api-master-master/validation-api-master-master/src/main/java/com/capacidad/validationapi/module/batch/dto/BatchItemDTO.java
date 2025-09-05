package com.capacidad.validationapi.module.batch.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.general.model.Period;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class BatchItemDTO extends BaseDTO<Long> {

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> nomenclator;

    @Valid
    private Set<IdDTO<Long>> practitioners;

    @Valid
    private Set<IdDTO<Long>> medicalCenters;

    @NotNull
    private Integer amount;

    private Period period;

}
