package com.capacidad.validationapi.module.base.model;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class AuditLog<P extends BaseProjection<I>, I extends Serializable> {

    private P payload;

    private String operation;

    private Set<String> modifiedFields;

}
