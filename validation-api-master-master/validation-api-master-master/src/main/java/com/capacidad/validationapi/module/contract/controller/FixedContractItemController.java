package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.contract.dto.ContractItemSpecialPriceDTO;
import com.capacidad.validationapi.module.contract.dto.FixedContractItemDTO;
import com.capacidad.validationapi.module.contract.projection.ContractItemSpecialPriceProjection;
import com.capacidad.validationapi.module.contract.service.FixedContractItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_FIXED_CONTRACT_ITEMS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_FIXED_CONTRACT_ITEMS)
public class FixedContractItemController extends ReducedBaseControllerImpl<FixedContractItemDTO, Long> {

    private final FixedContractItemService fixedContractItemService;

    @Autowired
    public FixedContractItemController(FixedContractItemService fixedContractItemService) {
        super(fixedContractItemService);
        this.fixedContractItemService = fixedContractItemService;
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItem(#fixedContractItemId)")
    @PostMapping(value = "{fixedContractItemId}/special-prices")
    public ResponseEntity<Object> addFixedSpecialPrice(@PathVariable Long fixedContractItemId, @Valid @RequestBody ContractItemSpecialPriceDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        ContractItemSpecialPriceProjection result = fixedContractItemService.addContractItemSpecialPrice(fixedContractItemId, input);
        SelfModel<ContractItemSpecialPriceProjection, Long> selfModel = new SelfModel<>(result);
        selfModel.add(linkTo((methodOn(ContractItemSpecialPriceController.class).getOne(result.getId()))).withSelfRel());
        return ResponseEntity.ok(selfModel);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItem(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItem(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItem(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.deleteOne(objectId);
    }

    @PreAuthorize("@contractChecker.hasAccessToContractItem(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

}
