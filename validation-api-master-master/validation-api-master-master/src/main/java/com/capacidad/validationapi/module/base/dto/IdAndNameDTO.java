package com.capacidad.validationapi.module.base.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
public class IdAndNameDTO<I extends Serializable> extends BaseDTO<I> {
    @NotNull
    private I id;

    @NotEmpty
    private String name;
}