package com.capacidad.validationapi.module.premedicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.encryption.EncryptionService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationResponseDTO;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorizationItem;
import com.capacidad.validationapi.module.premedicalauthorization.repository.PreMedicalAuthorizationRepository;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationBuilder;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationValidator;
import com.capacidad.validationapi.module.render.service.RenderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.ENCRYPTED_QR_KEY;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.TIMESTAMP;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference.AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION;
import static com.capacidad.validationapi.module.premedicalauthorization.service.impl.PreMedicalAuthorizationServiceImpl.PRE_MEDICAL_AUTHORIZATION_CODE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PreMedicalAuthorizationServiceImplTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    private RenderService renderService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private PreMedicalAuthorizationRepository preMedicalAuthorizationRepository;

    @Mock
    private PreMedicalAuthorizationBuilder preMedicalAuthorizationBuilder;

    @Mock
    private PreMedicalAuthorizationValidator preMedicalAuthorizationValidator;

    @Mock
    private Utils utils;

    @Spy
    @InjectMocks
    private PreMedicalAuthorizationServiceImpl preMedicalAuthorizationService;

    @Test
    public void testProcessMedicalAuthorizationDoNothingWhenNullPreMedAuth() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(null);

        preMedicalAuthorizationService.processMedicalAuthorization(medicalAuthorization);

        verify(preMedicalAuthorizationValidator, never()).validate(any(MedicalAuthorization.class));
    }

    @Test
    public void testProcessMedicalAuthorizationDoNotUpdatesChargeSubtotalsWhenNullUnitPrice() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        String preMedicalAuthorizationCode = "ABCDE123456";
        preMedicalAuthorization.setCode(preMedicalAuthorizationCode);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setChargeUnitPrice(null);
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        preMedicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalAuthorizationItem medicalAuthorizationItem = mock(MedicalAuthorizationItem.class);
        when(medicalAuthorizationItem.getNomenclator()).thenReturn(nomenclator);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.setSelectedPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        AuthorizationType authorizationType = new AuthorizationType();
        authorizationType.setId(AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION.getId());

        when(preMedicalAuthorizationRepository.findByCode(preMedicalAuthorizationCode)).thenReturn(Optional.of(preMedicalAuthorization));
        when(preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization)).thenReturn(active);
        when(preMedicalAuthorizationRepository.save(preMedicalAuthorization)).thenReturn(preMedicalAuthorization);

        preMedicalAuthorizationService.processMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorization.getStatus()).isEqualTo(active);
        assertThat(medicalAuthorization.getAuthorizationType().getId()).isEqualTo(authorizationType.getId());
        verify(medicalAuthorizationItem, never()).getChargeUnitPrice();
        verify(medicalAuthorizationItem, never()).getChargeSubtotal();
        verify(preMedicalAuthorizationValidator, times(1)).validate(medicalAuthorization);
        verify(preMedicalAuthorizationRepository, times(1)).save(preMedicalAuthorization);
    }

    @Test
    public void testProcessMedicalAuthorizationDoNotUpdatesChargeSubtotalsWhenInvalidNomenclator() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        String preMedicalAuthorizationCode = "ABCDE123456";
        preMedicalAuthorization.setCode(preMedicalAuthorizationCode);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator.setId(2L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setChargeUnitPrice(new BigDecimal(123));
        preMedicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalAuthorizationItem medicalAuthorizationItem = mock(MedicalAuthorizationItem.class);
        when(medicalAuthorizationItem.getNomenclator()).thenReturn(nomenclator2);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.setSelectedPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        AuthorizationType authorizationType = new AuthorizationType();
        authorizationType.setId(AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION.getId());

        when(preMedicalAuthorizationRepository.findByCode(preMedicalAuthorizationCode)).thenReturn(Optional.of(preMedicalAuthorization));
        when(preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization)).thenReturn(active);
        when(preMedicalAuthorizationRepository.save(preMedicalAuthorization)).thenReturn(preMedicalAuthorization);

        preMedicalAuthorizationService.processMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorization.getStatus()).isEqualTo(active);
        assertThat(medicalAuthorization.getAuthorizationType().getId()).isEqualTo(authorizationType.getId());
        verify(medicalAuthorizationItem, never()).getChargeUnitPrice();
        verify(medicalAuthorizationItem, never()).getChargeSubtotal();
        verify(preMedicalAuthorizationValidator, times(1)).validate(medicalAuthorization);
        verify(preMedicalAuthorizationRepository, times(1)).save(preMedicalAuthorization);
    }

    @Test
    public void testProcessMedicalAuthorizationUpdatesChargeSubtotalsWhenValidItem() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        String preMedicalAuthorizationCode = "ABCDE123456";
        preMedicalAuthorization.setCode(preMedicalAuthorizationCode);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setChargeUnitPrice(new BigDecimal(123));
        preMedicalAuthorizationItem.setNomenclator(nomenclator);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setQuantity(2);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.setSelectedPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        AuthorizationType authorizationType = new AuthorizationType();
        authorizationType.setId(AUTHORIZATION_TYPE_PRE_MEDICAL_AUTHORIZATION.getId());

        when(preMedicalAuthorizationRepository.findByCode(preMedicalAuthorizationCode)).thenReturn(Optional.of(preMedicalAuthorization));
        when(preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization)).thenReturn(active);
        when(preMedicalAuthorizationRepository.save(preMedicalAuthorization)).thenReturn(preMedicalAuthorization);

        preMedicalAuthorizationService.processMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorization.getStatus()).isEqualTo(active);
        assertThat(medicalAuthorization.getAuthorizationType().getId()).isEqualTo(authorizationType.getId());
        assertThat(medicalAuthorizationItem.getChargeUnitPrice()).isEqualTo(preMedicalAuthorizationItem.getChargeUnitPrice());
        assertThat(medicalAuthorizationItem.getChargeSubtotal()).isEqualTo(preMedicalAuthorizationItem.getChargeUnitPrice()
                .multiply(new BigDecimal(medicalAuthorizationItem.getQuantity())));
        verify(preMedicalAuthorizationValidator, times(1)).validate(medicalAuthorization);
        verify(preMedicalAuthorizationRepository, times(1)).save(preMedicalAuthorization);
    }

    @Test
    public void testProcessMedicalAuthorizationFailsWhenCodeDoesNotExist() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        String preMedicalAuthorizationCode = "invalidCode";
        preMedicalAuthorization.setCode(preMedicalAuthorizationCode);
        medicalAuthorization.setSelectedPreMedicalAuthorization(preMedicalAuthorization);

        when(preMedicalAuthorizationRepository.findByCode(preMedicalAuthorizationCode)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> preMedicalAuthorizationService.processMedicalAuthorization(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("preMedicalAuthorization.notFound");
    }

    @Test
    public void testCreateGeneratesReceiptSuccessfully() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        PreMedicalAuthorizationDTO preMedicalAuthorizationDTO = new PreMedicalAuthorizationDTO();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        preMedicalAuthorization.setId(2000L);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(30));

        String code = "ABCDE123456";

        preMedicalAuthorization.setCode(code);

        String encryptedContent = "encryptedContent";
        String qrContent = "qrContent";

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode content = objectMapper.createObjectNode();

        ByteArrayOutputStream mockByteArrayOutputStream = mock(ByteArrayOutputStream.class);

        when(preMedicalAuthorizationService.getObjectMapper())
                .thenReturn(mockObjectMapper).thenReturn(new ObjectMapper());
        when(mockObjectMapper.createObjectNode()).thenReturn(content);
        when(preMedicalAuthorizationBuilder.build(preMedicalAuthorizationDTO)).thenReturn(preMedicalAuthorization);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getPrincipal()).thenReturn("admin");
        when(preMedicalAuthorizationRepository.saveAndFlush(preMedicalAuthorization)).thenReturn(preMedicalAuthorization);
        doNothing().when(preMedicalAuthorizationRepository).refresh(preMedicalAuthorization);
        when(encryptionService.encrypt(anyString())).thenReturn(encryptedContent);
        when(renderService.renderQrCode(encryptedContent)).thenReturn(qrContent);
        when(renderService.renderPDF(anyString(), anyMap())).thenReturn(mockByteArrayOutputStream);

        PreMedicalAuthorizationResponseDTO result = preMedicalAuthorizationService.createAndGenerateReceipt(preMedicalAuthorizationDTO);

        JsonNode preMedicalAuthorizationObj = content.get("preMedicalAuthorization");
        assertThat(result.getOutputStream()).isEqualTo(mockByteArrayOutputStream);
        assertThat(preMedicalAuthorizationObj.get(PRE_MEDICAL_AUTHORIZATION_CODE_KEY).asText()).isEqualTo(code);
        assertThat(content.get(TIMESTAMP).asLong()).isPositive();
        assertThat(content.get(ENCRYPTED_QR_KEY)).isNotNull();
    }

    @Test
    public void testProcessMedicalAuthorizationItemDoNothingWhenNullPreMedAuth() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(null);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        preMedicalAuthorizationService.processMedicalAuthorizationItem(medicalAuthorizationItem, false);

        verify(preMedicalAuthorizationValidator, never()).determineItemConsumption(any(PreMedicalAuthorizationItem.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testProcessMedicalAuthorizationItemThrowsExceptionWhenInvalidItem() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator2);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        medicalAuthorizationItem.setStatus(approved);

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> preMedicalAuthorizationService.processMedicalAuthorizationItem(medicalAuthorizationItem, false));

        assertThat(exception.getMessage()).contains("preMedicalAuthorization.itemDoesNotExists");
    }

    @Test
    public void testProcessMedicalAuthorizationItemUpdatesAuditingOnlyWhenNotApproved() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setAuditing(true);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        medicalAuthorizationItem.setStatus(rejected);

        preMedicalAuthorizationService.processMedicalAuthorizationItem(medicalAuthorizationItem, false);

        assertThat(preMedicalAuthorizationItem.getAuditing()).isFalse();
    }

    @Test
    public void testProcessMedicalAuthorizationItemExecuteSuccessfullyWhenValidItem() throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setAuditing(true);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        medicalAuthorizationItem.setStatus(approved);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        when(preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization)).thenReturn(active);
        when(preMedicalAuthorizationRepository.save(preMedicalAuthorization)).thenReturn(preMedicalAuthorization);

        preMedicalAuthorizationService.processMedicalAuthorizationItem(medicalAuthorizationItem, false);

        verify(preMedicalAuthorizationValidator, times(1)).determineItemConsumption(preMedicalAuthorizationItem, medicalAuthorizationItem);
        assertThat(preMedicalAuthorization.getStatus()).isEqualTo(active);
        assertThat(preMedicalAuthorizationItem.getAuditing()).isFalse();
    }

    @Test
    public void testRollbackConsumptionFromMedicalAuthorizationDoNothingWhenNullPreAuth() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(null);

        preMedicalAuthorizationService.rollbackConsumptionFromMedicalAuthorization(medicalAuthorization);

        verify(preMedicalAuthorizationRepository, never()).save(any(PreMedicalAuthorization.class));
    }

    @Test
    public void testRollbackConsumptionFromMedicalAuthorizationDoNotUpdatesRemainingWhenInvalidItem() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator2);
        preMedicalAuthorizationItem.setRemaining(2);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        preMedicalAuthorizationService.rollbackConsumptionFromMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorizationItem.getRemaining()).isEqualTo(2);
    }

    @Test
    public void testRollbackConsumptionFromMedicalAuthorizationDoNotUpdatesWhenAuditingButApproved() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setStatus(approved);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setRemaining(2);
        preMedicalAuthorizationItem.setAuditing(true);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        preMedicalAuthorizationService.rollbackConsumptionFromMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorizationItem.getRemaining()).isEqualTo(2);
        assertThat(preMedicalAuthorizationItem.getAuditing()).isTrue();
    }

    @Test
    public void testRollbackConsumptionFromMedicalAuthorizationUpdatesAuditingFlagWhenPending() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setStatus(pending);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setRemaining(2);
        preMedicalAuthorizationItem.setAuditing(true);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        preMedicalAuthorizationService.rollbackConsumptionFromMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorizationItem.getRemaining()).isEqualTo(2);
        assertThat(preMedicalAuthorizationItem.getAuditing()).isFalse();
    }

    @Test
    public void testRollbackConsumptionFromMedicalAuthorizationDoNotUpdatesRemainingWhenNotApproved() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setStatus(rejected);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setRemaining(2);
        preMedicalAuthorizationItem.setAuditing(false);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        preMedicalAuthorizationService.rollbackConsumptionFromMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorizationItem.getRemaining()).isEqualTo(2);
    }

    @Test
    public void testRollbackConsumptionFromMedicalAuthorizationUpdatesRemainingWhenNotAuditingAndApproved() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setQuantity(1);
        medicalAuthorizationItem.setStatus(approved);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setRemaining(2);
        preMedicalAuthorizationItem.setAuditing(false);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        preMedicalAuthorizationService.rollbackConsumptionFromMedicalAuthorization(medicalAuthorization);

        assertThat(preMedicalAuthorizationItem.getRemaining()).isEqualTo(3);
    }

}
