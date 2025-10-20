package com.capacidad.validationapi.module.medicalauthorization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class RestrictionMessageExtra {

    private String message;
    private RestrictionMessageExtraType extraType;
    private List<Object> data;
    private String[] messageParams;

    public RestrictionMessageExtra(RestrictionMessageExtraType extraType, List<Object> data, String... messageParams) {
        this.extraType = extraType;
        this.data = data;
        this.messageParams = messageParams;
    }

}
