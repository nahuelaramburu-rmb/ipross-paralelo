package com.capacidad.validationapi.module.procedure.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ModelConstants;
import com.capacidad.validationapi.module.base.controller.BaseController;
import com.capacidad.validationapi.module.procedure.dto.FileTagDTO;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.model.FileTag;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.service.BaseProcedureService;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;

public abstract class BaseProcedureControllerImpl<D extends ProcedureDTO, R extends ProcedureResolutionDTO, T extends Procedure> implements BaseController<D, Long> {

    private final BaseProcedureService<T, D, R> procedureService;

    public BaseProcedureControllerImpl(BaseProcedureService<T, D, R> procedureService) {
        this.procedureService = procedureService;
    }

    @PostMapping
    public ResponseEntity<Object> addOne(@RequestParam(value = "file") List<MultipartFile> file,
                                         @Valid @RequestParam(value = "body") D input) throws ObjectNotValidException, ObjectNotFoundException {
        ProcedureProjection result = procedureService.create(input, file);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path(ID_PATH)
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @PatchMapping(value = "{objectId}")
    public ResponseEntity<EntityModel<ProcedureProjection>> updateOne(@PathVariable Long objectId,
                                                                      @RequestParam(value = "file") List<MultipartFile> file,
                                                                      @RequestParam(value = "body") Map<String, Object> update,
                                                                      @RequestParam(value = "filesToRemove", required = false) List<String> filesToRemove) throws ObjectNotValidException, ObjectNotFoundException {
        ProcedureProjection result = procedureService.update(update, objectId, file, filesToRemove);
        return ResponseEntity.ok(procedureService.getResource(result));
    }

    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @PutMapping(value = "/{objectId}/messages")
    public ResponseEntity<Message> addMessage(@PathVariable Long objectId, @Valid @RequestBody MessageDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(procedureService.addMessage(objectId, input));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @GetMapping(value = "/{objectId}/messages")
    public ResponseEntity<Set<Message>> getMessages(@PathVariable Long objectId) throws ObjectNotFoundException {
        return ResponseEntity.ok(procedureService.getMessages(objectId));
    }

    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @PutMapping(value = "/{objectId}/tags")
    public ResponseEntity<FileTag> addFileTag(@PathVariable Long objectId, @Valid @RequestBody FileTagDTO input) throws ObjectNotFoundException {
        return ResponseEntity.ok(procedureService.addFileTag(objectId, input));
    }

    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @PutMapping(value = "/{objectId}/files")
    public ResponseEntity<Object> addFiles(@RequestParam(value = "file") List<MultipartFile> file, @PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        procedureService.addFiles(objectId, file);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @DeleteMapping(value = "/{objectId}/files", params = {"filename"})
    public ResponseEntity<Object> removeFile(@RequestParam String filename, @PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        procedureService.removeFile(objectId, filename);
        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @GetMapping(value = "/{objectId}/files", params = {"filename"})
    public ResponseEntity<byte[]> retrieveFile(@PathVariable Long objectId, @RequestParam String filename) throws ObjectNotValidException {
        FileDTO file = procedureService.getFile(objectId, filename);
        return RenderUtils.buildFileResponseEntity(file);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @GetMapping(value = "/{objectId}/files")
    public ResponseEntity<List<SummaryDTO>> retrieveFileList(@PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok().body(procedureService.listFiles(objectId));
    }

    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @PutMapping(value = "{objectId}")
    public ResponseEntity<Object> resolve(@PathVariable Long objectId, @Valid @RequestBody R input) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(procedureService.resolve(objectId, input));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@procedureChecker.hasAccessToProcedure(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(procedureService.findProjectedById(objectId));
    }

    @Transactional(readOnly = true)
    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(StringUtils.join(ModelConstants.BENEFICIARY, DOT, ModelConstants.ID)).ascending());
        return ResponseEntity.ok(procedureService.findAll(pageable, search));
    }

    @Transactional(readOnly = true)
    @GetMapping(params = {"page", "size", "groups"})
    public ResponseEntity<Object> getAllGrouped(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String groups) throws ObjectNotValidException {
        Pageable pageable = PageRequest.of(page, size, Sort.by(StringUtils.join(ModelConstants.BENEFICIARY, DOT, ModelConstants.ID)).ascending());
        return ResponseEntity.ok(new EntityModel<>(procedureService.findAllGrouped(pageable, groups)));
    }

    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody D input) {
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
