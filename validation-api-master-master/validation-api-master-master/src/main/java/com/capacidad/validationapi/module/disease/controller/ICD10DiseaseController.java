package com.capacidad.validationapi.module.disease.controller;

import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import com.capacidad.validationapi.module.disease.service.ICD10DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_ICD10DISEASES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/diseases/icd10")
public class ICD10DiseaseController extends ReducedBaseControllerImpl<IdDTO<Long>, Long> {

    private final ICD10DiseaseService icd10DiseaseService;

    @Autowired
    public ICD10DiseaseController(ICD10DiseaseService icd10DiseaseService) {
        super(icd10DiseaseService);
        this.icd10DiseaseService = icd10DiseaseService;
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<CollectionModelWrapper<EntityModel<ICD10DiseaseProjection>>> getICD10Diseases(@RequestParam String name) {
        Set<ICD10DiseaseProjection> results = icd10DiseaseService.findICD10Diseases(name);
        ResourceAssembler<ICD10DiseaseProjection, Long> assembler = new ResourceAssembler<>(this.getClass());
        Link self = linkTo(methodOn(this.getClass()).getICD10Diseases(name)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_ICD10DISEASES, assembler.toResources(results), self));
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

}
