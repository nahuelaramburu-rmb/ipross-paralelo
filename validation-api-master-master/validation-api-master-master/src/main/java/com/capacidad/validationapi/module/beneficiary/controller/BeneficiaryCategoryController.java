package com.capacidad.validationapi.module.beneficiary.controller;

import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryCategoryProjection;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryCategoryService;
import com.capacidad.validationapi.module.general.dto.NameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_BENEFICIARY_CATEGORIES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_BENEFICIARY_CATEGORIES)
public class BeneficiaryCategoryController extends BaseControllerImpl<NameDTO, Long> {

    private final BeneficiaryCategoryService beneficiaryCategoryService;

    @Autowired
    public BeneficiaryCategoryController(BeneficiaryCategoryService beneficiaryCategoryService) {
        super(beneficiaryCategoryService);
        this.beneficiaryCategoryService = beneficiaryCategoryService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<BeneficiaryCategoryProjection>>> getAll() {
        CollectionModel<EntityModel<BeneficiaryCategoryProjection>> resources = beneficiaryCategoryService.findAll(BeneficiaryCategoryProjection.class);
        resources.add(linkTo(methodOn(this.getClass()).getAll()).withSelfRel());
        return ResponseEntity.ok(resources);
    }

}
