package com.capacidad.identityservice.model.dto.base;

import lombok.Getter;

import java.io.Serializable;

@Getter
public abstract class BaseDTO<I extends Serializable> {
    protected I id;
}

