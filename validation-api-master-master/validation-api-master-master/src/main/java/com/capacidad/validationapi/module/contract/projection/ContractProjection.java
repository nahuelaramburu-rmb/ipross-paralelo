package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ContractProjection extends BaseProjection<Long> {

    String getName();

    String getContractCode();

    LocalDateTime getCreatedAt();

    LocalDate getDateTo();

    LocalDate getDateFrom();

    String getType();

    Boolean getTransitCondition();

    Boolean getActive();

    Boolean getAutoSettlement();

    Integer getDayOfSettlement();

}
