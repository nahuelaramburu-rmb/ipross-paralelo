package com.capacidad.validationapi.module.medicalauthorization.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationItemDTO;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationItemProjection;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_AUTHORIZATION_ITEMS;

@RestController
@RequestMapping(value = ENDPOINT_AUTHORIZATION_ITEMS)
public class MedicalAuthorizationItemController extends BaseControllerImpl<MedicalAuthorizationItemDTO, Long> {

    private final MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Autowired
    public MedicalAuthorizationItemController(MedicalAuthorizationItemService medicalAuthorizationItemService) {
        super(medicalAuthorizationItemService);
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(medicalAuthorizationItemService.findAll(pageable, MedicalAuthorizationItemProjection.WithParent.class, search));
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorizationItem(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorizationItem(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        return super.getOne(objectId);
    }

    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody MedicalAuthorizationItemDTO input) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

}
