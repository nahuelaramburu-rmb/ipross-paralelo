package com.capacidad.validationapi.module.premedicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.encryption.EncryptionService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.premedicalauthorization.dto.BasePreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationQrResponseDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationResponseDTO;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorizationItem;
import com.capacidad.validationapi.module.premedicalauthorization.projection.PreMedicalAuthorizationProjection;
import com.capacidad.validationapi.module.premedicalauthorization.repository.PreMedicalAuthorizationRepository;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationBuilder;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationRoleSpecificationBuilder;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationValidator;
import com.capacidad.validationapi.module.render.service.RenderService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.ENCRYPTED_QR_KEY;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.TIMESTAMP;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PRE_MEDICAL_AUTHORIZATION_CANCELLED;
import static com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference.AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION;
import static com.capacidad.validationapi.module.medicalauthorization.service.impl.QRMedicalAuthorizationServiceImpl.QR_TYPE;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class PreMedicalAuthorizationServiceImpl extends BaseServiceImpl<PreMedicalAuthorization, PreMedicalAuthorizationDTO, Long> implements PreMedicalAuthorizationService {

    public static final String PRE_MEDICAL_AUTHORIZATION_CODE_KEY = "code";
    public static final String PRE_MEDICAL_AUTHORIZATION_TYPE = "preMedicalAuthorization";
    private final PreMedicalAuthorizationRepository preMedicalAuthorizationRepository;
    private final PreMedicalAuthorizationBuilder preMedicalAuthorizationBuilder;
    private final RenderService renderService;
    private final EncryptionService encryptionService;
    private final PreMedicalAuthorizationValidator preMedicalAuthorizationValidator;
    private final PreMedicalAuthorizationRoleSpecificationBuilder roleSpecificationBuilder;
    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public PreMedicalAuthorizationServiceImpl(PreMedicalAuthorizationRepository repository,
                                              PreMedicalAuthorizationBuilder preMedicalAuthorizationBuilder,
                                              RenderService renderService,
                                              EncryptionService encryptionService,
                                              PreMedicalAuthorizationValidator preMedicalAuthorizationValidator,
                                              PreMedicalAuthorizationRoleSpecificationBuilder roleSpecificationBuilder,
                                              BeneficiaryFinder beneficiaryFinder) {
        super(repository);
        this.preMedicalAuthorizationRepository = repository;
        this.preMedicalAuthorizationBuilder = preMedicalAuthorizationBuilder;
        this.renderService = renderService;
        this.encryptionService = encryptionService;
        this.preMedicalAuthorizationValidator = preMedicalAuthorizationValidator;
        this.roleSpecificationBuilder = roleSpecificationBuilder;
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public PreMedicalAuthorization create(PreMedicalAuthorizationDTO dto) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        PreMedicalAuthorization object = preMedicalAuthorizationBuilder.build(dto);
        return create(object);
    }

    private PreMedicalAuthorization create(PreMedicalAuthorization object) throws ObjectNotValidException, ObjectNotFoundException {
        this.validate(object);
        PreMedicalAuthorization result = preMedicalAuthorizationRepository.saveAndFlush(object);
        preMedicalAuthorizationRepository.refresh(result);
        return result;
    }

    @Override
    public <P extends BasePreMedicalAuthorizationDTO> PreMedicalAuthorizationResponseDTO createAndGenerateReceipt(P dto) throws ObjectNotValidException, ObjectNotFoundException {
        PreMedicalAuthorization object = preMedicalAuthorizationBuilder.build(dto);
        PreMedicalAuthorization created = create(object);
        return buildOutputStreamResponse(created);
    }

    private PreMedicalAuthorizationResponseDTO buildOutputStreamResponse(PreMedicalAuthorization preMedicalAuthorization) throws ObjectNotValidException {
        PreMedicalAuthorizationResponseDTO response = new PreMedicalAuthorizationResponseDTO();
        response.setOutputStream(generateReceipt(preMedicalAuthorization));
        response.setCode(preMedicalAuthorization.getCode());
        return response;
    }

    private ByteArrayOutputStream generateReceipt(PreMedicalAuthorization preMedicalAuthorization) throws ObjectNotValidException {
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("generatedBy", SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("-"));
        templateValues.put("generatedAt", LocalDateTime.now());
        templateValues.put("base64QrCode", generateQrCode(preMedicalAuthorization));
        templateValues.put("data", preMedicalAuthorization);
        return renderService.renderPDF("pre_medical_authorization", templateValues);
    }

    private String generateQrCode(PreMedicalAuthorization preMedicalAuthorization) throws ObjectNotValidException {
        ObjectNode content = this.getObjectMapper().createObjectNode();
        content.put(TIMESTAMP, preMedicalAuthorization.getExpirationDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        content.put(QR_TYPE, PRE_MEDICAL_AUTHORIZATION_TYPE);
        ObjectNode preMedAuth = this.getObjectMapper().createObjectNode();
        content.put(ENCRYPTED_QR_KEY, preMedicalAuthorization.getId().toString());
        preMedAuth.put(PRE_MEDICAL_AUTHORIZATION_CODE_KEY, preMedicalAuthorization.getCode());
        content.set(PRE_MEDICAL_AUTHORIZATION_TYPE, preMedAuth);
        String encryptedContent = encryptionService.encrypt(content.toString());
        return renderService.renderQrCode(encryptedContent);
    }

    @Override
    public <P extends BasePreMedicalAuthorizationDTO> PreMedicalAuthorizationQrResponseDTO createAndGetObjectResponse(P dto) throws ObjectNotValidException, ObjectNotFoundException {
        PreMedicalAuthorization object = preMedicalAuthorizationBuilder.build(dto);
        PreMedicalAuthorization created = create(object);
        return buildObjectResponse(created);
    }

    @Override
    public PreMedicalAuthorizationQrResponseDTO getObjectResponse(long preMedicalAuthorizationId) throws ObjectNotFoundException, ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = this.findById(preMedicalAuthorizationId);
        return buildObjectResponse(preMedicalAuthorization);
    }

    private PreMedicalAuthorizationQrResponseDTO buildObjectResponse(PreMedicalAuthorization preMedicalAuthorization) throws ObjectNotValidException {
        String qrCode = generateQrCode(preMedicalAuthorization);
        PreMedicalAuthorizationProjection projection = buildProjection(preMedicalAuthorization);
        PreMedicalAuthorizationQrResponseDTO responseDTO = new PreMedicalAuthorizationQrResponseDTO();
        responseDTO.setBeneficiary(projection.getBeneficiary());
        responseDTO.setPetitioner(projection.getPetitioner());
        responseDTO.setCode(projection.getCode());
        responseDTO.setPreMedicalAuthorizationItems(projection.getPreMedicalAuthorizationItems());
        responseDTO.setStatus(projection.getStatus());
        responseDTO.setQr(qrCode);
        return responseDTO;
    }

    private PreMedicalAuthorization findPreMedicalAuthorization(String preMedicalAuthorizationCode) throws ObjectNotFoundException {
        return preMedicalAuthorizationRepository
                .findByCode(preMedicalAuthorizationCode)
                .orElseThrow(() -> new ObjectNotFoundException("preMedicalAuthorization.notFound"));
    }

    private PreMedicalAuthorizationProjection buildProjection(PreMedicalAuthorization preMedicalAuthorization) {
        return this.getProjectionFactory().createProjection(PreMedicalAuthorizationProjection.class, preMedicalAuthorization);
    }

    @Override
    public PreMedicalAuthorizationProjection cancel(String preMedicalAuthorizationCode, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = findPreMedicalAuthorization(preMedicalAuthorizationCode);
        return cancel(preMedicalAuthorization, input.getCancellationReason());
    }

    @Override
    public PreMedicalAuthorizationProjection cancel(long preMedicalAuthorizationId, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = this.findById(preMedicalAuthorizationId);
        return cancel(preMedicalAuthorization, input.getCancellationReason());
    }

    private PreMedicalAuthorizationProjection cancel(PreMedicalAuthorization preMedicalAuthorization, String cancellationReason) throws ObjectNotValidException {
        preMedicalAuthorizationValidator.validateStatus(preMedicalAuthorization);
        Status cancelled = this.getUtils().getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_CANCELLED.getId());
        preMedicalAuthorization.setStatus(cancelled);
        preMedicalAuthorization.setCancellationReason(cancellationReason);
        PreMedicalAuthorization updated = preMedicalAuthorizationRepository.save(preMedicalAuthorization);
        return buildProjection(updated);
    }

    @Override
    public void processMedicalAuthorization(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        PreMedicalAuthorization selectedPreMedAuth = medicalAuthorization.getSelectedPreMedicalAuthorization();
        if (selectedPreMedAuth != null) {
            PreMedicalAuthorization preMedicalAuthorization = findPreMedicalAuthorization(selectedPreMedAuth.getCode());
            medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);
            preMedicalAuthorizationValidator.validate(medicalAuthorization);
            Status status = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);
            preMedicalAuthorization.setStatus(status);
            setChargeSubtotals(medicalAuthorization, preMedicalAuthorization);
            medicalAuthorization.setAuthorizationType(AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION.getInstance());
            preMedicalAuthorizationRepository.save(preMedicalAuthorization);
        }
    }

    private void setChargeSubtotals(MedicalAuthorization medicalAuthorization, PreMedicalAuthorization preMedicalAuthorization) {
        Map<Long, MedicalAuthorizationItem> validationItemMap = medicalAuthorization.getMedicalAuthorizationItems().stream()
                .collect(Collectors.toMap(i -> i.getNomenclator().getId(), Function.identity()));
        preMedicalAuthorization.getPreMedicalAuthorizationItems().forEach(i -> {
            MedicalAuthorizationItem medicalAuthorizationItem = validationItemMap.get(i.getNomenclator().getId());
            calculateAndSetItemChargeAndSubtotals(i, medicalAuthorizationItem);
        });
    }

    private void calculateAndSetItemChargeAndSubtotals(PreMedicalAuthorizationItem preMedicalAuthorizationItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (preMedicalAuthorizationItem.getChargeUnitPrice() != null &&
                medicalAuthorizationItem != null) {
            medicalAuthorizationItem.setChargeUnitPrice(preMedicalAuthorizationItem.getChargeUnitPrice());
            medicalAuthorizationItem.setChargeSubtotal(preMedicalAuthorizationItem.getChargeUnitPrice().multiply(BigDecimal.valueOf(medicalAuthorizationItem.getQuantity())));
        }
    }

    @Override
    public void processMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem, boolean auditing) throws ObjectNotValidException, ObjectNotFoundException {
        PreMedicalAuthorization preMedicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization().getPreMedicalAuthorization();
        if (preMedicalAuthorization != null) {
            PreMedicalAuthorizationItem preMedicalAuthorizationItem = preMedicalAuthorization.getPreMedicalAuthorizationItems().stream()
                    .filter(i -> i.getNomenclator().getId().equals(medicalAuthorizationItem.getNomenclator().getId()))
                    .findFirst()
                    .orElseThrow(() -> new ObjectNotFoundException("preMedicalAuthorization.itemDoesNotExists", medicalAuthorizationItem.getNomenclator().getNomenclatorCode()));
            preMedicalAuthorizationItem.setAuditing(auditing);
            if (medicalAuthorizationItem.isApproved()) {
                preMedicalAuthorizationValidator.determineItemConsumption(preMedicalAuthorizationItem, medicalAuthorizationItem);
                Status status = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);
                preMedicalAuthorization.setStatus(status);
            }
            preMedicalAuthorizationRepository.save(preMedicalAuthorization);
        }
    }

    @Override
    public void rollbackConsumptionFromMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        PreMedicalAuthorization preMedicalAuthorization = medicalAuthorization.getPreMedicalAuthorization();
        if (preMedicalAuthorization != null) {
            Map<Long, PreMedicalAuthorizationItem> validationMap = preMedicalAuthorization.getPreMedicalAuthorizationItems().stream()
                    .collect(Collectors.toMap(e -> e.getNomenclator().getId(), Function.identity()));
            medicalAuthorization.getMedicalAuthorizationItems()
                    .forEach(throwingConsumer(i -> {
                        Nomenclator nomenclator = i.getNomenclator();
                        if (validationMap.containsKey(nomenclator.getId())) {
                            PreMedicalAuthorizationItem preMedicalAuthorizationItem = validationMap.get(nomenclator.getId());
                            if (Boolean.FALSE.equals(preMedicalAuthorizationItem.getAuditing()) && i.isApproved())
                                preMedicalAuthorizationItem.setRemaining(preMedicalAuthorizationItem.getRemaining() + i.getQuantity());
                            if (Boolean.TRUE.equals(preMedicalAuthorizationItem.getAuditing()) && i.isPending())
                                preMedicalAuthorizationItem.setAuditing(false);
                        }
                    }));
            Status status = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);
            preMedicalAuthorization.setStatus(status);
            preMedicalAuthorizationRepository.save(preMedicalAuthorization);
        }
    }

    @Override
    public PreMedicalAuthorizationQrResponseDTO validateCodeAndBuildObjectResponse(String preMedicalAuthorizationCode) throws ObjectNotFoundException, ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = findPreMedicalAuthorization(preMedicalAuthorizationCode);
        return buildObjectResponse(preMedicalAuthorization);
    }

    @Override
    public PreMedicalAuthorization validateCode(String preMedicalAuthorizationCode) throws ObjectNotFoundException {
        return findPreMedicalAuthorization(preMedicalAuthorizationCode);
    }

    @Override
    public boolean existsByAuthBeneficiaryOrRelative(long preMedicalAuthorizationId) {
        return preMedicalAuthorizationRepository.existsByIdAndBeneficiaryResourceId(preMedicalAuthorizationId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                || preMedicalAuthorizationRepository.existsByIdAndBeneficiaryFamilyId(preMedicalAuthorizationId, beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
    }

    @Override
    public boolean existsByAuthBeneficiaryOrRelative(String code) {
        return preMedicalAuthorizationRepository.existsByCodeAndBeneficiaryResourceId(code, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                || preMedicalAuthorizationRepository.existsByCodeAndBeneficiaryFamilyId(code, beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
    }

    @Override
    public Optional<Specification<PreMedicalAuthorization>> appendCustomSpecification() {
        return roleSpecificationBuilder.buildSpecification();
    }

}
