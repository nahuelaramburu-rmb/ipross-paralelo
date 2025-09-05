package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.contract.dto.ContractItemSpecialPriceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@RestController
@RequestMapping(value = "/contract-items/fixed/special-prices")
public class ContractItemSpecialPriceController extends ReducedBaseControllerImpl<ContractItemSpecialPriceDTO, Long> {

    public ContractItemSpecialPriceController(BaseService<? extends BaseEntity<Long>, ContractItemSpecialPriceDTO, Long> abstractService) {
        super(abstractService);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItemSpecialPrice(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItemSpecialPrice(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItemSpecialPrice(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.deleteOne(objectId);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItemSpecialPrice(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

}
