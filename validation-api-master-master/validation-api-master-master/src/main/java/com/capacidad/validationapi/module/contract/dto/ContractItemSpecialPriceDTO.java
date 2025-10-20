package com.capacidad.validationapi.module.contract.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class ContractItemSpecialPriceDTO extends BaseDTO<Long> {

    @NotNull
    @Valid
    private IdDTO<Long> chargeType;

    @NotNull
    @Positive
    private BigDecimal specialValue;

    @NotNull
    private CalendarEventType eventType;

}
