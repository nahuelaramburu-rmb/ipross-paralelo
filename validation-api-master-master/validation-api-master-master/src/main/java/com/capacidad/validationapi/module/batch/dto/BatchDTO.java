package com.capacidad.validationapi.module.batch.dto;

import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class BatchDTO extends BaseDTO<Long> {

    @Valid
    private Set<IdDTO<Long>> diagnosis;

    @Size(max = 1000)
    private String description;

    @NotNull
    private LocalDate dateFrom;

    @FutureOrPresent
    @NotNull
    private LocalDate dateTo;

    @Immutable
    @NotNull
    @Valid
    private IdDTO<Long> beneficiary;

    @Immutable
    @Valid
    private Set<BatchItemDTO> batchItems = new HashSet<>();

}
