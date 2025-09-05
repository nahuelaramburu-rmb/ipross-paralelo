package com.capacidad.validationapi.module.procedure.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.procedure.model.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public interface ProcedureProjection extends BaseProjection<Long> {

    String getDescription();

    BeneficiaryProjection.Minor getBeneficiary();

    Set<Message> getMessages();

    IdAndNameOnlyProjection getStatus();

    Integer getFileCount();

    LocalDateTime getCreatedAt();

    LocalDateTime getClosedAt();

    LocalDate getExpiration();

    String getType();

    interface BeneficiaryId {

        BaseProjection<Long> getBeneficiary();

    }

}
