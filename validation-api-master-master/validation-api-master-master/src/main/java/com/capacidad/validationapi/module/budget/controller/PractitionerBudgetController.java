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
import com.capacidad.validationapi.module.budget.service.BudgetItemService;
import com.capacidad.validationapi.module.budget.service.BudgetService;
import com.capacidad.validationapi.module.budget.service.PractitionerBudgetService;
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

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PRACTITIONER_BUDGETS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_PRACTITIONER_BUDGETS)
public class PractitionerBudgetController extends BaseControllerImpl<IdDTO<Long>, Long> {

    private final BudgetService budgetService;
    private final PractitionerBudgetService practitionerBudgetService;
    private final BudgetItemService budgetItemService;
    private final LocaleHandler localeHandler;

    @Autowired
    public PractitionerBudgetController(BudgetService budgetService,
                                        PractitionerBudgetService practitionerBudgetService,
                                        BudgetItemService budgetItemService,
                                        LocaleHandler localeHandler) {
        super(practitionerBudgetService);
        this.budgetService = budgetService;
        this.practitionerBudgetService = practitionerBudgetService;
        this.budgetItemService = budgetItemService;
        this.localeHandler = localeHandler;
    }

    @PreAuthorize("@budgetChecker.hasAccessToPractitionerBudget(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(practitionerBudgetService.findProjectedById(objectId));
    }

    @PreAuthorize("@budgetChecker.hasAccessToPractitionerBudget(#objectId)")
    @GetMapping(value = "{objectId}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        ByteArrayOutputStream receipt = practitionerBudgetService.generateReceipt(objectId);
        String filename = localeHandler.getLocaleMessage("practitionerBudget.receipt", LocaleContextHolder.getLocale(), objectId.toString()).orElse("file.pdf");
        return RenderUtils.buildReceiptResponseEntity(receipt, filename);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(params = {"page", "size", "practitionerId"})
    public ResponseEntity<PageModelWrapper<EntityModel<MedicalAuthorizationProjection>>> getAllPractitionerBudgets(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam Long practitionerId,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        String practitionerSearchString = buildBudgetPractitionerSearchString(practitionerId, search);
        PageModelWrapper<EntityModel<MedicalAuthorizationProjection>> pageModelWrapper = practitionerBudgetService.findAll(pageable, search, practitionerSearchString);
        pageModelWrapper.addQueryParam("practitionerId", practitionerId.toString());
        return ResponseEntity.ok(pageModelWrapper);
    }

    @PreAuthorize("@budgetChecker.hasAccessToPractitionerBudget(#budgetId)")
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

    @PreAuthorize("@budgetChecker.hasAccessToPractitionerBudget(#budgetId)")
    @PutMapping(value = "{budgetId}")
    public ResponseEntity<EntityModel<BudgetProjection>> closeBudget(@PathVariable Long budgetId) throws ObjectNotValidException, ObjectNotFoundException {
        EntityModel<BudgetProjection> budgetResource = new EntityModel<>(budgetService.closeBudget(budgetId));
        budgetResource.add(linkTo(methodOn(this.getClass()).getOne(budgetId)).withSelfRel());
        return ResponseEntity.ok(budgetResource);
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

    private String buildBudgetPractitionerSearchString(Long practitionerId, String search) {
        return Utils.buildCompoundSearchString(String.format("practitioner:{id=%d}", practitionerId), search);
    }

    private String buildBudgetItemSearchString(String search, Long budgetId) {
        return Utils.buildCompoundSearchString(String.format("budget:{id=%d}", budgetId), search);
    }

}
