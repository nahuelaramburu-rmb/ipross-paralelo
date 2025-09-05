package com.capacidad.validationapi.module.base.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;

public abstract class BaseControllerImpl<E extends BaseDTO<I>, I extends Serializable> implements BaseController<E, I> {

    private final BaseService<? extends BaseEntity<I>, E, I> abstractService;
    @Autowired
    protected Utils utils;

    public BaseControllerImpl(BaseService<? extends BaseEntity<I>, E, I> abstractService) {
        this.abstractService = abstractService;
    }

    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable I objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(abstractService.findProjectedById(objectId));
    }

    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable I objectId) {
        return ResponseEntity.ok(abstractService.auditLogs(null, objectId));
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) throws ObjectNotFoundException, ObjectNotValidException {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(abstractService.findAll(pageable, search));
    }

    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody E input) throws ObjectNotValidException, ObjectNotFoundException {
        BaseEntity<I> result = abstractService.create(input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path(ID_PATH)
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable I objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(abstractService.update(update, objectId));
    }

    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable I objectId) throws ObjectNotFoundException, ObjectNotValidException {
        JsonNode id = abstractService.delete(objectId);
        return ResponseEntity.ok(id);
    }

}
