package com.capacidad.validationapi.module.contract.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class ContractDTO extends BaseDTO<Long> {

    @NotEmpty
    private String name;

    private String contractCode;

    @Future
    @NotNull
    private LocalDate dateTo;

    @FutureOrPresent
    @NotNull
    private LocalDate dateFrom;

    @NotNull
    private Boolean transitCondition;

    @NotNull
    private Boolean active;

    @NotNull
    private Boolean autoSettlement;

    @Min(value = 1)
    @Max(value = 31)
    @NotNull
    private Integer dayOfSettlement;

}
