package com.capacidad.validationapi.module.budget.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.budget.projection.BudgetItemProjection;
import com.capacidad.validationapi.module.budget.projection.BudgetProjection;
import com.capacidad.validationapi.module.budget.service.BeneficiaryBudgetService;
import com.capacidad.validationapi.module.budget.service.BudgetItemService;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_BENEFICIARY_BUDGETS;

@RestController
@RequestMapping(value = ENDPOINT_BENEFICIARY_BUDGETS)
public class BeneficiaryBudgetController extends BaseControllerImpl<IdDTO<Long>, Long> {

    private final BeneficiaryBudgetService beneficiaryBudgetService;
    private final BudgetItemService budgetItemService;
    private final LocaleHandler localeHandler;

    @Autowired
    public BeneficiaryBudgetController(BeneficiaryBudgetService beneficiaryBudgetService,
                                       BudgetItemService budgetItemService,
                                       LocaleHandler localeHandler) {
        super(beneficiaryBudgetService);
        this.beneficiaryBudgetService = beneficiaryBudgetService;
        this.budgetItemService = budgetItemService;
        this.localeHandler = localeHandler;
    }

    @PreAuthorize("@budgetChecker.hasAccessToBeneficiaryBudget(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(beneficiaryBudgetService.findProjectedById(objectId));
    }

    @PreAuthorize("@budgetChecker.hasAccessToBeneficiaryBudget(#objectId)")
    @GetMapping(value = "{objectId}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        ByteArrayOutputStream receipt = beneficiaryBudgetService.generateReceipt(objectId);
        String filename = localeHandler.getLocaleMessage("beneficiaryBudget.receipt", LocaleContextHolder.getLocale(), objectId.toString()).orElse("file.pdf");
        return RenderUtils.buildReceiptResponseEntity(receipt, filename);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(params = {"page", "size", "beneficiaryId"})
    public ResponseEntity<PageModelWrapper<EntityModel<MedicalAuthorizationProjection>>> getAllBeneficiaryBudgets(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam Long beneficiaryId,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        String practitionerSearchString = buildBudgetBeneficiarySearchString(beneficiaryId, search);
        PageModelWrapper<EntityModel<MedicalAuthorizationProjection>> pageModelWrapper = beneficiaryBudgetService.findAll(pageable, search, practitionerSearchString);
        pageModelWrapper.addQueryParam("beneficiaryId", beneficiaryId.toString());
        return ResponseEntity.ok(pageModelWrapper);
    }

    @PreAuthorize("@budgetChecker.hasAccessToBeneficiaryBudget(#budgetId)")
    @GetMapping(value = "{budgetId}/budget-items", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<BudgetItemProjection>>> getBudgetItems(
            @RequestParam int page,
            @RequestParam int size,
            @PathVariable Long budgetId,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        PageModelWrapper<EntityModel<BudgetItemProjection>> currentPage = budgetItemService.findAll(pageable, search, buildBudgetItemSearchString(search, budgetId));
        return ResponseEntity.ok(currentPage);
    }

    @PreAuthorize("@budgetChecker.hasAccessToBeneficiaryBudget(#budgetId)")
    @PutMapping(value = "{budgetId}")
    public ResponseEntity<EntityModel<BudgetProjection>> closeBudget(@PathVariable Long budgetId) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody IdDTO<Long> input) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    private String buildBudgetBeneficiarySearchString(Long beneficiaryId, String search) {
        return Utils.buildCompoundSearchString(String.format("beneficiary:{id=%d}", beneficiaryId), search);
    }

    private String buildBudgetItemSearchString(String search, Long budgetId) {
        return Utils.buildCompoundSearchString(String.format("budget:{id=%d}", budgetId), search);
    }


}
