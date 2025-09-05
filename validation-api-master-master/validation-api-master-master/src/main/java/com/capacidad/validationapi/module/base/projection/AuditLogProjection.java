package com.capacidad.validationapi.module.base.projection;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface AuditLogProjection<I extends Serializable> extends BaseProjection<I> {

    LocalDateTime getCreatedAt();

    LocalDateTime getModifiedAt();

    String getModifiedBy();

    String getCreatedBy();

}
