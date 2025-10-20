package com.capacidad.validationapi.module.settlement.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.settlement.model.SettlementOperation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class SettlementUpdateDTO extends BaseDTO<Long> {

    private Set<Long> itemIds;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @NotNull
    private SettlementOperation operation;

}
