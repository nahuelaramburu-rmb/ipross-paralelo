package com.capacidad.validationapi.module.organization.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.organization.dto.OrganizationDTO;
import com.capacidad.validationapi.module.organization.projection.OrganizationProjection;
import com.capacidad.validationapi.module.organization.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_AUTH;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_ORGANIZATIONS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_ORGANIZATIONS)
public class OrganizationController extends BaseControllerImpl<OrganizationDTO, Long> {

    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        super(organizationService);
        this.organizationService = organizationService;
    }

    @GetMapping(value = ENDPOINT_AUTH)
    public ResponseEntity<SelfModel<OrganizationProjection, Long>> getAuthOrganization() throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new SelfModel<>(organizationService.getProjectedAuthOrganization(), this.getClass()));
    }

    @GetMapping(params = "name")
    public ResponseEntity<CollectionModelWrapper<EntityModel<OrganizationProjection>>> findOrganizations(@RequestParam String name) {
        Set<OrganizationProjection> results = organizationService.findOrganizationsContaining(name);
        ResourceAssembler<OrganizationProjection, Long> assembler = new ResourceAssembler<>(OrganizationController.class);
        CollectionModelWrapper<EntityModel<OrganizationProjection>> resources = new CollectionModelWrapper<>(RESOURCE_ORGANIZATIONS, assembler.toResources(results));
        resources.add(linkTo(methodOn(this.getClass()).findOrganizations(name)).withSelfRel());
        return ResponseEntity.ok(resources);
    }

}
