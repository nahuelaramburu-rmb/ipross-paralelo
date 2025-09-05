package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.MedicalCenterContract;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import com.capacidad.validationapi.module.contract.service.ContractService;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.model.StatusScope;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.reference.MedicalSpecialtyReference;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.practitioner.dto.PractitionerDTO;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.practitioner.repository.PractitionerRepository;
import com.capacidad.validationapi.module.practitioner.service.MedicalRegistrationService;
import com.capacidad.validationapi.module.practitioner.service.MedicalSpecialtyService;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.rating.Rating;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hashids.Hashids;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerServiceImplTest {

    @Mock
    private IdentityClientService identityClientService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private MedicalSpecialtyService medicalSpecialtyService;

    @Mock
    private MedicalCenterService medicalCenterService;

    @Mock
    private MedicalRegistrationService medicalRegistrationService;

    @Mock
    private Utils utils;

    @Mock
    private Hashids hashids;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private PractitionerRepository practitionerRepository;

    @Mock
    private ContractService contractService;

    @Spy
    @InjectMocks
    private PractitionerServiceImpl practitionerService;

    @Test
    public void testCanPerformMedicalPracticeIsFalseWhenPractitionerSpecialtyIsInvalid() {
        Practitioner practitioner = new Practitioner();

        MedicalSpecialty invalidMedicalSpecialty = new MedicalSpecialty();
        invalidMedicalSpecialty.setId(1L);

        MedicalSpecialty allSpecialty = new MedicalSpecialty();
        allSpecialty.setId(MedicalSpecialtyReference.ALL.getId());

        Set<MedicalSpecialty> practitionerMedicalSpecialties = new HashSet<>();
        practitionerMedicalSpecialties.add(invalidMedicalSpecialty);

        MedicalSpecialty relatedMedicalSpecialty = new MedicalSpecialty();
        relatedMedicalSpecialty.setId(2L);
        Set<MedicalSpecialty> relatedMedicalSpecialties = new HashSet<>();
        relatedMedicalSpecialties.add(relatedMedicalSpecialty);

        practitioner.setMedicalSpecialties(practitionerMedicalSpecialties);
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setId(1L);

        when(practitionerService.getUtils()).thenReturn(utils);
        when(utils.getGenericsEntityReference(MedicalSpecialty.class, allSpecialty.getId())).thenReturn(allSpecialty);
        when(medicalSpecialtyService.getMedicalPracticeSpecialties(medicalPractice.getId())).thenReturn(relatedMedicalSpecialties);

        boolean result = practitionerService.canPerformMedicalPractice(practitioner, medicalPractice);

        assertThat(result).isFalse();
    }

    @Test
    public void testCanPerformMedicalPracticeIsTrueWhenPractitionerSpecialtyIsValid() {
        Practitioner practitioner = new Practitioner();

        MedicalSpecialty validMedicalSpecialty = new MedicalSpecialty();
        validMedicalSpecialty.setId(1L);

        MedicalSpecialty allSpecialty = new MedicalSpecialty();
        allSpecialty.setId(MedicalSpecialtyReference.ALL.getId());

        Set<MedicalSpecialty> practitionerMedicalSpecialties = new HashSet<>();
        practitionerMedicalSpecialties.add(validMedicalSpecialty);

        practitioner.setMedicalSpecialties(practitionerMedicalSpecialties);
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setId(1L);

        when(practitionerService.getUtils()).thenReturn(utils);
        when(utils.getGenericsEntityReference(MedicalSpecialty.class, allSpecialty.getId())).thenReturn(allSpecialty);
        when(medicalSpecialtyService.getMedicalPracticeSpecialties(medicalPractice.getId())).thenReturn(practitionerMedicalSpecialties);

        boolean result = practitionerService.canPerformMedicalPractice(practitioner, medicalPractice);

        assertThat(result).isTrue();
    }

    @Test
    public void testCanPerformMedicalPracticeIsTrueWhenMedicalPracticeContainsGeneralAllRelatedSpecialty() {
        Practitioner practitioner = new Practitioner();

        MedicalSpecialty allSpecialty = new MedicalSpecialty();
        allSpecialty.setId(MedicalSpecialtyReference.ALL.getId());

        Set<MedicalSpecialty> practitionerMedicalSpecialties = new HashSet<>();
        practitionerMedicalSpecialties.add(allSpecialty);

        practitioner.setMedicalSpecialties(practitionerMedicalSpecialties);
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setId(1L);

        when(practitionerService.getUtils()).thenReturn(utils);
        when(utils.getGenericsEntityReference(MedicalSpecialty.class, allSpecialty.getId())).thenReturn(allSpecialty);
        when(medicalSpecialtyService.getMedicalPracticeSpecialties(medicalPractice.getId())).thenReturn(practitionerMedicalSpecialties);

        boolean result = practitionerService.canPerformMedicalPractice(practitioner, medicalPractice);

        assertThat(result).isTrue();
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testAssociateContractFailsWhenPractitionerAlreadyOwnsIt() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        Contract contract = new Contract();
        contract.setId(1L);

        practitioner.getContracts().add(contract);

        when(contractService.findById(anyLong())).thenReturn(contract);
        doReturn(practitioner).when(practitionerService).findById(anyLong());

        practitionerService.associateContract(anyLong(), anyLong());
    }

    @Test
    public void testAssociateOrganizationContractFailsWhenPractitionerDoNotBelongsToOrganization() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(3L);

        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setId(1L);

        Organization organization = new Organization();
        organization.setId(2L);

        organizationContract.setOrganization(organization);

        when(contractService.findById(organizationContract.getId())).thenReturn(organizationContract);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(medicalRegistrationService.practitionerBelongsOrganization(practitioner.getId(), organization.getId())).thenReturn(false);

        RuntimeException exception = (RuntimeException) catchThrowable(() -> practitionerService.associateContract(practitioner.getId(), organizationContract.getId()));

        assertThat(exception.getMessage()).contains("practitioner.invalidOrganizationContract");
        assertThat(exception.getMessage()).contains(ObjectNotValidException.class.getName());
    }

    @Test
    public void testAssociateOrganizationContractDoNotFailsWhenPractitionerBelongsToOrganization() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(3L);

        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setId(1L);

        Organization organization = new Organization();
        organization.setId(2L);

        organizationContract.setOrganization(organization);

        when(contractService.findById(organizationContract.getId())).thenReturn(organizationContract);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);
        when(medicalRegistrationService.practitionerBelongsOrganization(practitioner.getId(), organization.getId())).thenReturn(true);

        PractitionerProjection result = practitionerService.associateContract(practitioner.getId(), organizationContract.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getContracts().size()).isEqualTo(1);
    }

    @Test
    public void testAssociateMedicalCenterContractDoNotFailsWhenPractitionerBelongsToMedicalCenter() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(3L);

        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();
        medicalCenterContract.setId(1L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(2L);

        medicalCenterContract.setMedicalCenter(medicalCenter);

        practitioner.getMedicalCenters().add(medicalCenter);

        when(contractService.findById(medicalCenterContract.getId())).thenReturn(medicalCenterContract);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.associateContract(practitioner.getId(), medicalCenterContract.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getContracts().size()).isEqualTo(1);
    }

    @Test
    public void testAssociatePractitionerContractFailsWhenPractitionerIsNotContractOwner() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(3L);

        PractitionerContract practitionerContract = new PractitionerContract();
        practitionerContract.setId(1L);

        Practitioner practitioner1 = new Practitioner();
        practitioner1.setId(4L);

        practitionerContract.setPractitioner(practitioner1);

        when(contractService.findById(practitionerContract.getId())).thenReturn(practitionerContract);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());

        RuntimeException exception = (RuntimeException) catchThrowable(() -> practitionerService.associateContract(practitioner.getId(), practitionerContract.getId()));

        assertThat(exception.getMessage()).contains("practitioner.invalidPractitionerContract");
        assertThat(exception.getMessage()).contains(ObjectNotValidException.class.getName());
    }

    @Test
    public void testAssociatePractitionerContractDoNotFailsWhenPractitionerIsContractOwner() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(3L);

        PractitionerContract practitionerContract = new PractitionerContract();
        practitionerContract.setId(1L);

        practitionerContract.setPractitioner(practitioner);

        when(contractService.findById(practitionerContract.getId())).thenReturn(practitionerContract);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.associateContract(practitioner.getId(), practitionerContract.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getContracts().size()).isEqualTo(1);
    }

    @Test
    public void testAssociateBaseContractDoNotFails() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(3L);

        Contract baseContract = new Contract();
        baseContract.setId(2L);

        when(contractService.findById(baseContract.getId())).thenReturn(baseContract);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.associateContract(practitioner.getId(), baseContract.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getContracts().size()).isEqualTo(1);
    }

    @Test
    public void testDisassociateContractFailsWhenPractitionerDoesNotOwnsIt() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        Contract contract = new Contract();
        contract.setId(1L);

        Contract contract1 = new Contract();
        contract1.setId(1L);

        Contract contract2 = new Contract();
        contract2.setId(1L);

        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);

        when(contractService.findById(contract2.getId())).thenReturn(contract2);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> practitionerService.dissociateContract(practitioner.getId(), contract2.getId()));

        assertThat(exception.getMessage()).isEqualTo("practitioner.invalidContract");
    }

    @Test
    public void testDisassociateContractDoNotFailsWhenPractitionerOwnsIt() throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        Contract contract = new Contract();
        contract.setId(1L);

        Contract contract1 = new Contract();
        contract1.setId(1L);

        Contract contract2 = new Contract();
        contract2.setId(1L);

        practitioner.getContracts().add(contract);
        practitioner.getContracts().add(contract1);
        practitioner.getContracts().add(contract2);

        when(contractService.findById(contract2.getId())).thenReturn(contract2);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.dissociateContract(practitioner.getId(), contract2.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getContracts().size()).isEqualTo(2);
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testAssociateMedicalCenterFailsWhenPractitionerAlreadyBelongsToIt() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        practitioner.getMedicalCenters().add(medicalCenter);

        when(medicalCenterService.findById(medicalCenter.getId())).thenReturn(medicalCenter);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());

        practitionerService.associateMedicalCenter(practitioner.getId(), medicalCenter.getId());
    }

    @Test
    public void testAssociateMedicalCenterDoNotFailWhenPractitionerDoNotBelongsToIt() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        when(medicalCenterService.findById(medicalCenter.getId())).thenReturn(medicalCenter);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.associateMedicalCenter(practitioner.getId(), medicalCenter.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getMedicalCenters().size()).isEqualTo(1);
    }

    @Test
    public void testDisassociateMedicalCenterFailsWhenPractitionerDoestNotBelongsToIt() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenter medicalCenter2 = new MedicalCenter();
        medicalCenter2.setId(1L);

        MedicalCenter medicalCenter3 = new MedicalCenter();
        medicalCenter3.setId(1L);

        practitioner.getMedicalCenters().add(medicalCenter);
        practitioner.getMedicalCenters().add(medicalCenter2);

        when(medicalCenterService.findById(medicalCenter3.getId())).thenReturn(medicalCenter3);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> practitionerService.dissociateMedicalCenter(practitioner.getId(), medicalCenter3.getId()));

        assertThat(exception.getMessage()).isEqualTo("practitioner.invalidMedicalCenter");
    }

    @Test
    public void testDisassociateMedicalCenterFailsWhenPractitionerBelongsToOnlyOneMedicalCenter() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        practitioner.getMedicalCenters().add(medicalCenter);

        when(medicalCenterService.findById(medicalCenter.getId())).thenReturn(medicalCenter);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> practitionerService.dissociateMedicalCenter(practitioner.getId(), medicalCenter.getId()));

        assertThat(exception.getMessage()).isEqualTo("practitioner.medicalCenterRequirement");
    }

    @Test
    public void testDisassociateMedicalCenterDoNotFailWhenPractitionerBelongsToIt() throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenter medicalCenter2 = new MedicalCenter();
        medicalCenter2.setId(1L);

        practitioner.getMedicalCenters().add(medicalCenter);
        practitioner.getMedicalCenters().add(medicalCenter2);

        when(medicalCenterService.findById(medicalCenter2.getId())).thenReturn(medicalCenter2);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.dissociateMedicalCenter(practitioner.getId(), medicalCenter2.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getMedicalCenters().size()).isEqualTo(1);
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testAssociateMedicalSpecialtyFailsWhenPractitionerAlreadyContainsMedicalSpecialty() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalSpecialty medicalSpecialty = new MedicalSpecialty();
        medicalSpecialty.setId(1L);

        practitioner.getMedicalSpecialties().add(medicalSpecialty);

        when(medicalSpecialtyService.findById(medicalSpecialty.getId())).thenReturn(medicalSpecialty);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());

        practitionerService.associateMedicalSpecialty(practitioner.getId(), medicalSpecialty.getId());
    }

    @Test
    public void testAssociateMedicalSpecialtyDoNotFailWhenPractitionerDoesNotContainMedicalSpecialty() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalSpecialty medicalSpecialty = new MedicalSpecialty();
        medicalSpecialty.setId(1L);

        when(medicalSpecialtyService.findById(anyLong())).thenReturn(medicalSpecialty);
        doReturn(practitioner).when(practitionerService).findById(anyLong());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.associateMedicalSpecialty(anyLong(), anyLong());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getMedicalSpecialties().size()).isEqualTo(1);
    }

    @Test
    public void testDisassociateMedicalSpecialtyFailsWhenPractitionerDoestNotContainsMedicalSpecialty() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalSpecialty medicalSpecialty = new MedicalSpecialty();
        medicalSpecialty.setId(1L);

        MedicalSpecialty medicalSpecialty2 = new MedicalSpecialty();
        medicalSpecialty2.setId(1L);

        MedicalSpecialty medicalSpecialty3 = new MedicalSpecialty();
        medicalSpecialty3.setId(1L);

        practitioner.getMedicalSpecialties().add(medicalSpecialty);
        practitioner.getMedicalSpecialties().add(medicalSpecialty2);

        when(medicalSpecialtyService.findById(medicalSpecialty3.getId())).thenReturn(medicalSpecialty3);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());


        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> practitionerService.dissociateMedicalSpecialty(practitioner.getId(), medicalSpecialty3.getId()));

        assertThat(exception.getMessage()).isEqualTo("practitioner.invalidSpecialty");
    }

    @Test
    public void testDisassociateMedicalSpecialtyFailsWhenPractitionerContainsOnlyOneMedicalSpecialty() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalSpecialty medicalSpecialty = new MedicalSpecialty();
        medicalSpecialty.setId(1L);

        practitioner.getMedicalSpecialties().add(medicalSpecialty);

        when(medicalSpecialtyService.findById(medicalSpecialty.getId())).thenReturn(medicalSpecialty);
        doReturn(practitioner).when(practitionerService).findById(anyLong());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> practitionerService.dissociateMedicalSpecialty(practitioner.getId(), medicalSpecialty.getId()));

        assertThat(exception.getMessage()).isEqualTo("practitioner.specialtiesRequirement");
    }

    @Test
    public void testDisassociateMedicalSpecialtyDoNotFailWhenPractitionerContainsMoreThanOneMedicalSpecialty() throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);

        MedicalSpecialty medicalSpecialty = new MedicalSpecialty();
        medicalSpecialty.setId(1L);

        MedicalSpecialty medicalSpecialty2 = new MedicalSpecialty();
        medicalSpecialty2.setId(2L);

        practitioner.getMedicalSpecialties().add(medicalSpecialty);
        practitioner.getMedicalSpecialties().add(medicalSpecialty2);

        when(medicalSpecialtyService.findById(medicalSpecialty2.getId())).thenReturn(medicalSpecialty2);
        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.dissociateMedicalSpecialty(practitioner.getId(), medicalSpecialty2.getId());

        verify(practitionerRepository, times(1)).save(practitioner);
        assertThat(result).isNotNull();
        assertThat(practitioner.getMedicalSpecialties().size()).isEqualTo(1);
    }

    @Test
    public void testCreatePractitionerWithEmptyContractIsSuccessful() throws ObjectNotValidException, ObjectNotFoundException {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        Practitioner practitioner = new Practitioner();
        practitioner.setId(111111L);

        String stringKey = StringUtils.join(practitioner.getId(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        String expectedKey = "expectedKey";

        when(practitionerService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(practitionerDTO, Practitioner.class)).thenReturn(practitioner);
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);
        when(hashids.encode(Long.parseLong(stringKey))).thenReturn(expectedKey);

        Practitioner result = practitionerService.create(practitionerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus().getId()).isEqualTo(AVAILABLE.getId());
        assertThat(result.getTransactionKey()).isEqualTo(expectedKey);
    }

    @Test
    public void testCreatePractitionerWithNullContractIsSuccessful() throws ObjectNotValidException, ObjectNotFoundException {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        Practitioner practitioner = new Practitioner();
        practitioner.setId(54321L);
        practitioner.setContracts(null);

        String stringKey = StringUtils.join(practitioner.getId(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        String expectedKey = "expectedKey";

        when(practitionerService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(practitionerDTO, Practitioner.class)).thenReturn(practitioner);
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);
        when(hashids.encode(Long.parseLong(stringKey))).thenReturn(expectedKey);


        Practitioner result = practitionerService.create(practitionerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus().getId()).isEqualTo(AVAILABLE.getId());
        assertThat(result.getTransactionKey()).isEqualTo(expectedKey);
    }

    @Test
    public void testCreatePractitionerWithBaseContractIsSuccessful() throws ObjectNotValidException, ObjectNotFoundException {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        Practitioner practitioner = new Practitioner();
        practitioner.setId(123456L);
        practitioner.getContracts().add(new Contract());

        String stringKey = StringUtils.join(practitioner.getId(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        String expectedKey = "expectedKey";

        when(practitionerService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(practitionerDTO, Practitioner.class)).thenReturn(practitioner);
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);
        when(hashids.encode(Long.parseLong(stringKey))).thenReturn(expectedKey);

        Practitioner result = practitionerService.create(practitionerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus().getId()).isEqualTo(AVAILABLE.getId());
        assertThat(result.getTransactionKey()).isEqualTo(expectedKey);
    }

    @Test
    public void testAppendCustomSpecificationReturnsEmptyWhenRoleIsAdmin() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_ADMIN_INSTANCE);

        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Optional<Specification<Practitioner>> result = practitionerService.appendCustomSpecification();

        assertThat(result).isEmpty();
    }

    @Test
    public void testAppendCustomSpecificationReturnsValidWhenRoleIsMedicalCenter() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_MEDICAL_CENTER_INSTANCE);

        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Optional<Specification<Practitioner>> result = practitionerService.appendCustomSpecification();

        assertThat(result).isPresent();
    }

    @Test
    public void testAppendCustomSpecificationReturnsValidWhenRoleIsOrganization() {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_ORGANIZATION_INSTANCE);

        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        Optional<Specification<Practitioner>> result = practitionerService.appendCustomSpecification();

        assertThat(result).isPresent();
    }

    @Test(expected = ObjectNotValidException.class)
    public void testUpdateStatusThrowsExceptionWhenInvalidStatusScope() throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());
        StatusScope statusScope = new StatusScope();
        statusScope.setId(VALIDATION_APPROVED.getStatusScopeReference().getId());
        approved.setStatusScope(statusScope);

        IdDTO<Long> statusIdDTO = new IdDTO<>();
        statusIdDTO.setId(approved.getId());
        statusUpdateDTO.setStatus(statusIdDTO);

        doReturn(practitioner).when(practitionerService).findById(1L);
        doReturn(utils).when(practitionerService).getUtils();
        when(utils.getGenericsEntityReference(Status.class, approved.getId())).thenReturn(approved);

        practitionerService.updateStatus(1L, statusUpdateDTO);
    }

    @Test
    public void testUpdateStatusUpdatesSuccessfullyWhenValidStatusScope() throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
        statusUpdateDTO.setStatusUpdateDescription("updated");

        Status disabled = new Status();
        disabled.setId(DISABLED.getId());
        StatusScope statusScope = new StatusScope();
        statusScope.setId(DISABLED.getStatusScopeReference().getId());
        disabled.setStatusScope(statusScope);

        IdDTO<Long> statusIdDTO = new IdDTO<>();
        statusIdDTO.setId(disabled.getId());
        statusUpdateDTO.setStatus(statusIdDTO);

        doReturn(practitioner).when(practitionerService).findById(1L);
        doReturn(utils).when(practitionerService).getUtils();
        when(utils.getGenericsEntityReference(Status.class, disabled.getId())).thenReturn(disabled);
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);

        PractitionerProjection result = practitionerService.updateStatus(1L, statusUpdateDTO);

        assertThat(result).isNotNull();
    }

    @Test
    public void testAddRatingWhenPractitionerNotRatedYetIsSuccessful() {
        Practitioner practitioner = new Practitioner();

        Rating rating = new Rating();
        rating.setAverage(new BigDecimal("3.3"));
        rating.setWaitTime(new BigDecimal(3));
        rating.setDuration(new BigDecimal(2));
        rating.setCharges(new BigDecimal(5));
        rating.setQuality(new BigDecimal(3));
        rating.setQuantity(1);

        practitionerService.addRating(practitioner, rating);

        Rating result = practitioner.getRating();

        assertThat(result.getQuantity()).isEqualTo(1);
        assertThat(result.getCharges()).isEqualTo(rating.getCharges());
        assertThat(result.getAverage()).isEqualTo(rating.getAverage());
        assertThat(result.getDuration()).isEqualTo(rating.getDuration());
        assertThat(result.getQuality()).isEqualTo(rating.getQuality());
        assertThat(result.getWaitTime()).isEqualTo(rating.getWaitTime());
    }

    @Test
    public void testAddRatingAppendsToPractitionerWhenAlreadyRatedIsSuccessful() {
        Practitioner practitioner = new Practitioner();

        Rating currentRating = new Rating();
        currentRating.setAverage(new BigDecimal("3.75"));
        currentRating.setWaitTime(new BigDecimal(4));
        currentRating.setDuration(new BigDecimal("4.25"));
        currentRating.setCharges(new BigDecimal("2.5"));
        currentRating.setQuality(new BigDecimal(3));
        currentRating.setQuantity(4);

        practitioner.setRating(currentRating);

        Rating newRating = new Rating();
        newRating.setAverage(new BigDecimal("3.5"));
        newRating.setWaitTime(new BigDecimal(5));
        newRating.setDuration(new BigDecimal(1));
        newRating.setCharges(new BigDecimal(5));
        newRating.setQuality(new BigDecimal(5));
        newRating.setQuantity(1);

        practitionerService.addRating(practitioner, newRating);

        Rating result = practitioner.getRating();

        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getAverage()).isEqualTo(new BigDecimal("3.7"));
        assertThat(result.getWaitTime()).isEqualTo(new BigDecimal("4.2"));
        assertThat(result.getDuration()).isEqualTo(new BigDecimal("3.6"));
        assertThat(result.getCharges()).isEqualTo(new BigDecimal("3.0"));
        assertThat(result.getQuality()).isEqualTo(new BigDecimal("3.4"));
    }

    @Test
    public void testRemoveRatingDoNothingWhenNullRating() {
        Practitioner practitioner = new Practitioner();

        Rating rating = new Rating();
        rating.setAverage(new BigDecimal("3.3"));
        rating.setWaitTime(new BigDecimal(3));
        rating.setDuration(new BigDecimal(2));
        rating.setCharges(new BigDecimal(5));
        rating.setQuality(new BigDecimal(3));
        rating.setQuantity(1);

        practitioner.setRating(rating);

        practitionerService.removeRating(practitioner, null);

        Rating result = practitioner.getRating();

        assertThat(result.getQuantity()).isEqualTo(1);
        assertThat(result.getCharges()).isEqualTo(rating.getCharges());
        assertThat(result.getAverage()).isEqualTo(rating.getAverage());
        assertThat(result.getDuration()).isEqualTo(rating.getDuration());
        assertThat(result.getQuality()).isEqualTo(rating.getQuality());
        assertThat(result.getWaitTime()).isEqualTo(rating.getWaitTime());
    }

    @Test
    public void testRemoveRatingSetsNullRatingWheQuantityEqualsOne() {
        Practitioner practitioner = new Practitioner();

        Rating rating = new Rating();
        rating.setAverage(new BigDecimal("3.3"));
        rating.setWaitTime(new BigDecimal(3));
        rating.setDuration(new BigDecimal(2));
        rating.setCharges(new BigDecimal(5));
        rating.setQuality(new BigDecimal(3));
        rating.setQuantity(1);

        practitioner.setRating(rating);

        practitionerService.removeRating(practitioner, new Rating());

        Rating result = practitioner.getRating();

        assertThat(result).isNull();
    }

    @Test
    public void testRemoveRatingUpdateValuesWhenNotNullRatingAndQuantityBiggerThanOne() {
        Practitioner practitioner = new Practitioner();

        Rating currentRating = new Rating();
        currentRating.setAverage(new BigDecimal("3.75"));
        currentRating.setWaitTime(new BigDecimal(4));
        currentRating.setDuration(new BigDecimal("4.25"));
        currentRating.setCharges(new BigDecimal("2.5"));
        currentRating.setQuality(new BigDecimal(3));
        currentRating.setQuantity(4);

        practitioner.setRating(currentRating);

        Rating rating = new Rating();
        rating.setAverage(new BigDecimal("2.7"));
        rating.setWaitTime(new BigDecimal(4));
        rating.setDuration(new BigDecimal(3));
        rating.setCharges(new BigDecimal(0));
        rating.setQuality(new BigDecimal(1));
        rating.setQuantity(1);

        practitionerService.removeRating(practitioner, rating);

        Rating result = practitioner.getRating();

        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getCharges()).isEqualTo(new BigDecimal("3.3"));
        assertThat(result.getAverage()).isEqualTo(new BigDecimal("4.1"));
        assertThat(result.getDuration()).isEqualTo(new BigDecimal("4.7"));
        assertThat(result.getQuality()).isEqualTo(new BigDecimal("3.7"));
        assertThat(result.getWaitTime()).isEqualTo(new BigDecimal("4.0"));
    }

    @Test
    public void testDeleteThrowsExceptionWhenContractAttached() throws ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        PractitionerContract practitionerContract = new PractitionerContract();
        practitionerContract.setName("practitionerContract");
        practitionerContract.setPractitioner(practitioner);

        practitioner.setContract(practitionerContract);

        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> practitionerService.delete(practitioner.getId()));

        assertThat(exception.getMessage()).isEqualTo("contractShouldBeDeletedFirst");
    }

    @Test
    public void testDeleteExecutesSuccessfullyWhenNotContractAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        practitioner.setContract(null);

        testDeleteExecutesSuccessfully(practitioner);
    }

    @Test
    public void testDeleteExecutesSuccessfullyWhenDeletedContractAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        PractitionerContract practitionerContract = new PractitionerContract();
        practitionerContract.setDeleted(true);
        practitioner.setContract(practitionerContract);

        testDeleteExecutesSuccessfully(practitioner);
    }

    private void testDeleteExecutesSuccessfully(Practitioner practitioner) throws ObjectNotValidException, ObjectNotFoundException {
        Address address = new Address();
        practitioner.setAddress(address);

        practitioner.getMedicalCenters().add(new MedicalCenter());
        practitioner.getContracts().add(new Contract());
        practitioner.getMedicalRegistrations().add(new MedicalRegistration());

        BatchItem batchItem = new BatchItem();
        batchItem.getPractitioners().add(practitioner);
        practitioner.getBatchItems().add(batchItem);

        PractitionerBudget practitionerBudget = new PractitionerBudget();
        practitionerBudget.setStatus(NOT_PAYED.getInstance());
        practitioner.getBudgets().add(practitionerBudget);

        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        preMedicalAuthorization.setStatus(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getInstance());
        preMedicalAuthorization.setPetitioner(practitioner);

        practitioner.getPreMedicalAuthorizations().add(preMedicalAuthorization);
        practitioner.getMedicalSpecialties().add(new MedicalSpecialty());

        doReturn(practitioner).when(practitionerService).findById(practitioner.getId());
        doReturn(new ObjectMapper()).when(practitionerService).getObjectMapper();
        doReturn(applicationEventPublisher).when(practitionerService).getApplicationEventPublisher();

        JsonNode result = practitionerService.delete(practitioner.getId());

        assertThat(result.get("id").asLong()).isEqualTo(practitioner.getId());
        assertThat(practitioner.getDeleted()).isTrue();
        assertThat(practitioner.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(practitioner.getAddress().getDeleted()).isTrue();
        assertThat(practitioner.getAddress().getDeletionToken()).isEqualTo(practitioner.getDeletionToken());
        assertThat(practitioner.getMedicalCenters()).isEmpty();
        assertThat(practitioner.getMedicalRegistrations()).isEmpty();
        assertThat(practitioner.getContracts()).isEmpty();
        assertThat(batchItem.getPractitioners()).isEmpty();
        assertThat(practitionerBudget.isPayed()).isTrue();
        assertThat(practitionerBudget.getClosedAt()).isNotNull();
        assertThat(preMedicalAuthorization.getPetitioner()).isNull();
        assertThat(practitioner.getMedicalSpecialties()).isEmpty();

        verify(practitionerRepository, times(1)).save(practitioner);
        verify(identityClientService, times(1)).deleteResourceIdAccounts(practitioner.getResourceId());
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

}
