package com.capacidad.validationapi.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class XYPoint {

    @JsonIgnore
    private Object key;
    private Object x;
    private Object y;

    public XYPoint(Object key, Object x, Object y) {
        this.key = key;
        this.x = x;
        this.y = y;
    }

    public XYPoint(Object x, Object y) {
        this.x = x;
        this.y = y;
    }

}
