package com.capacidad.validationapi.module.audittray.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.dto.AuditTrayDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.hateoas.AuditTrayResource;
import com.capacidad.validationapi.module.audittray.projection.AuditTrayProjection;
import com.capacidad.validationapi.module.audittray.projection.AuditorProjection;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.PageModelAssembler;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.nomenclator.controller.NomenclatorController;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_AUDIT_TRAYS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDITORS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_NOMENCLATORS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_AUDIT_TRAYS)
public class AuditTrayController extends BaseControllerImpl<AuditTrayDTO, Long> {

    private final AuditTrayService auditTrayService;

    @Autowired
    public AuditTrayController(AuditTrayService auditTrayService) {
        super(auditTrayService);
        this.auditTrayService = auditTrayService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AuditTrayProjection>>> getAllAvailableTrays() {
        CollectionModel<EntityModel<AuditTrayProjection>> resources = auditTrayService.findAll();
        resources.add(linkTo(methodOn(this.getClass()).getAllAvailableTrays()).withSelfRel());
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(auditTrayService.findProjectedById(objectId, AuditTrayProjection.Extended.class));
    }

    @GetMapping(value = "/{auditTrayId}/nomenclators", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<NomenclatorProjection.Minor>>> getAuditTrayNomenclators(@PathVariable Long auditTrayId,
                                                                                                               @RequestParam int page,
                                                                                                               @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NomenclatorProjection.Minor> pageResult = auditTrayService.getAuditTrayNomenclators(auditTrayId, pageable);
        ResourceAssembler<NomenclatorProjection.Minor, Long> assembler = new ResourceAssembler<>(NomenclatorController.class);
        CollectionModel<EntityModel<NomenclatorProjection.Minor>> resources = new CollectionModel<>(assembler.toResources(pageResult.getContent()));
        PageModelAssembler<EntityModel<NomenclatorProjection.Minor>> pageAssembler = new PageModelAssembler<>();
        return ResponseEntity.ok(pageAssembler.toPageResource(RESOURCE_NOMENCLATORS, resources, pageResult, pageable));
    }

    @PutMapping(value = "/{auditTrayId}/nomenclators/{nomenclatorId}")
    public ResponseEntity<AuditTrayResource> addNomenclator(@PathVariable Long auditTrayId, @PathVariable Long nomenclatorId) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(new AuditTrayResource(auditTrayService.addNomenclator(auditTrayId, nomenclatorId)));
    }

    @DeleteMapping(value = "/{auditTrayId}/nomenclators/{nomenclatorId}")
    public ResponseEntity<AuditTrayResource> removeNomenclator(@PathVariable Long auditTrayId, @PathVariable Long nomenclatorId) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(new AuditTrayResource(auditTrayService.removeNomenclator(auditTrayId, nomenclatorId)));
    }

    @GetMapping(value = "/{auditTrayId}/auditors", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<AuditorProjection>>> getAuditTrayAuditors(@PathVariable Long auditTrayId,
                                                                                                 @RequestParam int page,
                                                                                                 @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditorProjection> pageResult = auditTrayService.getAuditTrayAuditors(auditTrayId, pageable);
        ResourceAssembler<AuditorProjection, Long> assembler = new ResourceAssembler<>(AuditorController.class);
        CollectionModel<EntityModel<AuditorProjection>> resources = new CollectionModel<>(assembler.toResources(pageResult.getContent()));
        PageModelAssembler<EntityModel<AuditorProjection>> pageAssembler = new PageModelAssembler<>();
        return ResponseEntity.ok(pageAssembler.toPageResource(RESOURCE_AUDITORS, resources, pageResult, pageable));
    }

    @PostMapping(value = "/{auditTrayId}/auditors")
    public ResponseEntity<AuditTrayResource> addAuditor(@PathVariable Long auditTrayId, @Valid @RequestBody AuditorDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(new AuditTrayResource(auditTrayService.addAuditor(auditTrayId, input)));
    }

    @DeleteMapping(value = "/{auditTrayId}/auditors/{auditorId}")
    public ResponseEntity<AuditTrayResource> removeAuditor(@PathVariable Long auditTrayId, @PathVariable Long auditorId) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(new AuditTrayResource(auditTrayService.removeAuditor(auditTrayId, auditorId)));
    }

}
