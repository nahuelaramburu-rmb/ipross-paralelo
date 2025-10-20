package com.capacidad.validationapi.module.company.controller;

import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.company.dto.CompanyDTO;
import com.capacidad.validationapi.module.company.projection.CompanyProjection;
import com.capacidad.validationapi.module.company.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_COMPANIES;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_COMPANIES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_COMPANIES)
public class CompanyController extends BaseControllerImpl<CompanyDTO, Long> {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService abstractService) {
        super(abstractService);
        this.companyService = abstractService;
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<CollectionModelWrapper<EntityModel<CompanyProjection>>> getCompanies(@RequestParam String name) {
        Set<CompanyProjection> results = companyService.getCompanies(name);
        ResourceAssembler<CompanyProjection, Long> assembler = new ResourceAssembler<>(this.getClass());
        Link self = linkTo(methodOn(this.getClass()).getCompanies(name)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_COMPANIES, assembler.toResources(results), self));
    }
}
