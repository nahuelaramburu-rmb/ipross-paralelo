package com.capacidad.validationapi.module.premedicalauthorization.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.base.controller.BaseController;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationQrResponseDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationResponseDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PrePopulatedPreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.projection.PreMedicalAuthorizationProjection;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PRE_AUTHORIZATIONS;

@RestController
@RequestMapping(value = ENDPOINT_PRE_AUTHORIZATIONS)
public class PreMedicalAuthorizationController implements BaseController<PreMedicalAuthorizationDTO, Long> {

    private final PreMedicalAuthorizationService preMedicalAuthorizationService;
    private final LocaleHandler localeHandler;

    @Autowired
    public PreMedicalAuthorizationController(PreMedicalAuthorizationService preMedicalAuthorizationService,
                                             LocaleHandler localeHandler) {
        this.preMedicalAuthorizationService = preMedicalAuthorizationService;
        this.localeHandler = localeHandler;
    }

    @PostMapping
    public ResponseEntity<byte[]> addPreMedicalAuthorization(@Valid @RequestBody PreMedicalAuthorizationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        PreMedicalAuthorizationResponseDTO result = preMedicalAuthorizationService.createAndGenerateReceipt(input);
        String filename = localeHandler.getLocaleMessage("preMedicalAuthorization.receipt", LocaleContextHolder.getLocale(), result.getCode(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))).orElse("file.pdf");
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("code", result.getCode());
        return RenderUtils.buildReceiptResponseEntity(result.getOutputStream(), filename, headers);
    }

    @PostMapping(value = "/pre-populated")
    public ResponseEntity<PreMedicalAuthorizationQrResponseDTO> addPrePopulatedPreMedicalAuthorization(@Valid @RequestBody PrePopulatedPreMedicalAuthorizationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        PreMedicalAuthorizationQrResponseDTO responseDTO = preMedicalAuthorizationService.createAndGetObjectResponse(input);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("@preMedicalAuthorizationChecker.hasAccessToPreMedicalAuthorization(#objectId)")
    @GetMapping(params = {"code"})
    public ResponseEntity<PreMedicalAuthorizationQrResponseDTO> validatePreMedicalAuthorizationCode(@RequestParam String code) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(preMedicalAuthorizationService.validateCodeAndBuildObjectResponse(code));
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(preMedicalAuthorizationService.findAll(pageable, search));
    }

    @PreAuthorize("@preMedicalAuthorizationChecker.hasAccessToPreMedicalAuthorization(#objectId)")
    @GetMapping(value = "/{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(preMedicalAuthorizationService.getObjectResponse(objectId));
    }

    @Override
    public ResponseEntity<Object> getAuditLogs(Long objectId) {
        return null;
    }

    @PutMapping(params = {"code"})
    public ResponseEntity<PreMedicalAuthorizationProjection> cancelPreMedicalAuthorization(@RequestParam String code, @Valid @RequestBody CancellationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(preMedicalAuthorizationService.cancel(code, input));
    }

    @PutMapping(value = "/{objectId}")
    public ResponseEntity<PreMedicalAuthorizationProjection> cancelPreMedicalAuthorization(@PathVariable Long objectId, @Valid @RequestBody CancellationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        return ResponseEntity.ok(preMedicalAuthorizationService.cancel(objectId, input));
    }

    @Override
    public ResponseEntity<Object> addOne(PreMedicalAuthorizationDTO input) {
        return null;
    }

    @Override
    public ResponseEntity<Object> updateOne(Long objectId, Map<String, Object> update) {
        return null;
    }

    @Override
    public ResponseEntity<Object> deleteOne(Long objectId) {
        return null;
    }

}
