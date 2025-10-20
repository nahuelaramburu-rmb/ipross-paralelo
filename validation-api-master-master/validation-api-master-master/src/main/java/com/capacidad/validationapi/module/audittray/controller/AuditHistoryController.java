package com.capacidad.validationapi.module.audittray.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryAssignDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditHistoryResolutionDTO;
import com.capacidad.validationapi.module.audittray.hateoas.AuditHistoryResource;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.audittray.service.AuditHistoryService;
import com.capacidad.validationapi.module.audittray.service.AuditTraySender;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_AUDIT_HISTORY;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_HISTORIES;

@RestController
@RequestMapping(value = ENDPOINT_AUDIT_HISTORY)
public class AuditHistoryController {

    private final AuditHistoryService auditHistoryService;
    private final AuditTraySender auditTraySender;

    @Autowired
    public AuditHistoryController(AuditHistoryService auditHistoryService,
                                  AuditTraySender auditTraySender) {
        this.auditHistoryService = auditHistoryService;
        this.auditTraySender = auditTraySender;
    }

    @GetMapping(params = {"page", "size", "pending", "auditTrayResourceId"})
    public ResponseEntity<PageModelWrapper<AuditHistoryProjection.Minor>> getHistory(@RequestParam int page, @RequestParam int size, @RequestParam boolean pending, @RequestParam Collection<UUID> auditTrayResourceId) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<AuditHistoryProjection.Minor> pageResult = auditHistoryService.getHistory(auditTrayResourceId, pending, pageRequest);
        PageModelWrapper<AuditHistoryProjection.Minor> results = new PageModelWrapper<>(RESOURCE_AUDIT_HISTORIES, pageResult, pageRequest);
        results.setEncodeParams(false);
        results.addQueryParam("pending", String.valueOf(pending));
        results.addQueryParam("auditTrayResourceId", auditTrayResourceId);
        return ResponseEntity.ok(results);
    }

    @GetMapping(value = "/offline", params = {"page", "size", "auditTrayResourceId"})
    public ResponseEntity<PageModelWrapper<AuditHistoryProjection.Minor>> getOfflineHistory(@RequestParam int page, @RequestParam int size, @RequestParam Collection<UUID> auditTrayResourceId) throws ObjectNotFoundException {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<AuditHistoryProjection.Minor> pageResult = auditHistoryService.getOfflineHistory(auditTrayResourceId, pageRequest);
        PageModelWrapper<AuditHistoryProjection.Minor> results = new PageModelWrapper<>(RESOURCE_AUDIT_HISTORIES, pageResult, pageRequest);
        results.setEncodeParams(false);
        results.addQueryParam("auditTrayResourceId", auditTrayResourceId);
        return ResponseEntity.ok(results);
    }

    @PutMapping(value = "{objectId}")
    public ResponseEntity<Object> associateAuditHistoryWithAuditor(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        auditHistoryService.associateAuditHistoryWithAuditor(objectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "{objectId}")
    public ResponseEntity<Object> getAuditHistory(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        AuditHistoryProjection projection = auditHistoryService.getAuditHistory(objectId);
        return ResponseEntity.ok(new AuditHistoryResource(projection));
    }

    @PutMapping(value = "{objectId}/assignment")
    public ResponseEntity<Object> assignAuditHistoryToNewAuditTray(@PathVariable Long objectId, @Valid @RequestBody AuditHistoryAssignDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        AuditHistory auditHistory = auditHistoryService.assignAuditHistory(objectId, input);
        auditTraySender.sendIssueToAuditTrayQueue(auditHistory);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{objectId}/resolution")
    public ResponseEntity<MedicalAuthorizationProjection.Status> resolveAuditHistory(@PathVariable Long objectId, @Valid @RequestBody AuditHistoryResolutionDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(auditHistoryService.resolveIssue(objectId, input));
    }

}
