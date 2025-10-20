package com.capacidad.validationapi.module.audittray.controller;

import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_AUDITORS;

@RestController
@RequestMapping(value = ENDPOINT_AUDITORS)
public class AuditorController extends ReducedBaseControllerImpl<AuditorDTO, Long> {
    @Autowired
    public AuditorController(AuditorService auditorService) {
        super(auditorService);
    }

    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
}
