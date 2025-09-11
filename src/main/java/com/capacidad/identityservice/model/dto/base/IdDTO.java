package com.capacidad.identityservice.model.dto.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
public class IdDTO<I extends Serializable> extends BaseDTO<I> {
    @NotNull
    private I id;
}

