package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.encryption.EncryptionService;
import com.capacidad.validationapi.module.medicalauthorization.dto.QRMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.model.QRMedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalauthorization.repository.QRMedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.QRMedicalAuthorizationService;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.ENCRYPTED_QR_KEY;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.TIMESTAMP;
import static com.capacidad.validationapi.module.premedicalauthorization.service.impl.PreMedicalAuthorizationServiceImpl.PRE_MEDICAL_AUTHORIZATION_TYPE;

@Log4j2
@Service
public class QRMedicalAuthorizationServiceImpl extends BaseMedicalAuthorizationServiceImpl<QRMedicalAuthorization, QRMedicalAuthorizationDTO> implements QRMedicalAuthorizationService {

    public static final String QR_TYPE = "type";
    public static final String BENEFICIARY_TYPE = "beneficiary";
    private static final Integer QR_DURATION = 10;
    private final QRMedicalAuthorizationRepository qrMedicalAuthorizationRepository;
    private final EncryptionService encryptionService;
    private final PreMedicalAuthorizationService preMedicalAuthorizationService;
    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public QRMedicalAuthorizationServiceImpl(QRMedicalAuthorizationRepository repository,
                                             EncryptionService encryptionService,
                                             PreMedicalAuthorizationService preMedicalAuthorizationService,
                                             BeneficiaryFinder beneficiaryFinder) {
        super(repository);
        this.qrMedicalAuthorizationRepository = repository;
        this.encryptionService = encryptionService;
        this.preMedicalAuthorizationService = preMedicalAuthorizationService;
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public QRMedicalAuthorization initialize(QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        QRMedicalAuthorization initializedQrMedAuth = initializeDataFromValidatedQr(qrMedicalAuthorizationDTO);
        this.validate(initializedQrMedAuth);
        initializedQrMedAuth.setAuthorizationType(AuthorizationTypeReference.AUTHORIZATION_TYPE_AUTOMATIC_QR.getInstance());
        return initializedQrMedAuth;
    }

    private QRMedicalAuthorization initializeDataFromValidatedQr(QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO) throws ObjectNotFoundException, ObjectNotValidException {
        QRMedicalAuthorization initializedInput = super.initialize(qrMedicalAuthorizationDTO);
        QRMedicalAuthorization qrMedicalAuthorization = initializeDataFromValidatedQr(qrMedicalAuthorizationDTO.getEncryptedQr());
        initializedInput.setBeneficiary(qrMedicalAuthorization.getBeneficiary());
        initializedInput.setPreMedicalAuthorization(qrMedicalAuthorization.getPreMedicalAuthorization());
        initializedInput.setEncryptedQrKey(qrMedicalAuthorization.getEncryptedQrKey());
        if (qrMedicalAuthorization.getType().equals(PRE_MEDICAL_AUTHORIZATION_TYPE))
            initializedInput.setPetitioner(qrMedicalAuthorization.getPetitioner());
        return initializedInput;
    }

    private QRMedicalAuthorization initializeDataFromValidatedQr(String encryptedQr) throws ObjectNotFoundException, ObjectNotValidException {
        log.debug("Original Encrypted QR Content: {}", encryptedQr);
        String formattedQr = StringUtils.remove(encryptedQr, "\n");
        String decryptedQR = encryptionService.decrypt(formattedQr);
        QRMedicalAuthorization mappedQrMedicalAuthorization = mapMedicalAuthorizationFromQr(decryptedQR);
        switch (mappedQrMedicalAuthorization.getType()) {
            case BENEFICIARY_TYPE:
                parseAndInitializeBeneficiaryQr(mappedQrMedicalAuthorization);
                break;
            case PRE_MEDICAL_AUTHORIZATION_TYPE:
                parseAndInitializePreAuthorizationQr(mappedQrMedicalAuthorization);
                break;
            default:
                throw new ObjectNotValidException("medicalAuthorization.invalidQrType");
        }
        return mappedQrMedicalAuthorization;
    }

    private void parseAndInitializeBeneficiaryQr(QRMedicalAuthorization qrMedicalAuthorization) throws ObjectNotFoundException {
        Beneficiary beneficiary = qrMedicalAuthorization.getBeneficiary();
        Beneficiary initializedBeneficiary = beneficiaryFinder.findBeneficiaryLocked(beneficiary.getIdNumber(), beneficiary.getIdType());
        qrMedicalAuthorization.setBeneficiary(initializedBeneficiary);
    }

    /*Beneficiary not locked by pessimistic_write*/
    private void parseAndInitializePreAuthorizationQr(QRMedicalAuthorization qrMedicalAuthorization) throws ObjectNotFoundException, ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = qrMedicalAuthorization.getPreMedicalAuthorization();
        PreMedicalAuthorization initializedPreMedAuth = preMedicalAuthorizationService.validateCode(preMedicalAuthorization.getCode());
        qrMedicalAuthorization.setPreMedicalAuthorization(initializedPreMedAuth);
        qrMedicalAuthorization.setPetitioner(initializedPreMedAuth.getPetitioner());
        qrMedicalAuthorization.setBeneficiary(initializedPreMedAuth.getBeneficiary());
    }

    private JsonNode validateQrJson(String qrJsonString) throws IOException, ObjectNotValidException {
        JsonNode qrJson = this.getObjectMapper().readTree(qrJsonString);
        long generationTimestamp = qrJson.get(TIMESTAMP).asLong();
        LocalDateTime generationDateTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(generationTimestamp),
                        TimeZone.getDefault().toZoneId());
        if (generationDateTime.plusMinutes(QR_DURATION).isBefore(LocalDateTime.now()))
            throw new ObjectNotValidException(
                    "medicalAuthorization.expiredQR",
                    generationDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        String type = qrJson.get(QR_TYPE).asText();
        if (StringUtils.equals(type, BENEFICIARY_TYPE) && qrMedicalAuthorizationRepository.existsByEncryptedQrKey(qrJson.get(ENCRYPTED_QR_KEY).asText()))
            throw new ObjectAlreadyExistsException("medicalAuthorization.qrAlreadyUsed");
        return qrJson;
    }

    private QRMedicalAuthorization mapMedicalAuthorizationFromQr(String decryptedQr) throws ObjectNotValidException {
        try {
            JsonNode validatedQrJson = validateQrJson(decryptedQr);
            ObjectNode validatedQrJsonObjectNode = (ObjectNode) validatedQrJson;
            validatedQrJsonObjectNode.remove(TIMESTAMP);
            QRMedicalAuthorization qrMedicalAuthorization = this.getObjectMapper().treeToValue(validatedQrJsonObjectNode, QRMedicalAuthorization.class);
            qrMedicalAuthorization.setType(validatedQrJson.get(QR_TYPE).asText());
            return qrMedicalAuthorization;
        } catch (IOException e) {
            log.error("Object could not be mapped from decryptedQr: {}", decryptedQr);
            throw new ObjectNotValidException("medicalAuthorization.invalidQRContent");
        }
    }

    @Transactional
    @Override
    public MedicalAuthorizationProjection.QR resolveQrCode(String encryptedQr) throws ObjectNotValidException, ObjectNotFoundException {
        QRMedicalAuthorization qrMedicalAuthorization = initializeDataFromValidatedQr(encryptedQr);
        if (qrMedicalAuthorization.getPreMedicalAuthorization() != null) {
            Set<MedicalAuthorizationItem> items = buildPreMedicalAuthorizationItems(qrMedicalAuthorization.getPreMedicalAuthorization());
            qrMedicalAuthorization.setMedicalAuthorizationItems(items);
        }
        return this.getProjectionFactory().createProjection(MedicalAuthorizationProjection.QR.class, qrMedicalAuthorization);
    }

    private Set<MedicalAuthorizationItem> buildPreMedicalAuthorizationItems(PreMedicalAuthorization preMedicalAuthorization) {
        return preMedicalAuthorization.getPreMedicalAuthorizationItems().stream()
                .map(i -> {
                    MedicalAuthorizationItem medItem = new MedicalAuthorizationItem();
                    medItem.setNomenclator(i.getNomenclator());
                    medItem.setQuantity(i.getRemaining());
                    return medItem;
                })
                .collect(Collectors.toUnmodifiableSet());
    }

}