package com.capacidad.validationapi.module.audittray.dto;

import com.capacidad.validationapi.module.audittray.model.AuditTrayEvent;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class AuditHistoryResolutionDTO extends BaseDTO<Long> {

    @NotNull
    @Positive
    private Long medicalAuthorizationItemId;

    @NotEmpty
    @Size(min = 1, max = 200)
    private String resolution;

    @NotNull
    private AuditTrayEvent event;

    @Positive
    private Integer quantity;

}
