package com.capacidad.validationapi.module.base.controller;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.io.Serializable;

public abstract class ReducedBaseControllerImpl<E extends BaseDTO<I>, I extends Serializable> extends BaseControllerImpl<E, I> implements BaseController<E, I> {

    public ReducedBaseControllerImpl(BaseService<? extends BaseEntity<I>, E, I> abstractService) {
        super(abstractService);
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody E input) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

}
