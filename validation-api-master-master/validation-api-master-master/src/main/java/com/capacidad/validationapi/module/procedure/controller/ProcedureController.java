package com.capacidad.validationapi.module.procedure.controller;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ModelConstants;
import com.capacidad.validationapi.module.base.controller.BaseController;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.service.ProcedureService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PROCEDURES;

@RestController
@RequestMapping(value = ENDPOINT_PROCEDURES)
public class ProcedureController implements BaseController<ProcedureDTO, Long> {

    private final ProcedureService procedureService;

    @Autowired
    public ProcedureController(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @Transactional(readOnly = true)
    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(StringUtils.join(ModelConstants.BENEFICIARY, DOT, ModelConstants.ID)).ascending());
        Page<ProcedureProjection> currentPage = procedureService.findAllProcedures(pageable, search);
        PageModelWrapper<EntityModel<ProcedureProjection>> pageModelWrapper = procedureService.buildPageResource(currentPage, pageable, search);
        return ResponseEntity.ok(pageModelWrapper);
    }

    @Transactional(readOnly = true)
    @GetMapping(params = {"page", "size", "groups"})
    public ResponseEntity<Object> getAllGrouped(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String groups) throws ObjectNotValidException {
        Pageable pageable = PageRequest.of(page, size, Sort.by(StringUtils.join(ModelConstants.BENEFICIARY, DOT, ModelConstants.ID)).ascending());
        return ResponseEntity.ok(new EntityModel<>(procedureService.findAllGroupedProcedures(pageable, groups)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return ResponseEntity.ok(procedureService.auditLogs(null, objectId));
    }

    @Override
    public ResponseEntity<Object> addOne(ProcedureDTO input) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, Map<String, Object> update) {
        return null;
    }

    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) {
        return null;
    }

}
