package com.capacidad.validationapi.module.practitioner.controller;

import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.general.dto.NameDTO;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerCategoryProjection;
import com.capacidad.validationapi.module.practitioner.service.PractitionerCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PRACTITIONER_CATEGORIES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_PRACTITIONER_CATEGORIES)
public class PractitionerCategoryController extends BaseControllerImpl<NameDTO, Long> {

    private final PractitionerCategoryService practitionerCategoryService;

    @Autowired
    public PractitionerCategoryController(PractitionerCategoryService practitionerCategoryService) {
        super(practitionerCategoryService);
        this.practitionerCategoryService = practitionerCategoryService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PractitionerCategoryProjection>>> getAll() {
        CollectionModel<EntityModel<PractitionerCategoryProjection>> resources = practitionerCategoryService.findAll(PractitionerCategoryProjection.class);
        resources.add(linkTo(methodOn(this.getClass()).getAll()).withSelfRel());
        return ResponseEntity.ok(resources);
    }
}
