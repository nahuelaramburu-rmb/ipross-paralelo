package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationDiagnosisDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.repository.MedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationSupportService;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.rating.Rating;
import com.capacidad.validationapi.module.rating.RatingDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationServiceImplTest {

    @Mock
    private Utils utils;

    @Mock
    private ContractMediator contractMediator;

    @Mock
    private MedicalAuthorizationRepository medicalAuthorizationRepository;

    @Mock
    private MedicalAuthorizationSupportService medicalAuthorizationSupportService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Mock
    private PractitionerService practitionerService;

    @Spy
    @InjectMocks
    private MedicalAuthorizationServiceImpl medicalAuthorizationService;

    @Test
    public void testCancelMedicalAuthorizationThrowsExceptionWhenAlreadyCancelled() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setSettled(true);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        Status cancelled = new Status();
        cancelled.setId(VALIDATION_CANCELLED.getId());

        medicalAuthorization.setStatus(cancelled);

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.cancelMedicalAuthorization(1L, new CancellationDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.alreadyCancelled");
    }

    @Test
    public void testCancelMedicalAuthorizationThrowsExceptionWhenRejected() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setSettled(true);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        medicalAuthorization.setStatus(rejected);

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.cancelMedicalAuthorization(1L, new CancellationDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.rejected");
    }

    @Test
    public void testCancelMedicalAuthorizationSuccessfullyCancelAllItemsWhenNotSettled() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setSettled(false);

        MedicalAuthorizationItem medicalAuthorizationItem1 = new MedicalAuthorizationItem();
        medicalAuthorizationItem1.setSettled(false);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem1);

        medicalAuthorization.setPractitioner(new Practitioner());
        medicalAuthorization.setRating(new Rating());

        Status cancelled = new Status();
        cancelled.setId(VALIDATION_CANCELLED.getId());

        Status approved = new Status();
        approved.setId(StatusReference.VALIDATION_APPROVED.getId());

        medicalAuthorization.setStatus(approved);

        CancellationDTO cancellationDTO = new CancellationDTO();
        cancellationDTO.setCancellationReason("cancelReason");

        when(utils.getGenericsEntityReference(Status.class, VALIDATION_CANCELLED.getId())).thenReturn(cancelled);
        when(medicalAuthorizationService.getUtils()).thenReturn(utils);
        when(medicalAuthorizationRepository.save(medicalAuthorization)).thenReturn(medicalAuthorization);
        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);
        doNothing().when(medicalAuthorizationSupportService).publishStatusUpdateEventAndNotifyAuditors(medicalAuthorization);

        MedicalAuthorizationProjection result = medicalAuthorizationService.cancelMedicalAuthorization(1L, cancellationDTO);

        assertThat(result).isNotNull();
        assertThat(medicalAuthorization.getStatus().getId()).isEqualTo(cancelled.getId());
        assertThat(medicalAuthorization.getCancellationReason()).isEqualTo(cancellationDTO.getCancellationReason());
        assertThat(medicalAuthorizationItem.getStatus().getId()).isEqualTo(cancelled.getId());
        assertThat(medicalAuthorizationItem1.getStatus().getId()).isEqualTo(cancelled.getId());
        verify(medicalAuthorizationSupportService, times(1)).discountChargesAndValues(medicalAuthorization);
        verify(medicalAuthorizationSupportService, times(1)).publishStatusUpdateEventAndNotifyAuditors(medicalAuthorization);
        verify(practitionerService, times(1)).removeRating(any(Practitioner.class), any(Rating.class));
    }

    @Test
    public void testExistsByAuthBeneficiary() {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());

        when(medicalAuthorizationRepository.existsByIdAndBeneficiaryResourceId(anyLong(), any(UUID.class))).thenReturn(true);

        boolean result = medicalAuthorizationService.existsByAuthBeneficiaryOrRelative(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testExistsByAuthRelative() {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());

        when(medicalAuthorizationRepository.existsByIdAndBeneficiaryResourceId(anyLong(), any(UUID.class))).thenReturn(false);

        when(beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId()).thenReturn(Optional.of(UUID.randomUUID()));
        when(medicalAuthorizationRepository.existsByIdAndBeneficiaryFamilyId(anyLong(), any(UUID.class))).thenReturn(true);

        boolean result = medicalAuthorizationService.existsByAuthBeneficiaryOrRelative(1L);

        assertThat(result).isTrue();
        verify(medicalAuthorizationRepository, times(1)).existsByIdAndBeneficiaryFamilyId(anyLong(), any(UUID.class));
    }

    @Test
    public void testExistsByAuthOrganizationReturnsTrueWhenValidContracts() throws ObjectNotFoundException {
        Organization organization = new Organization();
        Region region = new Region();
        organization.setRegion(region);

        Set<Contract> contractSet = new HashSet<>();
        contractSet.add(new Contract());

        when(contractMediator.findAllAuthOrganizationAndRelatedContracts()).thenReturn(contractSet);
        when(medicalAuthorizationRepository.existsByIdAndContractIn(1L, contractSet)).thenReturn(true);


        boolean result = medicalAuthorizationService.existsByAuthOrganization(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testAddRatingThrowsExceptionWhenNotApproved() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(VALIDATION_CANCELLED.getInstance());

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.addRating(1L, new RatingDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.rateApprovedOnly");
    }

    @Test
    public void testAddRatingThrowsExceptionWhenAlreadyRated() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(VALIDATION_APPROVED.getInstance());
        medicalAuthorization.setRating(new Rating());

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.addRating(1L, new RatingDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.alreadyRated");
    }

    @Test
    public void testAddRatingCalculatesAndAppendNewRatingSuccessfully() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(VALIDATION_APPROVED.getInstance());
        Practitioner practitioner = new Practitioner();
        medicalAuthorization.setRating(null);
        medicalAuthorization.setPractitioner(practitioner);

        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setWaitTime(3);
        ratingDTO.setDuration(2);
        ratingDTO.setCharges(5);
        ratingDTO.setQuality(3);

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);
        when(medicalAuthorizationService.getProjectionFactory()).thenReturn(new SpelAwareProxyProjectionFactory());
        when(medicalAuthorizationRepository.save(medicalAuthorization)).thenReturn(medicalAuthorization);

        MedicalAuthorizationProjection result = medicalAuthorizationService.addRating(1L, ratingDTO);

        assertThat(result.getRating().getAverage()).isEqualTo(new BigDecimal("2.7"));
        assertThat(result.getRating().getQuantity()).isEqualTo(1);
        assertThat(result.getRating().getCharges()).isEqualTo(new BigDecimal(ratingDTO.getCharges()));
        assertThat(result.getRating().getDuration()).isEqualTo(new BigDecimal(ratingDTO.getDuration()));
        assertThat(result.getRating().getQuality()).isEqualTo(new BigDecimal(ratingDTO.getQuality()));
        assertThat(result.getRating().getWaitTime()).isEqualTo(new BigDecimal(ratingDTO.getWaitTime()));
        verify(practitionerService, times(1)).addRating(practitioner, medicalAuthorization.getRating());
    }

    @Test
    public void testUpdateAuthorizationDiagnosisThrowsExceptionWhenCancelled() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(VALIDATION_CANCELLED.getInstance());
        medicalAuthorization.setId(1L);

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.updateAuthorizationDiagnosis(1L, new MedicalAuthorizationDiagnosisDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.notPending");
    }

    @Test
    public void testUpdateAuthorizationDiagnosisThrowsExceptionWhenRejected() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(VALIDATION_REJECTED.getInstance());
        medicalAuthorization.setId(1L);

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.updateAuthorizationDiagnosis(1L, new MedicalAuthorizationDiagnosisDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.notPending");
    }

    @Test
    public void testUpdateAuthorizationDiagnosisOkWhenNullDisease() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        medicalAuthorization.setStatus(VALIDATION_PENDING.getInstance());

        MedicalAuthorizationDiagnosisDTO diagnosisDTO = new MedicalAuthorizationDiagnosisDTO();
        diagnosisDTO.setDiagnosis("diagnosis");

        ICD10Disease icd10Disease = new ICD10Disease();
        icd10Disease.setId(1L);
        icd10Disease.setName("disease");

        medicalAuthorization.setDisease(icd10Disease);

        when(medicalAuthorizationRepository.save(medicalAuthorization)).thenReturn(medicalAuthorization);
        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        MedicalAuthorizationProjection.Diagnosis result = medicalAuthorizationService.updateAuthorizationDiagnosis(1L, diagnosisDTO);

        assertThat(result.getDiagnosis()).isEqualTo(diagnosisDTO.getDiagnosis());
        assertThat(result.getDisease()).isNull();

        assertThat(medicalAuthorization.getDiagnosis()).isEqualTo(diagnosisDTO.getDiagnosis());
        assertThat(medicalAuthorization.getDisease()).isNull();
    }

    @Test
    public void testUpdateAuthorizationDiagnosisOkWhenApproved() throws ObjectNotFoundException, ObjectNotValidException {
        testUpdateAuthorizationDiagnosisOk(VALIDATION_APPROVED.getInstance());
    }

    private void testUpdateAuthorizationDiagnosisOk(Status status) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        medicalAuthorization.setStatus(status);

        MedicalAuthorizationDiagnosisDTO diagnosisDTO = new MedicalAuthorizationDiagnosisDTO();
        diagnosisDTO.setDiagnosis("diagnosis");

        IdDTO<Long> diseaseIDDTO = new IdDTO<>();
        diseaseIDDTO.setId(1L);

        diagnosisDTO.setDisease(diseaseIDDTO);

        ICD10Disease icd10Disease = new ICD10Disease();
        icd10Disease.setId(1L);
        icd10Disease.setName("disease");
        when(utils.getGenericsEntityReference(ICD10Disease.class, 1L)).thenReturn(icd10Disease);
        when(medicalAuthorizationService.getUtils()).thenReturn(utils);
        when(medicalAuthorizationRepository.save(medicalAuthorization)).thenReturn(medicalAuthorization);
        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        MedicalAuthorizationProjection.Diagnosis result = medicalAuthorizationService.updateAuthorizationDiagnosis(1L, diagnosisDTO);

        assertThat(result).isNotNull();
        assertThat(result.getDiagnosis()).isEqualTo(diagnosisDTO.getDiagnosis());
        assertThat(result.getDisease().getId()).isEqualTo(icd10Disease.getId());
        assertThat(result.getDisease().getName()).isEqualTo(icd10Disease.getName());

        assertThat(medicalAuthorization.getDiagnosis()).isEqualTo(diagnosisDTO.getDiagnosis());
        assertThat(medicalAuthorization.getDisease()).isEqualTo(icd10Disease);

        verify(medicalAuthorizationSupportService, times(1)).publishDiagnosisUpdateEventAndNotifyAuditors(medicalAuthorization);
    }

    @Test
    public void testUpdateAuthorizationDiagnosisOkWhenPartiallyApproved() throws ObjectNotFoundException, ObjectNotValidException {
        testUpdateAuthorizationDiagnosisOk(VALIDATION_PARTIALLY_APPROVED.getInstance());
    }

    @Test
    public void testReceiveMessageThrowsExceptionWhenNotPending() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        medicalAuthorization.setStatus(VALIDATION_APPROVED.getInstance());

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.receiveMessage(1L, new MessageDTO()));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.notPending");
    }

    @Test
    public void testReceiveMessageOK() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);


        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setStatus(VALIDATION_PENDING.getInstance());
        medicalAuthorization.setMessages(new HashSet<>());
        int originalSize = medicalAuthorization.getMessages().size();


        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setText("text");


        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getPrincipal()).thenReturn("username");

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);
        when(medicalAuthorizationRepository.save(medicalAuthorization)).thenReturn(medicalAuthorization);


        Set<Message> result = medicalAuthorizationService.receiveMessage(1L, messageDTO);


        SecurityContextHolder.setContext(defaultContext);

        Message resultMessage = result.iterator().next();
        assertThat(resultMessage.getFrom()).isEqualTo("username");
        assertThat(resultMessage.getText()).isEqualTo(messageDTO.getText());

        assertThat(medicalAuthorization.getMessages().size()).isEqualTo(originalSize + 1);

        verify(medicalAuthorizationSupportService, times(1)).publishNewMessageEventAndNotifyAuditors(medicalAuthorization);
    }

    @Test
    public void testDumpAllMessagesOK() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setMessages(new HashSet<>());

        doReturn(medicalAuthorization).when(medicalAuthorizationService).findById(1L);

        Set<Message> result = medicalAuthorizationService.dumpAllMessages(1L);

        assertThat(result).isEqualTo(medicalAuthorization.getMessages());
    }

}
