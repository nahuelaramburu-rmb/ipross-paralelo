package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.audittray.service.AuditTraySender;
import com.capacidad.validationapi.module.budget.service.BudgetService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationService;
import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
import com.capacidad.validationapi.module.settlement.service.SettlementService;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.service.StorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_MEDICAL_CENTER_INSTANCE;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_PRACTITIONER_INSTANCE;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationPostProcessorImplTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private StorageService storageService;

    @Mock
    private AuditTraySender auditTraySender;

    @Mock
    private BudgetService budgetService;

    @Mock
    private SettlementService settlementService;

    @Mock
    private PreMedicalAuthorizationService preMedicalAuthorizationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private MedicalAuthorizationPostProcessorImpl medicalAuthorizationPostProcessor;

    @Test
    public void testPostProcessExecutesSuccessfullyWhenNullSignatureAndMedicalCenterRole() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setDigitalSignature(null);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("123.56"));
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        medicalAuthorizationPostProcessor.postProcess(medicalAuthorization);

        assertThat(medicalAuthorization.getChargeTotal()).isEqualTo(new BigDecimal(124));
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorization(medicalAuthorization);
        verify(storageService, never()).storeFile(any(FileType.class), any(FileDTO.class), anyBoolean());
        verify(auditTraySender, times(1)).audit(medicalAuthorization);
        verify(budgetService, times(1)).calculateBudget(medicalAuthorization);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorization(medicalAuthorization);
        verify(prescriptionService, times(1)).createFromMedicalAuthorization(medicalAuthorization);
        verify(applicationEventPublisher, never()).publishEvent(any());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testPostProcessExecutesSuccessfullyWhenExceptionOnSignatureAndPractitionerRole() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_PRACTITIONER_INSTANCE));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setDigitalSignature(new byte[0]);
        medicalAuthorization.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("123.56"));
        medicalAuthorizationItem.setStatus(VALIDATION_REJECTED.getInstance());

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        doThrow(new ObjectNotValidException("")).when(storageService).storeFile(any(FileType.class), any(FileDTO.class), anyBoolean());

        medicalAuthorizationPostProcessor.postProcess(medicalAuthorization);

        assertThat(medicalAuthorization.getChargeTotal()).isEqualTo(new BigDecimal(0));
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorization(medicalAuthorization);
        verify(storageService, times(1)).storeFile(any(FileType.class), any(FileDTO.class), anyBoolean());
        verify(auditTraySender, times(1)).audit(medicalAuthorization);
        verify(budgetService, never()).calculateBudget(medicalAuthorization);
        verify(budgetService, times(1)).calculateBeneficiaryBudget(medicalAuthorization);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorization(medicalAuthorization);
        verify(prescriptionService, times(1)).createFromMedicalAuthorization(medicalAuthorization);
        verify(applicationEventPublisher, never()).publishEvent(any());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testPostProcessExecutesSuccessfullyWhenValidSignatureAndMedicalCenterRole() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setDigitalSignature(new byte[0]);
        medicalAuthorization.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal("123.56"));
        medicalAuthorizationItem.setStatus(VALIDATION_APPROVED.getInstance());

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        medicalAuthorizationPostProcessor.postProcess(medicalAuthorization);

        assertThat(medicalAuthorization.getChargeTotal()).isEqualTo(new BigDecimal(124));
        verify(preMedicalAuthorizationService, times(1)).processMedicalAuthorization(medicalAuthorization);
        verify(storageService, times(1)).storeFile(any(FileType.class), any(FileDTO.class), anyBoolean());
        verify(auditTraySender, times(1)).audit(medicalAuthorization);
        verify(budgetService, times(1)).calculateBudget(medicalAuthorization);
        verify(settlementService, times(1)).createOrUpdateFromMedicalAuthorization(medicalAuthorization);
        verify(prescriptionService, times(1)).createFromMedicalAuthorization(medicalAuthorization);
        verify(applicationEventPublisher, never()).publishEvent(any());

        SecurityContextHolder.setContext(defaultContext);
    }

}
