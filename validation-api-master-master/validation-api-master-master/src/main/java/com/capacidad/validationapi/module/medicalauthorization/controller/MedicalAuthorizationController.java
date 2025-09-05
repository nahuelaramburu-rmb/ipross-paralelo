package com.capacidad.validationapi.module.medicalauthorization.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.BaseController;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.dto.*;
import com.capacidad.validationapi.module.medicalauthorization.hateoas.MedicalAuthorizationResource;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemExportService;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationMediator;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationService;
import com.capacidad.validationapi.module.medicalauthorization.service.QRMedicalAuthorizationService;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.rating.RatingDTO;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_MESSAGES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_AUTHORIZATIONS)
public class MedicalAuthorizationController implements BaseController<IdDTO<Long>, Long> {

    private final MedicalAuthorizationService medicalAuthorizationService;
    private final MedicalAuthorizationMediator medicalAuthorizationMediator;
    private final QRMedicalAuthorizationService qrMedicalAuthorizationService;
    private final LocaleHandler localeHandler;
    private final MedicalAuthorizationItemExportService medicalAuthorizationItemExportService;

    @Autowired
    public MedicalAuthorizationController(MedicalAuthorizationService medicalAuthorizationService,
                                          MedicalAuthorizationMediator medicalAuthorizationMediator,
                                          QRMedicalAuthorizationService qrMedicalAuthorizationService,
                                          LocaleHandler localeHandler,
                                          MedicalAuthorizationItemExportService medicalAuthorizationItemExportService) {
        this.medicalAuthorizationService = medicalAuthorizationService;
        this.medicalAuthorizationMediator = medicalAuthorizationMediator;
        this.qrMedicalAuthorizationService = qrMedicalAuthorizationService;
        this.localeHandler = localeHandler;
        this.medicalAuthorizationItemExportService = medicalAuthorizationItemExportService;
    }

    @GetMapping(value = "/download-key")
    public ResponseEntity<Object> generateDownloadKey() {
        FileDownloadKey fileDownloadKey = medicalAuthorizationItemExportService.generateDownloadKey();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Download-Key", fileDownloadKey.getKey());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/data", params = "key")
    public ResponseEntity<StreamingResponseBody> getData(HttpServletResponse response,
                                                         @RequestParam(required = false) String search,
                                                         @RequestParam String key) {
        String filename = localeHandler.getLocaleMessage("medicalAuthorization.csv", LocaleContextHolder.getLocale()).orElse("file.csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        RenderUtils.initializeApplicationOctetStreamResponse(response, filename);
        StreamingResponseBody stream = out -> {
            try {
                medicalAuthorizationItemExportService.exportStreamToCsv(search, key, out);
            } catch (ObjectNotValidException e) {
                throw new RuntimeException(e);
            }
        };
        return ResponseEntity.ok(stream);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(medicalAuthorizationService.findProjectedById(objectId));
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @GetMapping(value = "{objectId}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        ByteArrayOutputStream receipt = medicalAuthorizationService.generateReceipt(objectId);
        String filename = localeHandler.getLocaleMessage("medicalAuthorization.receipt", LocaleContextHolder.getLocale(), objectId.toString()).orElse("file.pdf");
        return RenderUtils.buildReceiptResponseEntity(receipt, filename);
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(medicalAuthorizationService.findAll(pageable, search));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#input.practitioner.id)")
    @PostMapping(value = "/id-authorization")
    public ResponseEntity<MedicalAuthorizationResource> addOne(@Valid @RequestBody IdMedicalAuthorizationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorizationProjection result = medicalAuthorizationMediator.apply(input);
        return ResponseEntity.ok(new MedicalAuthorizationResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#input.practitioner.id)")
    @PostMapping(value = "/magstripe-authorization")
    public ResponseEntity<MedicalAuthorizationResource> addOne(@Valid @RequestBody MagstripeMedicalAuthorizationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorizationProjection result = medicalAuthorizationMediator.apply(input);
        return ResponseEntity.ok(new MedicalAuthorizationResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#input.practitioner.id)")
    @PostMapping(value = "/otp-authorization")
    public ResponseEntity<MedicalAuthorizationResource> addOtpAuthorization(@Valid @RequestBody OTPMedicalAuthorizationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorizationProjection result = medicalAuthorizationMediator.apply(input);
        return ResponseEntity.ok(new MedicalAuthorizationResource(result));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#input.practitioner.id)")
    @PostMapping(value = "/qr-authorization")
    public ResponseEntity<MedicalAuthorizationResource> addQrAuthorization(@Valid @RequestBody QRMedicalAuthorizationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorizationProjection result = medicalAuthorizationMediator.apply(input);
        return ResponseEntity.ok(new MedicalAuthorizationResource(result));
    }

    /**
     * Should be DEPRECATED
     * Use getAll with practitioner:{id=x}
     */
    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(params = {"practitionerId", "page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<MedicalAuthorizationProjection>>> getPractitionerAuthorizations(
            @RequestParam Long practitionerId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        String practitionerSearchString = buildMedicalAuthorizationPractitionerSearchString(practitionerId, search);
        PageModelWrapper<EntityModel<MedicalAuthorizationProjection>> pageModelWrapper = medicalAuthorizationService.findAll(pageable, search, practitionerSearchString);
        pageModelWrapper.addQueryParam("practitionerId", practitionerId.toString());
        return ResponseEntity.ok(pageModelWrapper);
    }

    /**
     * Should be DEPRECATED
     * Use getAll with beneficiary:{id=x}
     */
    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(params = {"beneficiaryId", "page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<MedicalAuthorizationProjection>>> getBeneficiaryAuthorizations(
            @RequestParam Long beneficiaryId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        String beneficiarySearchString = buildMedicalAuthorizationBeneficiarySearchString(beneficiaryId, search);
        PageModelWrapper<EntityModel<MedicalAuthorizationProjection>> pageModelWrapper = medicalAuthorizationService.findAll(pageable, search, beneficiarySearchString);
        pageModelWrapper.addQueryParam("beneficiaryId", beneficiaryId.toString());
        return ResponseEntity.ok(pageModelWrapper);
    }

    @PutMapping(value = "/qr-authorization")
    public ResponseEntity<MedicalAuthorizationProjection.QR> resolveValidationCode(@Valid @RequestBody QrDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(qrMedicalAuthorizationService.resolveQrCode(input.getEncryptedQr()));
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @PutMapping(value = "{objectId}/status")
    public ResponseEntity<MedicalAuthorizationResource> cancelAuthorization(@PathVariable Long objectId, @Valid @RequestBody CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new MedicalAuthorizationResource(medicalAuthorizationService.cancelMedicalAuthorization(objectId, input)));
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @GetMapping(value = "{objectId}/diagnosis")
    public ResponseEntity<EntityModel<MedicalAuthorizationProjection.Diagnosis>> getDiagnosis(@PathVariable Long objectId) throws ObjectNotFoundException {
        EntityModel<MedicalAuthorizationProjection.Diagnosis> resource = new EntityModel<>(medicalAuthorizationService.getDiagnosis(objectId));
        resource.add(linkTo(methodOn(this.getClass()).getDiagnosis(objectId)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @PutMapping(value = "{objectId}/diagnosis")
    public ResponseEntity<EntityModel<MedicalAuthorizationProjection.Diagnosis>> updateAuthorizationDiagnosis(@PathVariable Long objectId, @Valid @RequestBody MedicalAuthorizationDiagnosisDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        EntityModel<MedicalAuthorizationProjection.Diagnosis> resource = new EntityModel<>(medicalAuthorizationService.updateAuthorizationDiagnosis(objectId, input));
        resource.add(linkTo(methodOn(this.getClass()).getDiagnosis(objectId)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @PutMapping(value = "{objectId}/rating")
    public ResponseEntity<MedicalAuthorizationResource> addRatingToAuthorization(@PathVariable Long objectId, @Valid @RequestBody RatingDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new MedicalAuthorizationResource(medicalAuthorizationService.addRating(objectId, input)));
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @GetMapping(value = "{objectId}/messages")
    public ResponseEntity<CollectionModelWrapper<Message>> getAllMessages(@PathVariable Long objectId) throws ObjectNotFoundException {
        CollectionModelWrapper<Message> resource = new CollectionModelWrapper<>(RESOURCE_MESSAGES, medicalAuthorizationService.dumpAllMessages(objectId));
        resource.add(linkTo(methodOn(this.getClass()).getAllMessages(objectId)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @PutMapping(value = "{objectId}/messages")
    public ResponseEntity<CollectionModelWrapper<Message>> addMessageToAuthorization(@PathVariable Long objectId, @Valid @RequestBody MessageDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        CollectionModelWrapper<Message> resource = new CollectionModelWrapper<>(RESOURCE_MESSAGES, medicalAuthorizationService.receiveMessage(objectId, input));
        resource.add(linkTo(methodOn(this.getClass()).getAllMessages(objectId)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return ResponseEntity.ok(medicalAuthorizationService.auditLogs(null, objectId));
    }

    @GetMapping(value = "/restriction-types")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getAllRestrictionTypes() {
        return ResponseEntity.ok(medicalAuthorizationService.getAllRestrictionTypes());
    }

    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, Map<String, Object> update) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody IdDTO<Long> input) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    private String buildMedicalAuthorizationPractitionerSearchString(Long practitionerId, String search) {
        return Utils.buildCompoundSearchString(String.format("practitioner:{id=%d}", practitionerId), search);
    }

    private String buildMedicalAuthorizationBeneficiarySearchString(Long beneficiaryId, String search) {
        return Utils.buildCompoundSearchString(String.format("beneficiary:{id=%d}", beneficiaryId), search);
    }

}
