package com.capacidad.validationapi.module.practitioner.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.practitioner.dto.MedicalRegistrationDTO;
import com.capacidad.validationapi.module.practitioner.service.MedicalRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_MEDICAL_REGISTRATIONS)
public class MedicalRegistrationController extends ReducedBaseControllerImpl<MedicalRegistrationDTO, Long> {

    public MedicalRegistrationController(MedicalRegistrationService abstractService) {
        super(abstractService);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToMedicalRegistration(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToMedicalRegistration(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToMedicalRegistration(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToMedicalRegistration(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.deleteOne(objectId);
    }

}
