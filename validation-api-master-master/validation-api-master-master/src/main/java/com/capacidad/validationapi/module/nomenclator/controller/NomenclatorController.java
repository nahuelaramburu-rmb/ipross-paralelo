package com.capacidad.validationapi.module.nomenclator.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.nomenclator.dto.MedicalPracticeDTO;
import com.capacidad.validationapi.module.nomenclator.dto.NomenclatorDTO;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorSearch;
import com.capacidad.validationapi.module.nomenclator.projection.MedicalPracticeProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.capacidad.validationapi.module.nomenclator.service.MedicalPracticeService;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_NOMENCLATORS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_NOMENCLATORS)
public class NomenclatorController extends BaseControllerImpl<NomenclatorDTO, Long> {

    private final NomenclatorService nomenclatorService;
    private final MedicalPracticeService medicalPracticeService;

    @Autowired
    public NomenclatorController(NomenclatorService abstractService, MedicalPracticeService medicalPracticeService) {
        super(abstractService);
        this.nomenclatorService = abstractService;
        this.medicalPracticeService = medicalPracticeService;
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<List<NomenclatorSearch>> searchNomenclatorsAndGroups(@RequestParam String name) {
        List<NomenclatorSearch> results = nomenclatorService.searchNomenclatorsAndGroups(name);
        return ResponseEntity.ok(results);
    }

    @GetMapping(params = {"medicalSpecialtyId"})
    public ResponseEntity<CollectionModelWrapper<EntityModel<NomenclatorProjection.Minor>>> getMedicalSpecialtyNomenclators(@RequestParam Long medicalSpecialtyId) throws ObjectNotFoundException {
        ResourceAssembler<NomenclatorProjection.Minor, Long> assembler = new ResourceAssembler<>(this.getClass());
        CollectionModelWrapper<EntityModel<NomenclatorProjection.Minor>> resources = new CollectionModelWrapper<>(RESOURCE_NOMENCLATORS, assembler.toResources(nomenclatorService.getMedicalSpecialtyNomenclators(medicalSpecialtyId)));
        resources.add(linkTo(methodOn(this.getClass()).getMedicalSpecialtyNomenclators(medicalSpecialtyId)).withSelfRel());
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/medical-practices/{id}")
    public ResponseEntity<EntityModel<MedicalPracticeProjection>> getMedicalPractice(@PathVariable Long id) throws ObjectNotFoundException {
        MedicalPracticeProjection practice = medicalPracticeService.getMedicalPractice(id);

        EntityModel<MedicalPracticeProjection> resource = new EntityModel<>(practice);
        resource.add(linkTo(methodOn(this.getClass()).getMedicalPractice(id)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    @GetMapping(value = "/medical-practices", params = {"name"})
    public ResponseEntity<List<IdAndNameOnlyProjection>> findMedicalPractices(@RequestParam String name) {
        return ResponseEntity.ok(medicalPracticeService.getMedicalPractices(name));
    }

    @GetMapping(value = "/medical-practices", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<MedicalPracticeProjection>>> findMedicalPractices(@RequestParam int page,
                                                                                                         @RequestParam int size,
                                                                                                         @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(medicalPracticeService.findAll(pageable, search));
    }

    @PostMapping(value = "/medical-practices")
    public ResponseEntity<EntityModel<MedicalPracticeProjection>> createMedicalPractice(@Valid @RequestBody MedicalPracticeDTO input) {
        MedicalPracticeProjection practice = medicalPracticeService.createNewPractice(input);

        EntityModel<MedicalPracticeProjection> resource = new EntityModel<>(practice);
        resource.add(linkTo(methodOn(this.getClass()).createMedicalPractice(input)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    @GetMapping(value = "/medical-practice-areas")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getMedicalPracticeAreas() {
        return ResponseEntity.ok(medicalPracticeService.getAllMedicalPracticeAreas());
    }

    @GetMapping(value = "/medical-practice-types")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllMedicalPracticeTypes() {
        return ResponseEntity.ok(medicalPracticeService.getAllMedicalPracticeTypes());
    }

    @PatchMapping(value = "/medical-practices/{id}")
    public ResponseEntity<EntityModel<MedicalPracticeProjection>> updatePractice(@PathVariable Long id, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(medicalPracticeService.update(update, id));
    }

    @DeleteMapping(value = "/medical-practices/{id}")
    public ResponseEntity<Object> deletePractice(@PathVariable Long id) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(medicalPracticeService.delete(id));
    }
}
