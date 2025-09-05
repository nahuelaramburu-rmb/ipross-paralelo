package com.capacidad.validationapi.module.beneficiary.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.*;
import com.capacidad.validationapi.module.beneficiary.dto.*;
import com.capacidad.validationapi.module.beneficiary.hateoas.BeneficiaryResource;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.Expiration;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryInsurancePlanProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.projection.ExpirationProjection;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeCompanyReport;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeMonthlyReport;
import com.capacidad.validationapi.module.beneficiary.service.*;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.importprocessor.model.ImportOperation;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import com.capacidad.validationapi.module.person.service.PersonService;
import com.capacidad.validationapi.module.render.misc.RenderUtils;
import com.capacidad.validationapi.module.tradeunion.controller.TradeUnionController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_BENEFICIARIES)
public class BeneficiaryController extends BaseControllerImpl<BeneficiaryDTO, Long> {

    private final BeneficiaryService beneficiaryService;
    private final ExpirationService expirationService;
    private final PersonService personService;
    private final LocaleHandler localeHandler;
    private final BeneficiaryExportService beneficiaryExportService;
    private final BeneficiaryInsurancePlanService beneficiaryInsurancePlanService;
    private final BeneficiaryImportService beneficiaryImportService;
    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public BeneficiaryController(BeneficiaryService abstractService,
                                 PersonService personService,
                                 ExpirationService expirationService,
                                 LocaleHandler localeHandler,
                                 BeneficiaryExportService beneficiaryExportService,
                                 BeneficiaryImportService beneficiaryImportService,
                                 BeneficiaryInsurancePlanService beneficiaryInsurancePlanService,
                                 BeneficiaryFinder beneficiaryFinder) {
        super(abstractService);
        this.beneficiaryService = abstractService;
        this.personService = personService;
        this.expirationService = expirationService;
        this.localeHandler = localeHandler;
        this.beneficiaryExportService = beneficiaryExportService;
        this.beneficiaryImportService = beneficiaryImportService;
        this.beneficiaryInsurancePlanService = beneficiaryInsurancePlanService;
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @CrossOrigin
    @PostMapping(value = "/import")
    public ResponseEntity<StreamingResponseBody> importBeneficiaries(@RequestParam String columns,
                                                                     @RequestParam int skipLines,
                                                                     @RequestParam(required = false) Character separator,
                                                                     @RequestParam(value = "operation") ImportOperation operation,
                                                                     @RequestParam(value = "file") MultipartFile file) {
        UUID tenantId = TenantContext.getTenant();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_STREAM_JSON);
        StreamingResponseBody stream = out -> {
            try {
                ImportProperties properties = new ImportProperties(Arrays.asList(columns.split(COMA)),
                        skipLines,
                        separator,
                        operation,
                        file,
                        out,
                        authentication,
                        tenantId
                );
                beneficiaryImportService.importMultipartFile(properties);
            } catch (ObjectNotValidException e) {
                throw new RuntimeException(e);
            }
        };
        return new ResponseEntity<>(stream, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/download-key")
    public ResponseEntity<Object> generateDownloadKey() {
        FileDownloadKey fileDownloadKey = beneficiaryExportService.generateDownloadKey();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Download-Key", fileDownloadKey.getKey());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/data", params = "key")
    public ResponseEntity<StreamingResponseBody> getData(HttpServletResponse response,
                                                         @RequestParam(required = false) String search,
                                                         @RequestParam String key) {
        String filename = localeHandler.getLocaleMessage("beneficiary.csv", LocaleContextHolder.getLocale()).orElse("file.csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        RenderUtils.initializeApplicationOctetStreamResponse(response, filename);
        StreamingResponseBody stream = out -> {
            try {
                beneficiaryExportService.exportStreamToCsv(search, key, out);
            } catch (ObjectNotValidException e) {
                throw new RuntimeException(e);
            }
        };
        return ResponseEntity.ok(stream);
    }

    @GetMapping(value = ENDPOINT_VERIFICATION, params = {"idNumber", "idType", "beneficiaryCode", "birthDate"})
    public ResponseEntity<BeneficiaryProjection.Verification> validateBeneficiary(@RequestParam Long idNumber,
                                                                                  @RequestParam String idType,
                                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                                                                                  @RequestParam String beneficiaryCode) throws ObjectNotFoundException, ObjectNotValidException {
        IdType idTypeDb = personService.getIdType(idType);
        BeneficiaryVerificationDTO beneficiaryVerificationDTO = BeneficiaryVerificationDTO.builder()
                .idNumber(idNumber)
                .idType(idTypeDb)
                .birthDate(birthDate)
                .beneficiaryCode(beneficiaryCode)
                .build();
        return ResponseEntity.ok(beneficiaryFinder.verifyBeneficiary(beneficiaryVerificationDTO));
    }

    @GetMapping(value = ENDPOINT_AUTH)
    public ResponseEntity<BeneficiaryResource> getAuthBeneficiary() throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new BeneficiaryResource(beneficiaryFinder.findAuthBeneficiaryProjected()));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "{beneficiaryId}/charges", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<BeneficiaryChargeMonthlyReport>> getMonthlyGroupedCharges(@PathVariable Long beneficiaryId,
                                                                                                     @RequestParam int page,
                                                                                                     @RequestParam int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(beneficiaryFinder.getAllChargesGroupedByMonth(beneficiaryId, pageRequest));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "{beneficiaryId}/company-charges", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<BeneficiaryChargeCompanyReport>> getCompanyGroupedCharges(@PathVariable Long beneficiaryId,
                                                                                                     @RequestParam int page,
                                                                                                     @RequestParam int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(beneficiaryFinder.getAllChargesGroupedByCompany(beneficiaryId, pageRequest));
    }

    @PreAuthorize("@beneficiaryChecker.canGetAll()")
    @GetMapping(params = {"idNumber", "idType"})
    public ResponseEntity<BeneficiaryResource> getBeneficiary(@RequestParam Long idNumber,
                                                              @RequestParam String idType) throws ObjectNotFoundException, ObjectNotValidException {
        IdType idTypeDb = personService.getIdType(idType);
        return ResponseEntity.ok(new BeneficiaryResource(beneficiaryFinder.findBeneficiaryProjected(idNumber, idTypeDb)));
    }

    @PreAuthorize("@beneficiaryChecker.canGetAll()")
    @GetMapping(params = {"beneficiaryCode"})
    public ResponseEntity<BeneficiaryResource> getBeneficiary(@RequestParam String beneficiaryCode) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new BeneficiaryResource(beneficiaryFinder.findBeneficiaryProjected(beneficiaryCode)));
    }

    @PreAuthorize("@beneficiaryChecker.canGetAll()")
    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(@RequestParam int page,
                                         @RequestParam int size,
                                         @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        PageModelWrapper<EntityModel<BeneficiaryProjection.Minor>> pageModelWrapper = beneficiaryFinder.findAll(pageable, BeneficiaryProjection.Minor.class, search);
        return ResponseEntity.ok(pageModelWrapper);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @PostMapping(value = "{beneficiaryId}/expirations")
    public ResponseEntity<Object> addExpiration(@PathVariable Long beneficiaryId, @Valid @RequestBody ExpirationDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        Expiration result = expirationService.create(input, beneficiaryService.validateReference(beneficiaryId));
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_EXPIRATIONS, ID_PATH))
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "{beneficiaryId}/expirations", params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<EntityModel<ExpirationProjection>>> getExpirations(@PathVariable Long beneficiaryId,
                                                                                              @RequestParam int page,
                                                                                              @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExpirationProjection> currentPage = expirationService.getBeneficiaryExpirations(beneficiaryId, pageable);
        ResourceAssembler<ExpirationProjection, Long> assembler = new ResourceAssembler<>(ExpirationController.class);
        CollectionModel<EntityModel<ExpirationProjection>> resources = new CollectionModel<>(assembler.toResources(currentPage.getContent()));
        PageModelAssembler<EntityModel<ExpirationProjection>> pageAssembler = new PageModelAssembler<>();
        return ResponseEntity.ok(pageAssembler.toPageResource(RESOURCE_EXPIRATIONS, resources, currentPage, pageable));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @PostMapping(value = "{beneficiaryId}/relatives")
    public ResponseEntity<Object> addRelative(@PathVariable Long beneficiaryId, @Valid @RequestBody BeneficiaryRelativeDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary relative = beneficiaryService.createRelative(beneficiaryId, input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(StringUtils.join(ENDPOINT_BENEFICIARIES, ID_PATH))
                .buildAndExpand(relative.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "{beneficiaryId}/relatives")
    public ResponseEntity<CollectionModelWrapper<EntityModel<BeneficiaryProjection>>> getRelatives(@PathVariable Long beneficiaryId) throws ObjectNotFoundException {
        List<BeneficiaryProjection> results = beneficiaryFinder.getRelatives(beneficiaryId);
        ResourceAssembler<BeneficiaryProjection, Long> assembler = new ResourceAssembler<>(this.getClass(), BeneficiaryResource.class);
        Link self = linkTo(methodOn(this.getClass()).getRelatives(beneficiaryId)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_RELATIVES, assembler.toResources(results), self));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "{beneficiaryId}/contact-info")
    public ResponseEntity<EntityModel<PersonDetailProjection>> getContactInfo(@PathVariable Long beneficiaryId) throws ObjectNotFoundException {
        EntityModel<PersonDetailProjection> resource = new EntityModel<>(beneficiaryFinder.findBeneficiaryPersonDetailProjected(beneficiaryId));
        resource.add(linkTo(methodOn(this.getClass()).getContactInfo(beneficiaryId)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "{beneficiaryId}/insurance-plans")
    public ResponseEntity<CollectionModelWrapper<EntityModel<BeneficiaryInsurancePlanProjection>>> getInsurancePlans(@PathVariable Long beneficiaryId) {
        Set<BeneficiaryInsurancePlanProjection> results = beneficiaryInsurancePlanService.getBeneficiaryInsurancePlans(beneficiaryId);
        ResourceAssembler<BeneficiaryInsurancePlanProjection, Long> assembler = new ResourceAssembler<>(BeneficiaryInsurancePlanController.class);
        Link self = linkTo(methodOn(this.getClass()).getInsurancePlans(beneficiaryId)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_INSURANCE_PLANS, assembler.toResources(results), self));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @PutMapping(value = "{beneficiaryId}/insurance-plans")
    public ResponseEntity<SelfModel<BeneficiaryInsurancePlanProjection, Long>> associateInsurancePlan(@PathVariable Long beneficiaryId, @Valid @RequestBody BeneficiaryInsurancePlanDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        BeneficiaryInsurancePlanProjection result = beneficiaryInsurancePlanService.create(input, beneficiaryService.validateReference(beneficiaryId));
        Link self = linkTo(methodOn(BeneficiaryInsurancePlanController.class).getOne(result.getId())).withSelfRel();
        SelfModel<BeneficiaryInsurancePlanProjection, Long> selfModel = new SelfModel<>(result, self);
        return ResponseEntity.ok(selfModel);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @GetMapping(value = "{beneficiaryId}/trade-unions")
    public ResponseEntity<CollectionModelWrapper<EntityModel<IdAndNameOnlyProjection>>> getTradeUnions(@PathVariable Long beneficiaryId) {
        Set<IdAndNameOnlyProjection> results = beneficiaryFinder.findAllTradeUnions(beneficiaryId);
        ResourceAssembler<IdAndNameOnlyProjection, Long> assembler = new ResourceAssembler<>(TradeUnionController.class);
        Link self = linkTo(methodOn(this.getClass()).getTradeUnions(beneficiaryId)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_TRADE_UNIONS, assembler.toResources(results), self));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @PutMapping(value = "{beneficiaryId}/trade-unions/{tradeUnionId}")
    public ResponseEntity<BeneficiaryResource> associateTradeUnion(@PathVariable Long beneficiaryId, @PathVariable Long tradeUnionId) throws ObjectNotFoundException, ObjectNotValidException {
        BeneficiaryProjection result = beneficiaryService.associateTradeUnion(beneficiaryId, tradeUnionId);
        return ResponseEntity.ok(new BeneficiaryResource(result));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#beneficiaryId)")
    @DeleteMapping(value = "{beneficiaryId}/trade-unions/{tradeUnionId}")
    public ResponseEntity<BeneficiaryResource> disassociateTradeUnion(@PathVariable Long beneficiaryId, @PathVariable Long tradeUnionId) throws ObjectNotFoundException, ObjectNotValidException {
        BeneficiaryProjection result = beneficiaryService.dissociateTradeUnion(beneficiaryId, tradeUnionId);
        return ResponseEntity.ok(new BeneficiaryResource(result));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#objectId)")
    @PutMapping(value = "{objectId}/status")
    public ResponseEntity<BeneficiaryResource> updateStatus(@PathVariable Long objectId, @Valid @RequestBody StatusUpdateDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new BeneficiaryResource(beneficiaryService.updateStatus(objectId, input)));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#objectId)")
    @PutMapping(value = "{objectId}/relationship")
    public ResponseEntity<BeneficiaryResource> updateRelationship(@PathVariable Long objectId, @Valid @RequestBody BeneficiaryRelationshipDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new BeneficiaryResource(beneficiaryService.updateRelationship(objectId, input)));
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiary(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotValidException, ObjectNotFoundException {
        return super.deleteOne(objectId);
    }

}

