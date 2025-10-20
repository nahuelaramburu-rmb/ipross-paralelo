package com.capacidad.validationapi.module.properties.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.properties.dto.PropertiesDTO;
import com.capacidad.validationapi.module.properties.projection.PropertiesProjection;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PROPERTIES;

@RestController
@RequestMapping(value = ENDPOINT_PROPERTIES)
public class PropertiesController extends ReducedBaseControllerImpl<PropertiesDTO, Long> {

    private final PropertiesService propertiesService;

    @Autowired
    public PropertiesController(PropertiesService propertiesService) {
        super(propertiesService);
        this.propertiesService = propertiesService;
    }

    @PatchMapping
    public ResponseEntity<PropertiesProjection> update(@NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotValidException, ObjectNotFoundException, MethodArgumentNotValidException {
        return ResponseEntity.ok(propertiesService.update(update));
    }

    @GetMapping
    public ResponseEntity<PropertiesProjection> getAll() {
        return ResponseEntity.ok(propertiesService.getPropertiesProjection());
    }

    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) {
        return null;
    }

    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) {
        return null;
    }

}
