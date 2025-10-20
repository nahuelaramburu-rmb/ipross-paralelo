package com.capacidad.identityservice.service.base;

import com.capacidad.identityservice.model.base.BaseEntity;
import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

@Slf4j
public abstract class BaseServiceImpl<T extends BaseEntity<I>, I extends Serializable> implements BaseService<T, I> {

    protected ExtendedRepository<T, I> repository;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected ModelMapper modelMapper;
    private Class<T> modelType;

    @SuppressWarnings("unchecked")
    public BaseServiceImpl(ExtendedRepository<T, I> repository) {
        this.repository = repository;
        this.modelType = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public T create(BaseDTO<I> dto) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        T object = this.mapDtoToInput(dto);
        object.setId(null);
        object.associateChildObjects();
        this.validate(object);
        T objectResponse = repository.save(object);
        log.info("create - void: {}({})", object.getClass(), object);
        return objectResponse;
    }

    @Override
    public T mapDtoToInput(BaseDTO<I> dto) {
        return modelMapper.map(dto, modelType);
    }

    @Override
    public void validate(T object) throws ObjectNotFoundException, ObjectNotValidException {
        //Not default method implemented
    }

}
