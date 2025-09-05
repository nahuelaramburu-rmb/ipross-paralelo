package com.capacidad.validationapi.module.practitioner.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.contract.service.ContractService;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalcenter.controller.MedicalCenterController;
import com.capacidad.validationapi.module.medicalcenter.projection.MedicalCenterProjection;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import com.capacidad.validationapi.module.practitioner.dto.MedicalRegistrationDTO;
import com.capacidad.validationapi.module.practitioner.dto.PractitionerDTO;
import com.capacidad.validationapi.module.practitioner.hateoas.PractitionerResource;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import com.capacidad.validationapi.module.practitioner.projection.MedicalRegistrationProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.practitioner.service.MedicalRegistrationService;
import com.capacidad.validationapi.module.practitioner.service.MedicalSpecialtyService;
import com.capacidad.validationapi.module.practitioner.service.PractitionerExportService;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_AUTH;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_KEY;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_PRACTITIONERS)
public class PractitionerController extends BaseControllerImpl<PractitionerDTO, Long> {

    private final PractitionerService practitionerService;
    private final MedicalCenterService medicalCenterService;
    private final MedicalSpecialtyService medicalSpecialtyService;
    private final MedicalRegistrationService medicalRegistrationService;
    private final ContractService contractService;
    private final LocaleHandler localeHandler;
    private final PractitionerExportService practitionerExportService;

    @Autowired
    public PractitionerController(PractitionerService abstractService,
                                  MedicalCenterService medicalCenterService,
                                  MedicalSpecialtyService medicalSpecialtyService,
                                  MedicalRegistrationService medicalRegistrationService,
                                  ContractService contractService,
                                  LocaleHandler localeHandler,
                                  PractitionerExportService practitionerExportService) {
        super(abstractService);
        this.practitionerService = abstractService;
        this.medicalCenterService = medicalCenterService;
        this.medicalSpecialtyService = medicalSpecialtyService;
        this.medicalRegistrationService = medicalRegistrationService;
        this.contractService = contractService;
        this.localeHandler = localeHandler;
        this.practitionerExportService = practitionerExportService;
    }

    @GetMapping(value = "/download-key")
    public ResponseEntity<Object> generateDownloadKey() {
        FileDownloadKey fileDownloadKey = practitionerExportService.generateDownloadKey();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Download-Key", fileDownloadKey.getKey());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/data", params = "key")
    public ResponseEntity<StreamingResponseBody> getData(HttpServletResponse response,
                                                         @RequestParam(required = false) String search,
                                                         @RequestParam String key) {
        String filename = localeHandler.getLocaleMessage("practitioner.csv", LocaleContextHolder.getLocale()).orElse("file.csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        RenderUtils.initializeApplicationOctetStreamResponse(response, filename);
        StreamingResponseBody stream = out -> {
            try {
                practitionerExportService.exportStreamToCsv(search, key, out);
            } catch (ObjectNotValidException e) {
                throw new RuntimeException(e);
            }
        };
        return ResponseEntity.ok(stream);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @GetMapping(value = ENDPOINT_KEY)
    public ResponseEntity<String> getAuthPractitionerKey() throws ObjectNotFoundException {
        return ResponseEntity.ok(practitionerService.getAuthPractitionerKey());
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @GetMapping(value = ENDPOINT_AUTH)
    public ResponseEntity<PractitionerResource> getAuthPractitioner() throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerProjection result = practitionerService.getProjectedAuthPractitioner();
        return ResponseEntity.ok(new PractitionerResource(result));
    }

    @PreAuthorize("@practitionerChecker.canGetAll()")
    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getAll(page, size, search);
    }

    @GetMapping(params = "lastName")
    public ResponseEntity<CollectionModelWrapper<EntityModel<PractitionerProjection.Minor>>> findPractitioners(@RequestParam String lastName) {
        Set<PractitionerProjection.Minor> results = practitionerService.findAvailablePractitioners(lastName);
        ResourceAssembler<PractitionerProjection.Minor, Long> assembler = new ResourceAssembler<>(this.getClass());
        Link self = linkTo(methodOn(this.getClass()).findPractitioners(lastName)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_PRACTITIONERS, assembler.toResources(results), self));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(value = "{practitionerId}/medical-specialties")
    public ResponseEntity<Set<IdAndNameOnlyProjection>> getPractitionerMedicalSpecialties(@PathVariable Long practitionerId) {
        return ResponseEntity.ok(medicalSpecialtyService.getPractitionerMedicalSpecialties(practitionerId));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @PutMapping(value = "{practitionerId}/medical-specialties/{medicalSpecialtyId}")
    public ResponseEntity<PractitionerResource> addPractitionerMedicalSpecialty(@PathVariable Long practitionerId, @PathVariable Long medicalSpecialtyId) throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerProjection result = practitionerService.associateMedicalSpecialty(practitionerId, medicalSpecialtyId);
        return ResponseEntity.ok(new PractitionerResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @DeleteMapping(value = "{practitionerId}/medical-specialties/{medicalSpecialtyId}")
    public ResponseEntity<PractitionerResource> deletePractitionerMedicalSpecialty(@PathVariable Long practitionerId, @PathVariable Long medicalSpecialtyId) throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerProjection result = practitionerService.dissociateMedicalSpecialty(practitionerId, medicalSpecialtyId);
        return ResponseEntity.ok(new PractitionerResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(value = "{practitionerId}/contracts")
    public ResponseEntity<CollectionModelWrapper<EntityModel<IdAndNameOnlyProjection>>> getPractitionerContracts(@PathVariable Long practitionerId) {
        Set<IdAndNameOnlyProjection> results = contractService.getPractitionerContracts(practitionerId);
        ResourceAssembler<IdAndNameOnlyProjection, Long> assembler = new ResourceAssembler<>(ContractController.class);
        Link self = linkTo(methodOn(this.getClass()).getPractitionerContracts(practitionerId)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_CONTRACTS, assembler.toResources(results), self));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @PutMapping(value = "{practitionerId}/contracts/{contractId}")
    public ResponseEntity<PractitionerResource> addPractitionerContract(@PathVariable Long practitionerId, @PathVariable Long contractId) throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerProjection result = practitionerService.associateContract(practitionerId, contractId);
        return ResponseEntity.ok(new PractitionerResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @DeleteMapping(value = "{practitionerId}/contracts/{contractId}")
    public ResponseEntity<PractitionerResource> deletePractitionerContract(@PathVariable Long practitionerId, @PathVariable Long contractId) throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerProjection result = practitionerService.dissociateContract(practitionerId, contractId);
        return ResponseEntity.ok(new PractitionerResource(result));
    }

    @PreAuthorize("T(com.capacidad.validationapi.misc.SecurityUtils).isBeneficiary() or @practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(value = "{practitionerId}/medical-centers")
    public ResponseEntity<CollectionModelWrapper<EntityModel<MedicalCenterProjection.IdNameAndAddressProjection>>> getPractitionerMedicalCenters(@PathVariable Long practitionerId) {
        Set<MedicalCenterProjection.IdNameAndAddressProjection> results = medicalCenterService.getPractitionerMedicalCenters(practitionerId);
        ResourceAssembler<MedicalCenterProjection.IdNameAndAddressProjection, Long> assembler = new ResourceAssembler<>(MedicalCenterController.class);
        Link self = linkTo(methodOn(this.getClass()).getPractitionerMedicalCenters(practitionerId)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_MEDICAL_CENTERS, assembler.toResources(results), self));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @PutMapping(value = "{practitionerId}/medical-centers/{medicalCenterId}")
    public ResponseEntity<PractitionerResource> addPractitionerMedicalCenter(@PathVariable Long practitionerId, @PathVariable Long medicalCenterId) throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerProjection result = practitionerService.associateMedicalCenter(practitionerId, medicalCenterId);
        return ResponseEntity.ok(new PractitionerResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @DeleteMapping(value = "{practitionerId}/medical-centers/{medicalCenterId}")
    public ResponseEntity<PractitionerResource> deletePractitionerMedicalCenter(@PathVariable Long practitionerId, @PathVariable Long medicalCenterId) throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerProjection result = practitionerService.dissociateMedicalCenter(practitionerId, medicalCenterId);
        return ResponseEntity.ok(new PractitionerResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @PostMapping(value = "{practitionerId}/medical-registrations")
    public ResponseEntity<Object> addMedicalRegistration(@PathVariable Long practitionerId, @Valid @RequestBody MedicalRegistrationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalRegistration result = medicalRegistrationService.create(input, practitionerService.validateReference(practitionerId));
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ControllerEndpoints.ENDPOINT_MEDICAL_REGISTRATIONS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(value = "{practitionerId}/medical-registrations")
    public ResponseEntity<CollectionModelWrapper<EntityModel<MedicalRegistrationProjection>>> getMedicalRegistrations(@PathVariable Long practitionerId) {
        Set<MedicalRegistrationProjection> results = medicalRegistrationService.getMedicalRegistrations(practitionerId);
        ResourceAssembler<MedicalRegistrationProjection, Long> assembler = new ResourceAssembler<>(MedicalRegistrationController.class);
        Link self = linkTo(methodOn(this.getClass()).getMedicalRegistrations(practitionerId)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_MEDICAL_REGISTRATIONS, assembler.toResources(results), self));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(value = "{practitionerId}/contact-info")
    public ResponseEntity<EntityModel<PersonDetailProjection>> getContactInfo(@PathVariable Long practitionerId) throws ObjectNotFoundException {
        EntityModel<PersonDetailProjection> resource = new EntityModel<>(practitionerService.getPersonDetails(practitionerId));
        resource.add(linkTo(methodOn(this.getClass()).getContactInfo(practitionerId)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#objectId)")
    @PutMapping(value = "{objectId}/status")
    public ResponseEntity<PractitionerResource> updateStatus(@PathVariable Long objectId, @Valid @RequestBody StatusUpdateDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new PractitionerResource(practitionerService.updateStatus(objectId, input)));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        return super.deleteOne(objectId);
    }

}
