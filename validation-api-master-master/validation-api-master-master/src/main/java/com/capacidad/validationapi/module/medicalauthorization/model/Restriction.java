package com.capacidad.validationapi.module.medicalauthorization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Restriction {

    private String name;

    private String allowed;

    private String current;

    private FailureType failureType;

    private RestrictionType restrictionType;

    private RestrictionMessageExtra extra;

    public Restriction(RestrictionType restrictionType, FailureType failureType, RestrictionMessage restrictionMessage) {
        this.restrictionType = restrictionType;
        this.failureType = failureType;
        this.name = restrictionMessage.getName();
        this.current = restrictionMessage.getCurrent();
        this.allowed = restrictionMessage.getAllowed();
        this.extra = restrictionMessage.getExtra();
    }

}
