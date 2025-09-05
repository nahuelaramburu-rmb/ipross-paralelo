package com.capacidad.validationapi.module.batch.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.batch.dto.BatchDTO;
import com.capacidad.validationapi.module.batch.dto.BatchItemDTO;
import com.capacidad.validationapi.module.batch.hateoas.BatchItemResource;
import com.capacidad.validationapi.module.batch.hateoas.BatchResource;
import com.capacidad.validationapi.module.batch.projection.BatchItemProjection;
import com.capacidad.validationapi.module.batch.projection.BatchProjection;
import com.capacidad.validationapi.module.batch.service.BatchItemService;
import com.capacidad.validationapi.module.batch.service.BatchService;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_BATCHES;

@RestController
@RequestMapping(value = ENDPOINT_BATCHES)
public class BatchController extends BaseControllerImpl<BatchDTO, Long> {

    private final BatchService batchService;
    private final BatchItemService batchItemService;

    @Autowired
    public BatchController(BatchService batchService,
                           BatchItemService batchItemService) {
        super(batchService);
        this.batchService = batchService;
        this.batchItemService = batchItemService;
    }

    @GetMapping(params = {"page", "size", "minor"})
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "false") Boolean minor,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        if (Boolean.FALSE.equals(minor))
            return ResponseEntity.ok(batchService.findAll(pageable, BatchProjection.Full.class, search));
        return ResponseEntity.ok(batchService.findAll(pageable, BatchProjection.Minor.class, search));
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(batchService.findAll(pageable, BatchProjection.Full.class, search));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(params = {"page", "size", "groups", "beneficiaryId"})
    public ResponseEntity<EntityModel<Map<String, PageModelWrapper<EntityModel<BatchProjection.Minor>>>>> getAllGrouped(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String groups,
            @RequestParam Long beneficiaryId) throws ObjectNotValidException {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(new EntityModel<>(batchService.findAllGroupedProcedures(pageable, groups, beneficiaryId)));
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @PutMapping(value = "{objectId}")
    public ResponseEntity<BatchResource> updateStatus(@PathVariable Long objectId, @Valid @RequestBody StatusUpdateDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new BatchResource(batchService.updateStatus(objectId, input)));
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @GetMapping(value = "{objectId}/batch-items", params = {"page", "size"})
    public ResponseEntity<Object> getAllBatchItems(@PathVariable Long objectId,
                                                   @RequestParam int page,
                                                   @RequestParam int size,
                                                   @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        PageModelWrapper<EntityModel<BatchItemProjection>> currentPage = batchItemService.findAll(pageable, search, buildBatchSearchString(objectId, search));
        return ResponseEntity.ok(currentPage);
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#batchId)")
    @PostMapping(value = "{batchId}/batch-items")
    public ResponseEntity<BatchItemResource> addBatchItem(@PathVariable Long batchId, @Valid @RequestBody BatchItemDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        BatchItemProjection result = batchItemService.create(input, batchService.findById(batchId));
        return ResponseEntity.ok(new BatchItemResource(result));
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @PutMapping(value = "/{objectId}/files")
    public ResponseEntity<Object> addFiles(@RequestParam(value = "file") List<MultipartFile> file, @PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        batchService.addFiles(objectId, file);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @DeleteMapping(value = "/{objectId}/files", params = {"filename"})
    public ResponseEntity<Object> removeFile(@RequestParam String filename, @PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        batchService.removeFile(objectId, filename);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @GetMapping(value = "/{objectId}/files", params = {"filename"})
    public ResponseEntity<byte[]> retrieveFile(@PathVariable Long objectId, @RequestParam String filename) throws ObjectNotValidException {
        FileDTO file = batchService.getFile(objectId, filename);
        return RenderUtils.buildFileResponseEntity(file);
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @GetMapping(value = "/{objectId}/files")
    public ResponseEntity<List<SummaryDTO>> retrieveFileList(@PathVariable Long objectId) throws ObjectNotValidException {
        return ResponseEntity.ok().body(batchService.listFiles(objectId));
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(batchService.findProjectedById(objectId, BatchProjection.Full.class));
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@batchChecker.hasAccessToBatch(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(batchService.update(update, objectId, BatchProjection.Full.class));
    }

    @Override
    public ResponseEntity<Object> deleteOne(Long objectId) {
        return null;
    }

    private String buildBatchSearchString(Long batchId, String search) {
        return Utils.buildCompoundSearchString(String.format("batch:{id=%d}", batchId), search);
    }

}