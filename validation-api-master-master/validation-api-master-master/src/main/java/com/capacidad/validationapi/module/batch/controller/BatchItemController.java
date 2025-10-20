package com.capacidad.validationapi.module.batch.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.batch.dto.BatchItemDTO;
import com.capacidad.validationapi.module.batch.service.BatchItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_BATCH_ITEMS;

@RestController
@RequestMapping(value = ENDPOINT_BATCH_ITEMS)
public class BatchItemController extends ReducedBaseControllerImpl<BatchItemDTO, Long> {

    @Autowired
    public BatchItemController(BatchItemService batchItemService) {
        super(batchItemService);
    }

    @PreAuthorize("@batchChecker.hasAccessToBatchItem(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@batchChecker.hasAccessToBatchItem(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@batchChecker.hasAccessToBatchItem(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@batchChecker.hasAccessToBatchItem(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.deleteOne(objectId);
    }
}
