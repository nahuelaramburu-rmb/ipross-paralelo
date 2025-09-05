package com.capacidad.validationapi.module.storage.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.event.NewAttachmentEvent;
import com.capacidad.validationapi.module.storage.event.NewReportEvent;
import com.capacidad.validationapi.module.storage.model.FileEncoding;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.model.MultipartFileWrapper;
import com.capacidad.validationapi.module.storage.service.StorageService;
import com.capacidad.validationapi.module.storage.service.StorageServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_STORAGE;

@RestController
@RequestMapping(value = ENDPOINT_STORAGE)
public class StorageController {

    private final StorageService storageService;
    private final StorageServiceValidator storageServiceValidator;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public StorageController(StorageService storageService,
                             StorageServiceValidator storageServiceValidator,
                             ApplicationEventPublisher applicationEventPublisher) {
        this.storageService = storageService;
        this.storageServiceValidator = storageServiceValidator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @GetMapping(value = "/signatures", params = {"authorizationId"})
    public ResponseEntity<byte[]> retrieveSignature(@RequestParam Long authorizationId) throws ObjectNotValidException {
        byte[] image = storageService.retrieveFileAsByteArray(FileType.SIGNATURE, authorizationId, null);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Base64.getEncoder().encode(image));
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @PostMapping(value = "/reports", params = {"authorizationId"})
    public ResponseEntity<Object> storeReport(@RequestParam(value = "file") MultipartFile file, @RequestParam Long authorizationId) throws ObjectNotValidException, ObjectNotFoundException {
        storageServiceValidator.validateReport(authorizationId);
        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper(file, authorizationId);
        storageService.storeFile(FileType.REPORT, multipartFileWrapper, false);
        applicationEventPublisher.publishEvent(new NewReportEvent(authorizationId, file.getOriginalFilename(), TenantContext.getTenant()));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @GetMapping(value = "/reports", params = {"authorizationId", "filename"})
    public ResponseEntity<byte[]> retrieveReport(@RequestParam Long authorizationId, @RequestParam String filename) throws ObjectNotValidException {
        FileDTO file = storageService.retrieveFileAsDTO(FileType.REPORT, authorizationId, filename);
        return RenderUtils.buildFileResponseEntity(file);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @DeleteMapping(value = "/reports", params = {"authorizationId", "filename"})
    public ResponseEntity<Object> deleteReport(@RequestParam Long authorizationId, @RequestParam String filename) throws ObjectNotValidException, ObjectNotFoundException {
        storageServiceValidator.validateReport(authorizationId);
        storageService.deleteFileAsync(FileType.REPORT, authorizationId, filename);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @GetMapping(value = "/reports", params = {"authorizationId"})
    public ResponseEntity<List<SummaryDTO>> retrieveReportList(@RequestParam Long authorizationId) throws ObjectNotValidException {
        return ResponseEntity.ok().body(storageService.getFileList(FileType.REPORT, authorizationId));
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @GetMapping(value = "/authorizations", params = {"authorizationId"})
    public ResponseEntity<List<SummaryDTO>> retrieveAuthorizationFileList(@RequestParam Long authorizationId) {
        return ResponseEntity.ok().body(storageService.getFileList(Arrays.asList(FileType.REPORT, FileType.ATTACHMENT), authorizationId));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#input.relatedId)")
    @PostMapping(value = "/beneficiary/profile")
    public ResponseEntity<Object> storeBeneficiaryProfile(@RequestBody FileDTO input) throws ObjectNotValidException {
        input.setFileEncoding(FileEncoding.PNG);
        storageService.storeFile(FileType.BENEFICIARY_PROFILE, input, false);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "/beneficiary/profile", params = {"beneficiaryId"})
    public ResponseEntity<byte[]> retrieveBeneficiaryProfile(@RequestParam Long beneficiaryId) throws ObjectNotValidException {
        byte[] image = storageService.retrieveFileAsByteArray(FileType.BENEFICIARY_PROFILE, beneficiaryId, null);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Base64.getEncoder().encode(image));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @DeleteMapping(value = "/beneficiary/profile", params = {"beneficiaryId"})
    public ResponseEntity<Object> deleteBeneficiaryProfile(@RequestParam Long beneficiaryId) throws ObjectNotValidException {
        storageService.deleteFileAsync(FileType.BENEFICIARY_PROFILE, beneficiaryId, null);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#input.relatedId)")
    @PostMapping(value = "/practitioner/profile")
    public ResponseEntity<Object> storePractitionerProfile(@RequestBody FileDTO input) throws ObjectNotValidException {
        input.setFileEncoding(FileEncoding.PNG);
        storageService.storeFile(FileType.PRACTITIONER_PROFILE, input, false);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @GetMapping(value = "/practitioner/profile", params = {"practitionerId"})
    public ResponseEntity<byte[]> retrievePractitionerProfile(@RequestParam Long practitionerId) throws ObjectNotValidException {
        byte[] image = storageService.retrieveFileAsByteArray(FileType.PRACTITIONER_PROFILE, practitionerId, null);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Base64.getEncoder().encode(image));
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#practitionerId)")
    @DeleteMapping(value = "/practitioner/profile", params = {"practitionerId"})
    public ResponseEntity<Object> deletePractitionerProfile(@RequestParam Long practitionerId) throws ObjectNotValidException {
        storageService.deleteFileAsync(FileType.PRACTITIONER_PROFILE, practitionerId, null);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @PostMapping(value = "/attachments", params = {"authorizationId"})
    public ResponseEntity<Object> storeAttachment(@RequestParam(value = "file") MultipartFile file, @RequestParam Long authorizationId) throws ObjectNotValidException, ObjectNotFoundException {
        storageServiceValidator.validateAttachment(authorizationId);
        MultipartFileWrapper multipartFileWrapper = new MultipartFileWrapper(file, authorizationId);
        storageService.storeFile(FileType.ATTACHMENT, multipartFileWrapper, false);
        applicationEventPublisher.publishEvent(new NewAttachmentEvent(authorizationId, file.getOriginalFilename(), TenantContext.getTenant()));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @GetMapping(value = "/attachments", params = {"authorizationId", "filename"})
    public ResponseEntity<byte[]> retrieveAttachment(@RequestParam Long authorizationId, @RequestParam String filename) throws ObjectNotValidException {
        FileDTO file = storageService.retrieveFileAsDTO(FileType.ATTACHMENT, authorizationId, filename);
        return RenderUtils.buildFileResponseEntity(file);
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @DeleteMapping(value = "/attachments", params = {"authorizationId", "filename"})
    public ResponseEntity<Object> deleteAttachments(@RequestParam Long authorizationId, @RequestParam String filename) throws ObjectNotValidException, ObjectNotFoundException {
        storageServiceValidator.validateAttachment(authorizationId);
        storageService.deleteFileAsync(FileType.ATTACHMENT, authorizationId, filename);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#authorizationId)")
    @GetMapping(value = "/attachments", params = {"authorizationId"})
    public ResponseEntity<List<SummaryDTO>> retrieveAttachmentList(@RequestParam Long authorizationId) throws ObjectNotValidException {
        return ResponseEntity.ok().body(storageService.getFileList(FileType.ATTACHMENT, authorizationId));
    }

}
