package com.capacidad.validationapi.module.medicalcoverage.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.PageModelAssembler;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageDTO;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageItemDTO;
import com.capacidad.validationapi.module.medicalcoverage.hateoas.MedicalCoverageItemResource;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageItemProjection;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageItemService;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_MEDICAL_COVERAGES;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_MEDICAL_COVERAGE_ITEMS;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_MEDICAL_COVERAGE_ITEMS;

@RestController
@RequestMapping(value = ENDPOINT_MEDICAL_COVERAGES)
public class MedicalCoverageController extends BaseControllerImpl<MedicalCoverageDTO, Long> {

    private final MedicalCoverageItemService medicalCoverageItemService;
    private final MedicalCoverageService medicalCoverageService;

    public MedicalCoverageController(MedicalCoverageService medicalCoverageService,
                                     MedicalCoverageItemService medicalCoverageItemService) {
        super(medicalCoverageService);
        this.medicalCoverageItemService = medicalCoverageItemService;
        this.medicalCoverageService = medicalCoverageService;
    }

    @PostMapping(value = "{medicalCoverageId}/medical-coverage-items")
    public ResponseEntity<Object> addMedicalCoverageItem(@PathVariable Long medicalCoverageId, @Valid @RequestBody MedicalCoverageItemDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalCoverageItem result = medicalCoverageItemService.create(input, medicalCoverageService.validateReference(medicalCoverageId));
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_MEDICAL_COVERAGE_ITEMS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "{medicalCoverageId}/medical-coverage-items", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<MedicalCoverageItemProjection>>> getMedicalCoverageItems(@PathVariable Long medicalCoverageId,
                                                                                                                @RequestParam int page,
                                                                                                                @RequestParam int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<MedicalCoverageItemProjection> pageResult = medicalCoverageItemService.getMedicalCoverageItems(medicalCoverageId, pageable);
        ResourceAssembler<MedicalCoverageItemProjection, Long> assembler = new ResourceAssembler<>(MedicalCoverageItemController.class, MedicalCoverageItemResource.class);
        CollectionModel<EntityModel<MedicalCoverageItemProjection>> resources = new CollectionModel<>(assembler.toResources(pageResult.getContent()));
        PageModelAssembler<EntityModel<MedicalCoverageItemProjection>> pageAssembler = new PageModelAssembler<>();
        return ResponseEntity.ok(pageAssembler.toPageResource(RESOURCE_MEDICAL_COVERAGE_ITEMS, resources, pageResult, pageable));
    }

    @GetMapping
    @Override
    public ResponseEntity<Object> getAll(@RequestParam int page,
                                         @RequestParam int size,
                                         @RequestParam(required = false) String search) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody MedicalCoverageDTO input) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @GetMapping(value = "/charge-types")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllChargeTypes() {
        return ResponseEntity.ok(medicalCoverageService.getAllChargeTypes());
    }

}
