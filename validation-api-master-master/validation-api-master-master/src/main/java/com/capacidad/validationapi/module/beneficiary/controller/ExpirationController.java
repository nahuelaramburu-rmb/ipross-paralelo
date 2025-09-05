package com.capacidad.validationapi.module.beneficiary.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.beneficiary.dto.ExpirationDTO;
import com.capacidad.validationapi.module.beneficiary.service.ExpirationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_EXPIRATIONS;

@RestController
@RequestMapping(value = ENDPOINT_EXPIRATIONS)
public class ExpirationController extends ReducedBaseControllerImpl<ExpirationDTO, Long> {

    @Autowired
    public ExpirationController(ExpirationService abstractService) {
        super(abstractService);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToExpiration(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToExpiration(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToExpiration(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToExpiration(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.deleteOne(objectId);
    }

}
