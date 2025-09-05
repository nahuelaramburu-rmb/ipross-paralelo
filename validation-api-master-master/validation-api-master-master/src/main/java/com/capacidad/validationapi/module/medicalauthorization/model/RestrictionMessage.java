package com.capacidad.validationapi.module.medicalauthorization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RestrictionMessage {

    private String name;
    private String allowed;
    private String current;
    private RestrictionMessageExtra extra;

    public RestrictionMessage(String name, String allowed, String current, RestrictionMessageExtra extra) {
        this.name = name;
        this.allowed = allowed;
        this.current = current;
        this.extra = extra;
    }

}
