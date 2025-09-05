package com.capacidad.validationapi.module.settlement.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import com.capacidad.validationapi.module.settlement.dto.SettlementDTO;
import com.capacidad.validationapi.module.settlement.dto.SettlementUpdateDTO;
import com.capacidad.validationapi.module.settlement.hateoas.SettlementResource;
import com.capacidad.validationapi.module.settlement.projection.SettlementItemProjection;
import com.capacidad.validationapi.module.settlement.projection.SettlementProjection;
import com.capacidad.validationapi.module.settlement.service.SettlementItemDetailedExportService;
import com.capacidad.validationapi.module.settlement.service.SettlementItemExportService;
import com.capacidad.validationapi.module.settlement.service.SettlementItemService;
import com.capacidad.validationapi.module.settlement.service.SettlementService;
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
import javax.validation.constraints.NotEmpty;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_SETTLEMENTS;

@RestController
@RequestMapping(value = ENDPOINT_SETTLEMENTS)
public class SettlementController extends BaseControllerImpl<SettlementDTO, Long> {

    private final SettlementService settlementService;
    private final SettlementItemService settlementItemService;
    private final LocaleHandler localeHandler;
    private final SettlementItemExportService settlementItemExportService;
    private final SettlementItemDetailedExportService settlementItemDetailedExportService;

    @Autowired
    public SettlementController(SettlementService settlementService,
                                SettlementItemService settlementItemService,
                                LocaleHandler localeHandler,
                                SettlementItemExportService settlementItemExportService,
                                SettlementItemDetailedExportService settlementItemDetailedExportService) {
        super(settlementService);
        this.settlementService = settlementService;
        this.settlementItemService = settlementItemService;
        this.localeHandler = localeHandler;
        this.settlementItemExportService = settlementItemExportService;
        this.settlementItemDetailedExportService = settlementItemDetailedExportService;
    }

    @GetMapping(value = "/download-key")
    public ResponseEntity<Object> generateDownloadKey() {
        FileDownloadKey fileDownloadKey = settlementItemExportService.generateDownloadKey();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Download-Key", fileDownloadKey.getKey());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/data", params = "key")
    public ResponseEntity<StreamingResponseBody> getData(HttpServletResponse response,
                                                         @RequestParam(required = false) String search,
                                                         @RequestParam String key) {

        String filename = localeHandler.getLocaleMessage("settlement.csv", LocaleContextHolder.getLocale()).orElse("file.csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        RenderUtils.initializeApplicationOctetStreamResponse(response, filename);
        StreamingResponseBody stream = out -> {
            try {
                settlementItemExportService.exportStreamToCsv(search, key, out);
            } catch (ObjectNotValidException e) {
                throw new RuntimeException(e);
            }
        };
        return ResponseEntity.ok(stream);
    }

    @GetMapping(value = "/data/detailed", params = "key")
    public ResponseEntity<StreamingResponseBody> getDataDetailed(HttpServletResponse response,
                                                                 @RequestParam(required = false) String search,
                                                                 @RequestParam String key) {

        String filename = localeHandler.getLocaleMessage("settlement_detailed.csv", LocaleContextHolder.getLocale()).orElse("file.csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        RenderUtils.initializeApplicationOctetStreamResponse(response, filename);
        StreamingResponseBody stream = out -> {
            try {
                settlementItemDetailedExportService.exportStreamToCsv(search, key, out);
            } catch (ObjectNotValidException e) {
                throw new RuntimeException(e);
            }
        };
        return ResponseEntity.ok(stream);
    }

    @PreAuthorize("@practitionerChecker.hasAccessToPractitioner(#input.practitioner.id)")
    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@Valid @RequestBody SettlementDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        SettlementProjection result = settlementService.createSettlement(input);
        return ResponseEntity.ok(new SettlementResource(result));
    }

    @PreAuthorize("@settlementChecker.hasAccessToSettlement(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(settlementService.findProjectedById(objectId));
    }

    @PreAuthorize("@settlementChecker.hasAccessToSettlement(#objectId)")
    @GetMapping(value = "{objectId}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        ByteArrayOutputStream receipt = settlementService.generateReceipt(objectId);
        String filename = localeHandler.getLocaleMessage("settlement.receipt", LocaleContextHolder.getLocale(), objectId.toString()).orElse("file.pdf");
        return RenderUtils.buildReceiptResponseEntity(receipt, filename);
    }

    @PreAuthorize("@settlementChecker.hasAccessToSettlement(#objectId)")
    @GetMapping(value = "{objectId}/receipt", params = "itemIds")
    public ResponseEntity<byte[]> getItemsReceipt(@PathVariable Long objectId, @RequestParam Set<Long> itemIds) throws ObjectNotValidException {
        ByteArrayOutputStream receipt = settlementService.generateItemsReceipt(objectId, itemIds);
        String filename = localeHandler.getLocaleMessage("settlement_partial.receipt", LocaleContextHolder.getLocale(), objectId.toString()).orElse("file.pdf");
        return RenderUtils.buildReceiptResponseEntity(receipt, filename);
    }

    @PreAuthorize("@settlementChecker.hasAccessToSettlement(#objectId)")
    @GetMapping(value = "{objectId}/receipt", params = "beneficiaryCode")
    public ResponseEntity<byte[]> getBeneficiaryReceipt(@PathVariable Long objectId, @RequestParam String beneficiaryCode) throws ObjectNotValidException {
        ByteArrayOutputStream receipt = settlementService.generateBeneficiaryItemsReceipt(objectId, beneficiaryCode);
        String filename = localeHandler.getLocaleMessage("settlement_partial.receipt", LocaleContextHolder.getLocale(), objectId.toString()).orElse("file.pdf");
        return RenderUtils.buildReceiptResponseEntity(receipt, filename);
    }

    @PreAuthorize("@settlementChecker.hasAccessToSettlement(#settlementId)")
    @GetMapping(value = "/{settlementId}/settlement-items")
    public ResponseEntity<PageModelWrapper<EntityModel<SettlementItemProjection>>> getSettlementItems(@PathVariable Long settlementId,
                                                                                                      @RequestParam int page,
                                                                                                      @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageModelWrapper<EntityModel<SettlementItemProjection>> currentPage = settlementItemService.findAll(pageable, "", buildSettlementItemSearchString(settlementId));
        return ResponseEntity.ok(currentPage);
    }

    @PreAuthorize("@settlementChecker.hasAccessToSettlement(#settlementId)")
    @PutMapping(value = "{settlementId}")
    public ResponseEntity<SettlementResource> closeSettlement(@PathVariable Long settlementId, @Valid @RequestBody SettlementUpdateDTO updateDTO) throws ObjectNotFoundException, ObjectNotValidException {
        SettlementProjection result = settlementService.updateSettlement(settlementId, updateDTO);
        SettlementResource settlementResource = new SettlementResource(result);
        return ResponseEntity.ok(settlementResource);
    }

    @PreAuthorize("@settlementChecker.hasAccessToSettlement(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        return super.deleteOne(objectId);
    }

    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    private String buildSettlementItemSearchString(Long settlementId) {
        return String.format("settlement:{id=%d}", settlementId);
    }

}
