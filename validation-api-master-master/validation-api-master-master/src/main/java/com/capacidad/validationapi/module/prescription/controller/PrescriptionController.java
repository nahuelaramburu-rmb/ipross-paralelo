package com.capacidad.validationapi.module.prescription.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.prescription.dto.BulkPrescriptionDTO;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionDTO;
import com.capacidad.validationapi.module.prescription.hateoas.PrescriptionResource;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionProjection;
import com.capacidad.validationapi.module.prescription.service.PrescriptionItemExportService;
import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PRESCRIPTIONS;

@RestController
@RequestMapping(value = ENDPOINT_PRESCRIPTIONS)
public class PrescriptionController extends BaseControllerImpl<PrescriptionDTO, Long> {

    private final PrescriptionService prescriptionService;
    private final LocaleHandler localeHandler;
    private final PrescriptionItemExportService prescriptionItemExportService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService,
                                  LocaleHandler localeHandler,
                                  PrescriptionItemExportService prescriptionItemExportService) {
        super(prescriptionService);
        this.prescriptionService = prescriptionService;
        this.localeHandler = localeHandler;
        this.prescriptionItemExportService = prescriptionItemExportService;
    }

    @GetMapping(value =  "/download-key")
    public ResponseEntity<Object> generateDownloadKey(){
        FileDownloadKey fileDownloadKey = prescriptionItemExportService.generateDownloadKey();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Download-Key", fileDownloadKey.getKey());
        return new ResponseEntity<>(null,httpHeaders,HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/data", params = "key")
    public  ResponseEntity<StreamingResponseBody> getData(HttpServletResponse response,
                                                          @RequestParam(required = false) String search,
                                                          @RequestParam String key) {
        String filename = localeHandler.getLocaleMessage("prescription.csv", LocaleContextHolder.getLocale()).orElse("file.csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        RenderUtils.initializeApplicationOctetStreamResponse(response, filename);
        StreamingResponseBody stream = out -> {
            try {
                prescriptionItemExportService.exportStreamToCsv(search,key,out);
            } catch (ObjectNotValidException e) {
                throw new RuntimeException(e);
            }
        };
        return ResponseEntity.ok(stream);
    }

    @GetMapping(params = {"beneficiaryCode", "exchangeId"})
    public ResponseEntity<PrescriptionProjection.Integration> findPrescription(@RequestParam String beneficiaryCode, @RequestParam Long exchangeId) throws ObjectNotValidException, ObjectNotFoundException {
        PrescriptionProjection.Integration result = prescriptionService.findPrescription(beneficiaryCode, exchangeId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("@prescriptionChecker.hasAccessToPrescription(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#input.practitioner.id)")
    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody PrescriptionDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        return super.addOne(input);
    }

    @PreAuthorize("@prescriptionChecker.allPrescriptionsBelongsToSameValidPractitioner(#input.getPrescriptions())")
    @PostMapping(value = "/bulk")
    public ResponseEntity<Object> addAll(@Valid @RequestBody BulkPrescriptionDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        prescriptionService.createAll(input.getPrescriptions());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@prescriptionChecker.hasAccessToPrescription(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@prescriptionChecker.hasAccessToPrescription(#objectId)")
    @PutMapping(value = "{objectId}")
    public ResponseEntity<PrescriptionResource> cancelPrescription(@PathVariable Long objectId, @Valid @RequestBody CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new PrescriptionResource(prescriptionService.cancelPrescription(objectId, input)));
    }

    @PreAuthorize("@prescriptionChecker.hasAccessToPrescription(#objectId)")
    @GetMapping(value = "{objectId}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        ByteArrayOutputStream receipt = prescriptionService.generateReceipt(objectId);
        String filename = localeHandler.getLocaleMessage("prescription.receipt", LocaleContextHolder.getLocale(), objectId.toString()).orElse("file.pdf");
        return RenderUtils.buildReceiptResponseEntity(receipt, filename);
    }

    @Override
    public ResponseEntity<Object> updateOne(Long objectId, Map<String, Object> update) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    public ResponseEntity<Object> deleteOne(Long objectId) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

}
