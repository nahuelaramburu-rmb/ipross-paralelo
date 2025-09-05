package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.contract.dto.*;
import com.capacidad.validationapi.module.contract.model.*;
import com.capacidad.validationapi.module.contract.projection.*;
import com.capacidad.validationapi.module.contract.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.net.URI;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;

public abstract class BaseContractController<T extends Contract, D extends ContractDTO> extends BaseControllerImpl<D, Long> {

    private final BaseContractService<T, D> baseContractService;

    @Autowired
    private FixedContractItemService fixedContractItemService;
    @Autowired
    private UsageRateAdjustmentService usageRateAdjustmentService;
    @Autowired
    private MaximumAdjustmentService maximumAdjustmentService;
    @Autowired
    private MonetaryAdjustmentService monetaryAdjustmentService;
    @Autowired
    private ContractAdjustmentService contractAdjustmentService;

    public BaseContractController(BaseContractService<T, D> baseContractService) {
        super(baseContractService);
        this.baseContractService = baseContractService;
    }

    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody D input) throws ObjectNotValidException, ObjectNotFoundException {
        T contract = baseContractService.create(input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path(ID_PATH)
                .buildAndExpand(contract.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @PostMapping(value = "{contractId}/contract-items/fixed")
    public ResponseEntity<Object> addFixedContractItem(@PathVariable Long contractId, @Valid @RequestBody FixedContractItemDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        T contract = baseContractService.validateReference(contractId);
        FixedContractItem result = fixedContractItemService.create(contract, input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_FIXED_CONTRACT_ITEMS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @GetMapping(value = "{contractId}/contract-items/fixed", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<FixedContractItemProjection>>> getAllFixedContractItems(@PathVariable Long contractId,
                                                                                                               @RequestParam int page,
                                                                                                               @RequestParam int size,
                                                                                                               @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(fixedContractItemService.findAll(pageable, buildContractSearchString(contractId, search)));
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @PostMapping(value = "{contractId}/adjustments/usage-rate")
    public ResponseEntity<Object> addUsageRageAdjustment(@PathVariable Long contractId, @Valid @RequestBody UsageRateAdjustmentDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        T contract = baseContractService.validateReference(contractId);
        UsageRateAdjustment result = usageRateAdjustmentService.create(contract, input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_USAGE_RATE_ADJUSTMENTS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @GetMapping(value = "{contractId}/adjustments/usage-rate", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<UsageRateAdjustmentProjection>>> getAllUsageRageAdjustment(@PathVariable Long contractId,
                                                                                                                  @RequestParam int page,
                                                                                                                  @RequestParam int size,
                                                                                                                  @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(usageRateAdjustmentService.findAll(pageable, search, buildContractSearchString(contractId, search)));
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @PostMapping(value = "{contractId}/adjustments/maximum")
    public ResponseEntity<Object> addUsageRageAdjustments(@PathVariable Long contractId, @Valid @RequestBody MaximumAdjustmentDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        T contract = baseContractService.validateReference(contractId);
        MaximumAdjustment result = maximumAdjustmentService.create(contract, input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_MAXIMUM_ADJUSTMENTS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @GetMapping(value = "{contractId}/adjustments/maximum", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<MaximumAdjustmentProjection>>> getAllMaximumAdjustments(@PathVariable Long contractId,
                                                                                                               @RequestParam int page,
                                                                                                               @RequestParam int size,
                                                                                                               @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(maximumAdjustmentService.findAll(pageable, search, buildContractSearchString(contractId, search)));
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @PostMapping(value = "{contractId}/adjustments/monetary")
    public ResponseEntity<Object> addMonetaryAdjustments(@PathVariable Long contractId, @Valid @RequestBody MonetaryAdjustmentDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        T contract = baseContractService.validateReference(contractId);
        MonetaryAdjustment result = monetaryAdjustmentService.create(contract, input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_MONETARY_ADJUSTMENTS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @GetMapping(value = "{contractId}/adjustments/monetary", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<MonetaryAdjustmentProjection>>> getAllMonetaryAdjustments(@PathVariable Long contractId,
                                                                                                                 @RequestParam int page,
                                                                                                                 @RequestParam int size,
                                                                                                                 @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(monetaryAdjustmentService.findAll(pageable, search, buildContractSearchString(contractId, search)));
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#contractId)")
    @GetMapping(value = "{contractId}/adjustments", params = {"page", "size"})
    public ResponseEntity<Object> getAllAdjustments(@PathVariable Long contractId,
                                                    @RequestParam int page,
                                                    @RequestParam int size,
                                                    @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContractAdjustmentProjection> currentPage = contractAdjustmentService.findAllContractAdjustments(pageable, buildContractSearchString(contractId, search));
        PageModelWrapper<EntityModel<ContractAdjustmentProjection>> contractPageModelWrapper = contractAdjustmentService.buildPageResource(currentPage, pageable, search);
        return ResponseEntity.ok(contractPageModelWrapper);
    }


    @PreAuthorize("@contractChecker.hasAccessToContract(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return ResponseEntity.ok(baseContractService.auditLogs(ContractProjection.class, objectId));
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@contractChecker.hasAccessToContract(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.deleteOne(objectId);
    }

    private String buildContractSearchString(Long contractId, String search) {
        return Utils.buildCompoundSearchString(String.format("contract:{id=%d}", contractId), search);
    }

}
