package com.capacidad.validationapi.module.insuranceplan.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.insuranceplan.dto.InsurancePlanDTO;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlanType;
import com.capacidad.validationapi.module.insuranceplan.projection.InsurancePlanProjection;
import com.capacidad.validationapi.module.insuranceplan.service.InsurancePlanService;
import com.capacidad.validationapi.module.medicalcoverage.controller.MedicalCoverageController;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageDTO;
import com.capacidad.validationapi.module.medicalcoverage.hateoas.MedicalCoverageResource;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageProjection;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_MEDICAL_COVERAGES;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_MEDICAL_COVERAGES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_INSURANCE_PLANS)
public class InsurancePlanController extends BaseControllerImpl<InsurancePlanDTO, Long> {

    private final InsurancePlanService insurancePlanService;
    private final MedicalCoverageService medicalCoverageService;

    @Autowired
    public InsurancePlanController(InsurancePlanService abstractService,
                                   MedicalCoverageService medicalCoverageService) {
        super(abstractService);
        this.insurancePlanService = abstractService;
        this.medicalCoverageService = medicalCoverageService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<InsurancePlanProjection>>> getAllInsurancePlans() {
        CollectionModel<EntityModel<InsurancePlanProjection>> resources = insurancePlanService.findAll();
        resources.add(linkTo(methodOn(this.getClass()).getAllInsurancePlans()).withSelfRel());
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/insurance-plan-types")
    public ResponseEntity<List<InsurancePlanType>> getAllInsurancePlanTypes() {
        return ResponseEntity.ok(insurancePlanService.getAllInsurancePlanTypes());
    }

    @PostMapping(value = "{insurancePlanId}/medical-coverages")
    public ResponseEntity<Object> addMedicalCoverage(@PathVariable Long insurancePlanId, @Valid @RequestBody MedicalCoverageDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalCoverage result = medicalCoverageService.create(input, insurancePlanService.validateReference(insurancePlanId));
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_MEDICAL_COVERAGES, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "{insurancePlanId}/medical-coverages")
    public ResponseEntity<CollectionModelWrapper<EntityModel<MedicalCoverageProjection>>> getInsurancePlanMedicalCoverages(@PathVariable Long insurancePlanId) {
        Set<MedicalCoverageProjection> results = medicalCoverageService.getMedicalCoverages(insurancePlanId);
        ResourceAssembler<MedicalCoverageProjection, Long> assembler = new ResourceAssembler<>(MedicalCoverageController.class, MedicalCoverageResource.class);
        Link self = linkTo(methodOn(this.getClass()).getInsurancePlanMedicalCoverages(insurancePlanId)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_MEDICAL_COVERAGES, assembler.toResources(results), self));
    }

}
