package com.capacidad.identityservice.service.base;

import com.capacidad.identityservice.model.base.BaseEntity;
import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;

import java.io.Serializable;

public interface BaseService<T extends BaseEntity<I>, I extends Serializable> {

    T create(BaseDTO<I> dto) throws ObjectNotValidException, ObjectNotFoundException;

    T mapDtoToInput(BaseDTO<I> dto);

    void validate(T object) throws ObjectNotValidException, ObjectNotFoundException;

}
