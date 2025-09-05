package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionDTO;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionProjection;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionRepository;
import com.capacidad.validationapi.module.prescription.service.PrescriptionIntegrationInvoker;
import com.capacidad.validationapi.module.prescription.service.PrescriptionSupportService;
import com.capacidad.validationapi.module.prescription.service.PrescriptionValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionValidator prescriptionValidator;

    @Mock
    private Utils utils;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private PrescriptionSupportService prescriptionSupportService;

    @Mock
    private PrescriptionIntegrationInvoker prescriptionIntegrationInvoker;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Mock
    private ProjectionFactory projectionFactory;

    @Spy
    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    @Test
    public void testCreatePrescriptionIsSuccessful() throws ObjectNotValidException, ObjectNotFoundException {
        PrescriptionDTO prescriptionDTO = new PrescriptionDTO();

        Prescription prescription = initialize(prescriptionDTO);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(123456L);

        Practitioner practitioner = new Practitioner();
        practitioner.setIdNumber(654321L);

        prescription.setBeneficiary(beneficiary);
        prescription.setPractitioner(practitioner);

        when(prescriptionSupportService.getPrescriptionExpirationPeriod()).thenReturn(Period.WEEKLY);

        Prescription result = prescriptionService.create(prescriptionDTO);

        assertThat(result.getStatus().getId()).isEqualTo(PRESCRIPTION_APPROVED.getId());
        assertThat(result.getKey()).isNotEmpty();
        assertThat(result.getExpirationPeriod()).isEqualTo(Period.WEEKLY);
        assertThat(result.getDType()).isEqualTo("defaultPrescriptionServiceImpl");
        verify(prescriptionIntegrationInvoker, times(1)).invokeCreation(prescription);
        verify(prescriptionSupportService, times(1)).sendNewPrescriptionNotification(prescription);
    }

    @Test
    public void testCreatePrescriptionsIsSuccessful() throws ObjectNotValidException, ObjectNotFoundException {
        PrescriptionDTO prescriptionDTO = new PrescriptionDTO();

        Prescription prescription = initialize(prescriptionDTO);

        Set<PrescriptionDTO> prescriptionDTOS = new HashSet<>();
        prescriptionDTOS.add(prescriptionDTO);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(123456L);

        Practitioner practitioner = new Practitioner();
        practitioner.setIdNumber(654321L);

        prescription.setBeneficiary(beneficiary);
        prescription.setPractitioner(practitioner);

        List<Prescription> prescriptions = Collections.singletonList(prescription);

        when(prescriptionRepository.saveAll(anyCollection())).thenReturn(prescriptions);

        when(prescriptionSupportService.getPrescriptionExpirationPeriod()).thenReturn(Period.WEEKLY);

        prescriptionService.createAll(prescriptionDTOS);

        assertThat(prescription.getStatus().getId()).isEqualTo(PRESCRIPTION_APPROVED.getId());
        assertThat(prescription.getKey()).isNotEmpty();
        assertThat(prescription.getExpirationPeriod()).isEqualTo(Period.WEEKLY);
        assertThat(prescription.getDType()).isEqualTo("defaultPrescriptionServiceImpl");
        verify(prescriptionIntegrationInvoker, times(1)).invokeCreation(prescriptions);
        verify(prescriptionSupportService, times(1)).sendNewPrescriptionNotification(prescription);
    }

    @Test
    public void testCreatePrescriptionFromMedAuthIsSuccessful() throws ObjectNotValidException, ObjectNotFoundException {
        PrescriptionDTO prescriptionDTO = new PrescriptionDTO();

        Prescription prescription = initialize(prescriptionDTO);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(123456L);

        Practitioner practitioner = new Practitioner();
        practitioner.setIdNumber(654321L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setMedicalCenter(new MedicalCenter());
        medicalAuthorization.setCity(new City());
        medicalAuthorization.getPrescriptions().add(prescription);

        List<Prescription> savedResults = new ArrayList<>(medicalAuthorization.getPrescriptions());

        when(prescriptionSupportService.getPrescriptionExpirationPeriod()).thenReturn(Period.WEEKLY);
        when(prescriptionRepository.saveAll(medicalAuthorization.getPrescriptions())).thenReturn(savedResults);

        prescriptionService.createFromMedicalAuthorization(medicalAuthorization);

        assertThat(prescription.getStatus().getId()).isEqualTo(PRESCRIPTION_APPROVED.getId());
        assertThat(prescription.getPractitioner()).isEqualTo(medicalAuthorization.getPractitioner());
        assertThat(prescription.getBeneficiary()).isEqualTo(medicalAuthorization.getBeneficiary());
        assertThat(prescription.getMedicalCenter()).isEqualTo(medicalAuthorization.getMedicalCenter());
        assertThat(prescription.getKey()).isNotEmpty();
        assertThat(prescription.getExpirationPeriod()).isEqualTo(Period.WEEKLY);
        assertThat(prescription.getDType()).isEqualTo("defaultPrescriptionServiceImpl");
        verify(prescriptionIntegrationInvoker, times(1)).invokeCreation(savedResults);
    }

    @Test
    public void testExistsByAuthBeneficiary() {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());

        when(prescriptionRepository.existsByIdAndBeneficiaryResourceId(anyLong(), any(UUID.class))).thenReturn(true);

        boolean result = prescriptionService.existsByAuthBeneficiaryOrRelative(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testExistsByAuthRelative() {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());

        when(prescriptionRepository.existsByIdAndBeneficiaryResourceId(anyLong(), any(UUID.class))).thenReturn(false);

        when(beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId()).thenReturn(Optional.of(UUID.randomUUID()));
        when(prescriptionRepository.existsByIdAndBeneficiaryFamilyId(anyLong(), any(UUID.class))).thenReturn(true);

        boolean result = prescriptionService.existsByAuthBeneficiaryOrRelative(1L);

        assertThat(result).isTrue();
        verify(prescriptionRepository, times(1)).existsByIdAndBeneficiaryFamilyId(anyLong(), any(UUID.class));
    }

    @Test
    public void testAppendCustomSpecificationReturnsSpecWhenRoleIsBeneficiaryAndExists() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(ROLE_BENEFICIARY_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        Optional<Specification<Prescription>> spec = prescriptionService.appendCustomSpecification();

        assertThat(spec).isPresent();
    }

    @Test
    public void testAppendCustomSpecificationReturnsSpecWhenRoleIsPractitionerAndExists() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(ROLE_PRACTITIONER_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        Optional<Specification<Prescription>> spec = prescriptionService.appendCustomSpecification();

        assertThat(spec).isPresent();
    }

    @Test
    public void testAppendCustomSpecificationReturnsEmptyWhenRoleIsAdmin() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(ROLE_ADMIN_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        Optional<Specification<Prescription>> spec = prescriptionService.appendCustomSpecification();

        assertThat(spec).isEmpty();
    }

    @Test
    public void testCancelPrescriptionThrowsExceptionWhenAlreadyUtilized() throws ObjectNotFoundException {
        Status utilized = new Status();
        utilized.setId(PRESCRIPTION_UTILIZED.getId());

        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(utilized);

        CancellationDTO input = new CancellationDTO();
        input.setCancellationReason("cancellation reason");

        doReturn(prescription).when(prescriptionService).findById(prescription.getId());

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> prescriptionService.cancelPrescription(1L, input));

        assertThat(objectNotValidException.getMessage()).isEqualTo("prescription.alreadyUtilized");
    }

    @Test
    public void testCancelPrescriptionThrowsExceptionWhenAlreadyCancelled() throws ObjectNotFoundException {
        Status cancelled = new Status();
        cancelled.setId(StatusReference.PRESCRIPTION_CANCELLED.getId());

        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(cancelled);

        CancellationDTO input = new CancellationDTO();
        input.setCancellationReason("cancellation reason");

        doReturn(prescription).when(prescriptionService).findById(prescription.getId());

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> prescriptionService.cancelPrescription(1L, input));

        assertThat(objectNotValidException.getMessage()).isEqualTo("prescription.alreadyCancelled");
    }

    @Test
    public void testCancelPrescriptionIsSuccessfulWhenStatusNotCancelled() throws ObjectNotFoundException, ObjectNotValidException {
        Status approved = new Status();
        approved.setId(PRESCRIPTION_APPROVED.getId());

        Status cancelled = new Status();
        cancelled.setId(PRESCRIPTION_CANCELLED.getId());

        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(approved);

        CancellationDTO input = new CancellationDTO();
        input.setCancellationReason("cancellation reason");

        when(prescriptionService.getUtils()).thenReturn(utils);
        when(utils.getEntityReference(Status.class, PRESCRIPTION_CANCELLED.getId())).thenReturn(cancelled);
        when(prescriptionService.getProjectionFactory()).thenReturn(projectionFactory);
        when(prescriptionRepository.save(prescription)).thenReturn(prescription);
        when(projectionFactory.createProjection(PrescriptionProjection.class, prescription)).thenReturn(mock(PrescriptionProjection.class));
        doReturn(prescription).when(prescriptionService).findById(prescription.getId());

        PrescriptionProjection result = prescriptionService.cancelPrescription(1L, input);

        assertThat(result).isNotNull();
        assertThat(prescription.getStatus().getId()).isEqualTo(StatusReference.PRESCRIPTION_CANCELLED.getId());
        verify(prescriptionIntegrationInvoker, times(1)).invokeCancellation(prescription);
    }

    @Test
    public void testCancelPrescriptionIsSuccessfulWhenPreAuthorized() throws ObjectNotFoundException, ObjectNotValidException {
        Status approved = new Status();
        approved.setId(PRESCRIPTION_APPROVED.getId());

        Status cancelled = new Status();
        cancelled.setId(PRESCRIPTION_CANCELLED.getId());

        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(approved);
        prescription.setPreAuthorized(true);
        prescription.getExchangeId().add(12345L);
        CancellationDTO input = new CancellationDTO();
        input.setCancellationReason("cancellation reason");

        when(prescriptionService.getUtils()).thenReturn(utils);
        when(utils.getEntityReference(Status.class, PRESCRIPTION_CANCELLED.getId())).thenReturn(cancelled);
        when(prescriptionService.getProjectionFactory()).thenReturn(projectionFactory);
        when(prescriptionRepository.save(prescription)).thenReturn(prescription);
        when(projectionFactory.createProjection(PrescriptionProjection.class, prescription)).thenReturn(mock(PrescriptionProjection.class));
        doReturn(prescription).when(prescriptionService).findById(prescription.getId());

        PrescriptionProjection result = prescriptionService.cancelPrescription(1L, input);

        assertThat(result).isNotNull();
        assertThat(result.getExchangeId()).isEmpty();
        assertThat(prescription.getStatus().getId()).isEqualTo(StatusReference.PRESCRIPTION_CANCELLED.getId());
        verify(prescriptionIntegrationInvoker, never()).invokeCancellation(prescription);
    }

    @Test
    public void testSyncStatusDoNotUpdateWhenNotExpiredAndApproved() {
        Prescription prescription = new Prescription();
        prescription.setDType("defaultPrescriptionServiceImpl");
        prescription.setExpirationDate(LocalDate.now().plusDays(1));
        Set<Prescription> prescriptions = new HashSet<>();
        prescriptions.add(prescription);

        Status expired = new Status();
        expired.setId(StatusReference.PRESCRIPTION_EXPIRED.getId());

        Status approved = new Status();
        approved.setId(StatusReference.PRESCRIPTION_APPROVED.getId());

        prescription.setStatus(approved);

        when(prescriptionService.getUtils()).thenReturn(utils);
        when(utils.getEntityReference(Status.class, PRESCRIPTION_EXPIRED.getId())).thenReturn(expired);
        when(prescriptionRepository.findAllByStatusId(PRESCRIPTION_APPROVED.getId())).thenReturn(prescriptions);

        prescriptionService.syncStatus();

        assertThat(prescription.getStatus()).isEqualTo(approved);
    }

    @Test
    public void testSyncStatusDoNotUpdateWhenUtilized() {
        Prescription prescription = new Prescription();
        prescription.setDType("defaultPrescriptionServiceImpl");
        Set<Prescription> prescriptions = new HashSet<>();
        prescriptions.add(prescription);

        Status expired = new Status();
        expired.setId(StatusReference.PRESCRIPTION_EXPIRED.getId());

        Status utilized = new Status();
        utilized.setId(PRESCRIPTION_UTILIZED.getId());

        prescription.setStatus(utilized);

        when(prescriptionService.getUtils()).thenReturn(utils);
        when(utils.getEntityReference(Status.class, PRESCRIPTION_EXPIRED.getId())).thenReturn(expired);
        when(prescriptionRepository.findAllByStatusId(PRESCRIPTION_APPROVED.getId())).thenReturn(prescriptions);

        prescriptionService.syncStatus();

        assertThat(prescription.getStatus()).isEqualTo(utilized);
    }

    @Test
    public void testSyncStatusUpdatesWhenExpiredAndApproved() {
        Prescription prescription = new Prescription();
        prescription.setDType("defaultPrescriptionServiceImpl");
        prescription.setExpirationDate(LocalDate.now().minusDays(1));
        Set<Prescription> prescriptions = new HashSet<>();
        prescriptions.add(prescription);

        Status expired = new Status();
        expired.setId(StatusReference.PRESCRIPTION_EXPIRED.getId());

        Status approved = new Status();
        approved.setId(StatusReference.PRESCRIPTION_APPROVED.getId());

        prescription.setStatus(approved);

        when(prescriptionService.getUtils()).thenReturn(utils);
        when(utils.getEntityReference(Status.class, PRESCRIPTION_EXPIRED.getId())).thenReturn(expired);
        when(prescriptionRepository.findAllByStatusId(PRESCRIPTION_APPROVED.getId())).thenReturn(prescriptions);

        prescriptionService.syncStatus();

        assertThat(prescription.getStatus()).isEqualTo(expired);
    }

    private Prescription initialize(PrescriptionDTO prescriptionDTO) throws ObjectNotValidException, ObjectNotFoundException {
        Prescription prescription = new Prescription();
        doReturn(prescription).when(prescriptionService).mapDtoToInput(prescriptionDTO);
        PrescriptionItem prescriptionItem1 = new PrescriptionItem();
        ICD10Disease disease1 = new ICD10Disease();
        disease1.setId(1L);
        prescriptionItem1.setDisease(disease1);

        PrescriptionItem prescriptionItem2 = new PrescriptionItem();
        ICD10Disease disease2 = new ICD10Disease();
        disease2.setId(2L);
        prescriptionItem2.setDisease(disease2);

        prescription.getPrescriptionItems().add(prescriptionItem1);
        prescription.getPrescriptionItems().add(prescriptionItem2);

        Status prescriptionApproved = new Status();
        prescriptionApproved.setId(PRESCRIPTION_APPROVED.getId());

        when(prescriptionService.getUtils()).thenReturn(utils);

        when(utils.getEntityReference(Status.class, PRESCRIPTION_APPROVED.getId())).thenReturn(prescriptionApproved);
        when(utils.getEntityReference(ICD10Disease.class, disease1.getId())).thenReturn(disease1);
        when(utils.getEntityReference(ICD10Disease.class, disease2.getId())).thenReturn(disease2);

        when(prescriptionRepository.save(prescription)).thenReturn(prescription);

        String encryptedKey = "encryptedKey";
        when(prescriptionSupportService.buildPrescriptionKey(prescription)).thenReturn(encryptedKey);

        doNothing().when(prescriptionValidator).validate(prescription);

        return prescription;
    }


}
