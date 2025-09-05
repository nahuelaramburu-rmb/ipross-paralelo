package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.encryption.EncryptionService;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.dto.QRMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;
import com.capacidad.validationapi.module.medicalauthorization.model.QRMedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalauthorization.repository.QRMedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorizationItem;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.ENCRYPTED_QR_KEY;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.TIMESTAMP;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_MEDICAL_CENTER_INSTANCE;
import static com.capacidad.validationapi.module.medicalauthorization.service.impl.QRMedicalAuthorizationServiceImpl.BENEFICIARY_TYPE;
import static com.capacidad.validationapi.module.medicalauthorization.service.impl.QRMedicalAuthorizationServiceImpl.QR_TYPE;
import static com.capacidad.validationapi.module.premedicalauthorization.service.impl.PreMedicalAuthorizationServiceImpl.PRE_MEDICAL_AUTHORIZATION_CODE_KEY;
import static com.capacidad.validationapi.module.premedicalauthorization.service.impl.PreMedicalAuthorizationServiceImpl.PRE_MEDICAL_AUTHORIZATION_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QRMedicalAuthorizationServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    private SecurityContext defaultContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private MedicalCenterService medicalCenterService;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Mock
    private QRMedicalAuthorizationRepository qrMedicalAuthorizationRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private PreMedicalAuthorizationService preMedicalAuthorizationService;

    @Spy
    @InjectMocks
    private QRMedicalAuthorizationServiceImpl qrMedicalAuthorizationService;

    public static JSONObject buildBeneficiaryQrJson(Beneficiary beneficiary, boolean expired) {
        JSONObject beneficiaryJson = new JSONObject();
        beneficiaryJson.put("idNumber", beneficiary.getIdNumber());
        if (beneficiary.getIdType() != null) {
            JSONObject idTypeJson = new JSONObject();
            idTypeJson.put("id", beneficiary.getIdType().getId());
            beneficiaryJson.put("idType", idTypeJson);
        }
        JSONObject container = new JSONObject();
        container.put("beneficiary", beneficiaryJson);
        container.put(ENCRYPTED_QR_KEY, UUID.randomUUID());
        container.put(QR_TYPE, BENEFICIARY_TYPE);
        container.put(TIMESTAMP, expired ? Instant.now().minusSeconds(3600).toEpochMilli() : Instant.now().toEpochMilli());
        return container;
    }

    @Before
    public void init() throws ObjectNotFoundException {
        defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE));
        when(qrMedicalAuthorizationService.getMedicalCenterService()).thenReturn(medicalCenterService);
        when(qrMedicalAuthorizationService.getPractitionerService()).thenReturn(practitionerService);
        MedicalCenter medicalCenter = new MedicalCenter();
        Address address = new Address();
        address.setCity(new City());
        medicalCenter.setAddress(address);
        when(medicalCenterService.getAuthMedicalCenter()).thenReturn(medicalCenter);
        when(practitionerService.findByIdLocked(anyLong())).thenReturn(new Practitioner());
    }

    @After
    public void destroy() {
        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testInitializeFailsWhenQRIsExpired() throws ObjectNotValidException {
        QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO = new QRMedicalAuthorizationDTO();
        QRMedicalAuthorization qrMedicalAuthorization = new QRMedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        qrMedicalAuthorization.setPractitioner(practitioner);

        String beneficiaryJsonString = buildBeneficiaryQrJson(beneficiary, true).toString();
        String encryptedQr = "encryptedQr";
        qrMedicalAuthorizationDTO.setEncryptedQr(encryptedQr);

        when(encryptionService.decrypt(encryptedQr)).thenReturn(beneficiaryJsonString);
        when(qrMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper).thenReturn(new ObjectMapper());
        when(objectMapper.convertValue(qrMedicalAuthorizationDTO, QRMedicalAuthorization.class)).thenReturn(qrMedicalAuthorization);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> qrMedicalAuthorizationService.initialize(qrMedicalAuthorizationDTO));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.expiredQR");
    }

    @Test
    public void testInitializeFailsWhenQRAlreadyUsed() throws ObjectNotValidException {
        QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO = new QRMedicalAuthorizationDTO();
        QRMedicalAuthorization qrMedicalAuthorization = new QRMedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        qrMedicalAuthorization.setPractitioner(practitioner);

        String beneficiaryJsonString = buildBeneficiaryQrJson(beneficiary, false).toString();
        String encryptedQr = "encryptedQr";
        qrMedicalAuthorizationDTO.setEncryptedQr(encryptedQr);

        when(qrMedicalAuthorizationRepository.existsByEncryptedQrKey(anyString())).thenReturn(true);
        when(encryptionService.decrypt(encryptedQr)).thenReturn(beneficiaryJsonString);
        when(qrMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper).thenReturn(new ObjectMapper());
        when(objectMapper.convertValue(qrMedicalAuthorizationDTO, QRMedicalAuthorization.class)).thenReturn(qrMedicalAuthorization);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> qrMedicalAuthorizationService.initialize(qrMedicalAuthorizationDTO));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.qrAlreadyUsed");
    }

    @Test
    public void testInitializeFailsWhenQRContentIsInvalid() throws ObjectNotValidException {
        QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO = new QRMedicalAuthorizationDTO();
        QRMedicalAuthorization qrMedicalAuthorization = new QRMedicalAuthorization();

        String encryptedQr = "encryptedQr";
        qrMedicalAuthorizationDTO.setEncryptedQr(encryptedQr);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        qrMedicalAuthorization.setPractitioner(practitioner);

        when(encryptionService.decrypt(encryptedQr)).thenReturn("invalidcontent");
        when(qrMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper).thenReturn(new ObjectMapper());
        when(objectMapper.convertValue(qrMedicalAuthorizationDTO, QRMedicalAuthorization.class)).thenReturn(qrMedicalAuthorization);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> qrMedicalAuthorizationService.initialize(qrMedicalAuthorizationDTO));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.invalidQRContent");
    }

    @Test
    public void testInitializeFailsWhenInvalidQRType() throws ObjectNotValidException {
        QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO = new QRMedicalAuthorizationDTO();
        QRMedicalAuthorization qrMedicalAuthorization = new QRMedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        qrMedicalAuthorization.setPractitioner(practitioner);

        JSONObject beneficiaryJson = buildBeneficiaryQrJson(beneficiary, false);
        beneficiaryJson.put(QR_TYPE, "invalidType");
        String encryptedQr = "encryptedQr";
        qrMedicalAuthorizationDTO.setEncryptedQr(encryptedQr);

        when(encryptionService.decrypt(encryptedQr)).thenReturn(beneficiaryJson.toString());
        AuthorizationType qrAuthorizationType = new AuthorizationType();
        qrAuthorizationType.setId(AuthorizationTypeReference.AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());

        when(qrMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper).thenReturn(new ObjectMapper());
        when(objectMapper.convertValue(qrMedicalAuthorizationDTO, QRMedicalAuthorization.class)).thenReturn(qrMedicalAuthorization);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> qrMedicalAuthorizationService.initialize(qrMedicalAuthorizationDTO));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.invalidQrType");
    }

    @Test
    public void testInitializeReturnsValidAuthorizationType() throws ObjectNotFoundException, ObjectNotValidException {
        QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO = new QRMedicalAuthorizationDTO();
        QRMedicalAuthorization qrMedicalAuthorization = new QRMedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        qrMedicalAuthorization.setPractitioner(practitioner);

        String beneficiaryJsonString = buildBeneficiaryQrJson(beneficiary, false).toString();
        String encryptedQr = "encryptedQr";
        qrMedicalAuthorizationDTO.setEncryptedQr(encryptedQr);

        when(encryptionService.decrypt(encryptedQr)).thenReturn(beneficiaryJsonString);
        AuthorizationType qrAuthorizationType = new AuthorizationType();
        qrAuthorizationType.setId(AuthorizationTypeReference.AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());

        when(qrMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper).thenReturn(new ObjectMapper());
        when(objectMapper.convertValue(qrMedicalAuthorizationDTO, QRMedicalAuthorization.class)).thenReturn(qrMedicalAuthorization);
        when(beneficiaryFinder.findBeneficiaryLocked(anyLong(), any(IdType.class))).thenReturn(beneficiary);
        doNothing().when(qrMedicalAuthorizationService).validate(qrMedicalAuthorization);

        QRMedicalAuthorization result = qrMedicalAuthorizationService.initialize(qrMedicalAuthorizationDTO);

        assertThat(result.getAuthorizationType()).isNotNull();
        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getBeneficiary().getId()).isEqualTo(beneficiary.getId());
        assertThat(result.getAuthorizationType().getId()).isEqualTo(AuthorizationTypeReference.AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());
    }

    @Test
    public void testInitializeWithPreAuthorizationReturnsValidAuthorizationType() throws ObjectNotFoundException, ObjectNotValidException {
        QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO = new QRMedicalAuthorizationDTO();
        QRMedicalAuthorization qrMedicalAuthorization = new QRMedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        Practitioner petitioner = new Practitioner();
        petitioner.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        qrMedicalAuthorization.setPractitioner(practitioner);

        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        preMedicalAuthorization.setBeneficiary(beneficiary);
        preMedicalAuthorization.setPetitioner(petitioner);
        String code = "ABCD1234";
        preMedicalAuthorization.setCode(code);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setQuantity(1);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        JSONObject content = new JSONObject();
        JSONObject preMedAuth = new JSONObject();

        preMedAuth.put(PRE_MEDICAL_AUTHORIZATION_CODE_KEY, code);
        content.put("preMedicalAuthorization", preMedAuth);
        content.put(ENCRYPTED_QR_KEY, UUID.randomUUID());
        content.put(TIMESTAMP, Instant.now().toEpochMilli());
        content.put(QR_TYPE, PRE_MEDICAL_AUTHORIZATION_TYPE);

        String encryptedQr = "encryptedQr";
        qrMedicalAuthorizationDTO.setEncryptedQr(encryptedQr);

        when(encryptionService.decrypt(encryptedQr)).thenReturn(content.toString());
        AuthorizationType qrAuthorizationType = new AuthorizationType();
        qrAuthorizationType.setId(AuthorizationTypeReference.AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());

        when(qrMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper).thenReturn(new ObjectMapper());
        when(objectMapper.convertValue(qrMedicalAuthorizationDTO, QRMedicalAuthorization.class)).thenReturn(qrMedicalAuthorization);
        when(preMedicalAuthorizationService.validateCode(code)).thenReturn(preMedicalAuthorization);
        doNothing().when(qrMedicalAuthorizationService).validate(qrMedicalAuthorization);

        QRMedicalAuthorization result = qrMedicalAuthorizationService.initialize(qrMedicalAuthorizationDTO);

        assertThat(result.getAuthorizationType()).isNotNull();
        assertThat(result.getPetitioner().getId()).isEqualTo(petitioner.getId());
        assertThat(result.getPreMedicalAuthorization().getCode()).isEqualTo(code);
        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getBeneficiary().getId()).isEqualTo(beneficiary.getId());
        assertThat(result.getAuthorizationType().getId()).isEqualTo(AuthorizationTypeReference.AUTHORIZATION_TYPE_AUTOMATIC_QR.getId());
    }

    @Test
    public void testResolveQrCodeReturnsValidProjectionWhenQrHasNotBeenUsed() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        String beneficiaryJsonString = buildBeneficiaryQrJson(beneficiary, false).toString();
        String encryptedQr = "encryptedQr";

        when(encryptionService.decrypt(encryptedQr)).thenReturn(beneficiaryJsonString);
        when(qrMedicalAuthorizationRepository.existsByEncryptedQrKey(anyString())).thenReturn(false);
        when(qrMedicalAuthorizationService.getObjectMapper()).thenReturn(new ObjectMapper());
        when(beneficiaryFinder.findBeneficiaryLocked(anyLong(), any(IdType.class))).thenReturn(beneficiary);

        MedicalAuthorizationProjection.QR result = qrMedicalAuthorizationService.resolveQrCode(encryptedQr);

        assertThat(result).isNotNull();
        assertThat(result.getBeneficiary().getIdNumber()).isEqualTo(beneficiary.getIdNumber());
    }

}
