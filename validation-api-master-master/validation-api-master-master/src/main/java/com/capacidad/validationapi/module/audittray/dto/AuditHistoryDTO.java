package com.capacidad.validationapi.module.audittray.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class AuditHistoryDTO extends BaseDTO<Long> {

    private UUID auditSub;

}
