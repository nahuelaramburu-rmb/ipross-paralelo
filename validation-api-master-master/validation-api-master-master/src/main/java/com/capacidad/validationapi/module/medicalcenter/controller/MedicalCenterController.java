package com.capacidad.validationapi.module.medicalcenter.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.medicalcenter.dto.MedicalCenterDTO;
import com.capacidad.validationapi.module.medicalcenter.projection.MedicalCenterProjection;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_AUTH;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_MEDICAL_CENTERS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_MEDICAL_CENTERS)
public class MedicalCenterController extends BaseControllerImpl<MedicalCenterDTO, Long> {

    private final MedicalCenterService medicalCenterService;

    @Autowired
    public MedicalCenterController(MedicalCenterService medicalCenterService) {
        super(medicalCenterService);
        this.medicalCenterService = medicalCenterService;
    }

    @GetMapping(value = ENDPOINT_AUTH)
    public ResponseEntity<SelfModel<MedicalCenterProjection, Long>> getAuthMedicalCenter() throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new SelfModel<>(medicalCenterService.getProjectedAuthMedicalCenter(), this.getClass()));
    }

    @GetMapping(params = "name")
    public ResponseEntity<CollectionModelWrapper<EntityModel<MedicalCenterProjection>>> getMedicalCentersByName(@RequestParam String name) {
        Set<MedicalCenterProjection> results = medicalCenterService.getMedicalCenters(name);
        ResourceAssembler<MedicalCenterProjection, Long> assembler = new ResourceAssembler<>(this.getClass());
        Link self = linkTo(methodOn(this.getClass()).getMedicalCentersByName(name)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_MEDICAL_CENTERS, assembler.toResources(results), self));
    }

}
