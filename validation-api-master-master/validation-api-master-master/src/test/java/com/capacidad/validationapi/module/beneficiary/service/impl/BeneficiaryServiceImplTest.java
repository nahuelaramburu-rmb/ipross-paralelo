package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryDTO;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryRelationshipDTO;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryRelativeDTO;
import com.capacidad.validationapi.module.beneficiary.model.*;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference;
import com.capacidad.validationapi.module.beneficiary.repository.BeneficiaryRepository;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryInsurancePlanService;
import com.capacidad.validationapi.module.budget.model.BeneficiaryBudget;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.model.StatusScope;
import com.capacidad.validationapi.module.general.reference.StatusScopeReference;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlanType;
import com.capacidad.validationapi.module.insuranceplan.reference.InsurancePlanTypeReference;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.model.Phone;
import com.capacidad.validationapi.module.person.model.RelationshipType;
import com.capacidad.validationapi.module.person.reference.RelationshipTypeReference;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnion;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static com.capacidad.validationapi.module.person.reference.IdTypeReference.TEMPORARY_ID;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryServiceImplTest {

    private final ObjectMapper objectMapperInstance = new ObjectMapper();

    @Mock
    private IdentityClientService identityClientService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private BeneficiaryInsurancePlanService beneficiaryInsurancePlanService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Utils utils;

    @Mock
    private Properties properties;

    @Mock
    private PropertiesService propertiesService;

    @Mock
    private TradeUnionService tradeUnionService;

    @Spy
    @InjectMocks
    private BeneficiaryServiceImpl beneficiaryService;

    @Before
    public void init() {
        doNothing().when(beneficiaryInsurancePlanService).validate(anySet());
        when(propertiesService.getProperties()).thenReturn(properties);
    }

    @Test
    public void testCreateThrowsObjectNotValidExceptionWhenBeneficiaryIsUnderAge() {
        Beneficiary beneficiary = new Beneficiary();
        BeneficiaryDTO beneficiaryDTO = new BeneficiaryDTO();
        beneficiary.setIdType(new IdType());
        beneficiary.setIdNumber(12345L);

        beneficiary.setBirthDate(LocalDate.now().minusYears(15));

        doReturn(beneficiary).when(beneficiaryService).mapDtoToInput(beneficiaryDTO);

        when(properties.getHolderBeneficiaryMinAge()).thenReturn(18);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.create(beneficiaryDTO));

        assertThat(objectNotValidException.getMessage()).contains("beneficiary.underAge");
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenCompanyIsNullAndPaymentMethodIsPaycheck() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdType(new IdType());
        beneficiary.setIdNumber(12345L);

        beneficiary.setBirthDate(LocalDate.now().minusYears(40));

        PaymentMethod paymentMethodPaycheck = new PaymentMethod();
        paymentMethodPaycheck.setId(PaymentMethodReference.PAYCHECK.getId());

        beneficiary.setPaymentMethod(paymentMethodPaycheck);
        beneficiary.setCompany(null);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.validate(beneficiary));

        assertThat(objectNotValidException.getMessage()).contains("beneficiary.invalidPaymentMethod");
    }


    @Test
    public void testValidateDoNotFailWhenCompanyIsNotNullAndPaymentMethodIsPaycheckAndValidPlans() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdType(new IdType());
        beneficiary.setIdNumber(12345L);

        beneficiary.setBirthDate(LocalDate.now().minusYears(40));

        PaymentMethod paymentMethodPaycheck = new PaymentMethod();
        paymentMethodPaycheck.setId(PaymentMethodReference.PAYCHECK.getId());

        beneficiary.setPaymentMethod(paymentMethodPaycheck);
        beneficiary.setCompany(new Company());

        InsurancePlanType special = new InsurancePlanType();
        special.setId(InsurancePlanTypeReference.SPECIAL.getId());

        InsurancePlanType normal = new InsurancePlanType();
        normal.setId(InsurancePlanTypeReference.NORMAL.getId());

        InsurancePlan primary = new InsurancePlan();
        primary.setId(1L);
        primary.setInsurancePlanType(special);

        InsurancePlan secondary = new InsurancePlan();
        secondary.setId(2L);
        secondary.setInsurancePlanType(normal);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(primary);

        BeneficiaryInsurancePlan beneficiarySecondaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiarySecondaryInsurancePlan.setInsurancePlan(secondary);

        beneficiary.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);
        beneficiary.getBeneficiaryInsurancePlans().add(beneficiarySecondaryInsurancePlan);

        beneficiaryService.validate(beneficiary);
    }

    @Test
    public void testValidateDoNotFailWhenPaymentMethodIsVoluntary() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdType(new IdType());
        beneficiary.setIdNumber(12345L);

        beneficiary.setBirthDate(LocalDate.now().minusYears(40));

        beneficiary.setPaymentMethod(VOLUNTARY.getInstance());
        beneficiary.setCompany(new Company());

        beneficiaryService.validate(beneficiary);
    }

    @Test
    public void testValidateThrowsExceptionWhenNullIdType() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdType(null);
        beneficiary.setIdNumber(12345L);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.validate(beneficiary));

        assertThat(objectNotValidException.getMessage()).contains("beneficiary.invalidIdNumberOrType");
    }

    @Test
    public void testValidateThrowsExceptionWhenNullIdNumber() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdType(new IdType());
        beneficiary.setIdNumber(null);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.validate(beneficiary));

        assertThat(objectNotValidException.getMessage()).contains("beneficiary.invalidIdNumberOrType");
    }

    @Test(expected = ObjectNotValidException.class)
    public void testValidateUpdateFailsWhenRelativeRelationshipTypeIsHolder() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        RelationshipType holder = new RelationshipType();
        holder.setId(HOLDER.getId());
        beneficiary.setRelationshipType(holder);
        beneficiary.setRelatedBeneficiary(new Beneficiary());

        doNothing().when(beneficiaryService).validate(beneficiary);

        beneficiaryService.validateUpdate(beneficiary);
    }

    @Test
    public void testValidateUpdateReplaceMandatoryDataWithHolderData() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        RelationshipType son = new RelationshipType();
        son.setId(SON.getId());
        beneficiary.setRelationshipType(son);

        Beneficiary holder = new Beneficiary();
        holder.setPaymentMethod(new PaymentMethod());
        holder.setCompany(new Company());
        beneficiary.setRelatedBeneficiary(holder);

        doNothing().when(beneficiaryService).validate(beneficiary);

        beneficiaryService.validateUpdate(beneficiary);

        assertThat(beneficiary.getPaymentMethod()).isEqualTo(holder.getPaymentMethod());
        assertThat(beneficiary.getCompany()).isEqualTo(holder.getCompany());
    }

    @Test
    public void testValidateUpdateReplaceMandatoryFamilyWithHolderData() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary holder = new Beneficiary();
        holder.setId(1L);
        holder.setPaymentMethod(new PaymentMethod());
        holder.setCompany(new Company());
        holder.setRelationshipType(HOLDER.getInstance());

        Beneficiary family1 = new Beneficiary();
        Beneficiary family2 = new Beneficiary();
        List<Beneficiary> relatives = new ArrayList<>();
        relatives.add(family1);
        relatives.add(family2);

        doNothing().when(beneficiaryService).validate(holder);
        when(beneficiaryRepository.findAllByFamilyIdAndIdIsNot(any(), anyLong())).thenReturn(relatives);

        beneficiaryService.validateUpdate(holder);

        assertThat(family1.getPaymentMethod()).isEqualTo(holder.getPaymentMethod());
        assertThat(family1.getCompany()).isEqualTo(holder.getCompany());
        assertThat(family2.getPaymentMethod()).isEqualTo(holder.getPaymentMethod());
        assertThat(family2.getCompany()).isEqualTo(holder.getCompany());

        verify(beneficiaryRepository, times(1)).saveAll(relatives);
    }

    @Test
    public void testValidateUpdateDoNotReplaceMandatoryFamilyWhenNotAHolder() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary son = new Beneficiary();
        son.setId(1L);
        son.setPaymentMethod(new PaymentMethod());
        son.setCompany(new Company());
        son.setRelationshipType(SON.getInstance());

        Beneficiary family1 = new Beneficiary();
        Beneficiary family2 = new Beneficiary();
        List<Beneficiary> relatives = new ArrayList<>();
        relatives.add(family1);
        relatives.add(family2);

        doNothing().when(beneficiaryService).validate(son);

        beneficiaryService.validateUpdate(son);

        assertThat(family1.getPaymentMethod()).isNotEqualTo(son.getPaymentMethod());
        assertThat(family1.getCompany()).isNotEqualTo(son.getCompany());
        assertThat(family2.getPaymentMethod()).isNotEqualTo(son.getPaymentMethod());
        assertThat(family2.getCompany()).isNotEqualTo(son.getCompany());

        verify(beneficiaryRepository, never()).saveAll(relatives);
    }

    @Test
    public void testValidateUpdateDoNotFailsWhenStatusScopeIsValid() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setRelationshipType(HOLDER.getInstance());

        Status statusUpdate = new Status();
        StatusScope statusScope = new StatusScope();
        statusScope.setId(StatusScopeReference.BENEFICIARY.getId());
        statusUpdate.setStatusScope(statusScope);

        beneficiary.setStatus(statusUpdate);

        doNothing().when(beneficiaryService).validate(beneficiary);

        beneficiaryService.validateUpdate(beneficiary);

        verify(beneficiaryService, times(1)).validate(beneficiary);
    }

    @Test
    public void testCreateRelativeThrowsObjectNotValidExceptionWhenRelationshipTypeIsHolder() throws ObjectNotFoundException {
        Beneficiary holder = new Beneficiary();
        holder.setId(1L);

        BeneficiaryRelativeDTO beneficiaryRelativeDto = new BeneficiaryRelativeDTO();
        Beneficiary relative = new Beneficiary();

        RelationshipType relationshipTypeHolder = new RelationshipType();
        relationshipTypeHolder.setId(HOLDER.getId());

        relative.setRelationshipType(relationshipTypeHolder);

        doReturn(holder).when(beneficiaryService).findById(holder.getId());
        when(beneficiaryService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(beneficiaryRelativeDto, Beneficiary.class)).thenReturn(relative);

        ObjectNotValidException objectNotValidException = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.createRelative(holder.getId(), beneficiaryRelativeDto));

        assertThat(objectNotValidException.getMessage()).contains("beneficiary.notAssignableRelationshipType");
    }

    @Test
    public void testCreateRelativeIsSuccessfulWithAttachedHolderData() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary holder = new Beneficiary();
        holder.setId(1L);

        Address address = new Address();
        address.setId(1L);

        PaymentMethod paymentMethodPaycheck = new PaymentMethod();
        paymentMethodPaycheck.setId(PaymentMethodReference.PAYCHECK.getId());

        Phone phone = new Phone();
        phone.setId(1L);

        Company company = new Company();
        company.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(new InsurancePlan());

        holder.setPhone(phone);
        holder.setAddress(address);
        holder.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);
        holder.setPaymentMethod(paymentMethodPaycheck);
        holder.setCompany(company);
        holder.setFamilyId(UUID.randomUUID());
        holder.setRelationshipType(HOLDER.getInstance());

        BeneficiaryRelativeDTO beneficiaryRelativeDto = new BeneficiaryRelativeDTO();
        Beneficiary relative = new Beneficiary();

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.SON.getId());

        relative.setRelationshipType(relationshipType);
        relative.setIdType(new IdType());
        relative.setIdNumber(12345L);

        Status beneficiaryWithCoverage = new Status();
        beneficiaryWithCoverage.setId(BENEFICIARY_WITH_COVERAGE.getId());

        doReturn(holder).when(beneficiaryService).findById(holder.getId());
        when(beneficiaryService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(beneficiaryRelativeDto, Beneficiary.class)).thenReturn(relative);
        when(beneficiaryRepository.save(relative)).thenReturn(relative);

        Beneficiary beneficiaryResult = beneficiaryService.createRelative(holder.getId(), beneficiaryRelativeDto);

        assertThat(beneficiaryResult.getRelatedBeneficiary()).isNotNull();
        assertThat(beneficiaryResult.getRelatedBeneficiary().getId()).isEqualTo(holder.getId());
        assertThat(beneficiaryResult.getPhone()).isNotNull();
        assertThat(beneficiaryResult.getAddress().getId()).isEqualTo(holder.getAddress().getId());
        assertThat(beneficiaryResult.getPaymentMethod().getId()).isEqualTo(holder.getPaymentMethod().getId());
        assertThat(beneficiaryResult.getCompany().getId()).isEqualTo(holder.getCompany().getId());
        assertThat(beneficiaryResult.getFamilyId()).isEqualTo(holder.getFamilyId());
    }

    @Test
    public void testCreateRelativeIsSuccessfulWithOwnedRelativeData() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary holder = new Beneficiary();
        holder.setId(1L);

        Address address = new Address();
        address.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(new InsurancePlan());

        PaymentMethod paymentMethodPaycheck = new PaymentMethod();
        paymentMethodPaycheck.setId(PaymentMethodReference.PAYCHECK.getId());

        Phone phone = new Phone();
        phone.setId(1L);

        Company company = new Company();
        company.setId(1L);

        holder.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);
        holder.setPaymentMethod(paymentMethodPaycheck);
        holder.setCompany(company);
        holder.setFamilyId(UUID.randomUUID());
        holder.setRelationshipType(HOLDER.getInstance());

        BeneficiaryRelativeDTO beneficiaryRelativeDto = new BeneficiaryRelativeDTO();
        Beneficiary relative = new Beneficiary();

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.SON.getId());

        relative.setRelationshipType(relationshipType);
        relative.setPhone(phone);
        relative.setAddress(address);
        relative.setIdType(new IdType());
        relative.setIdNumber(12345L);

        Status beneficiaryWithCoverage = new Status();
        beneficiaryWithCoverage.setId(BENEFICIARY_WITH_COVERAGE.getId());

        doReturn(holder).when(beneficiaryService).findById(holder.getId());
        when(beneficiaryService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(beneficiaryRelativeDto, Beneficiary.class)).thenReturn(relative);
        when(beneficiaryRepository.save(relative)).thenReturn(relative);

        Beneficiary beneficiaryResult = beneficiaryService.createRelative(holder.getId(), beneficiaryRelativeDto);

        assertThat(beneficiaryResult.getRelatedBeneficiary()).isEqualTo(holder);
        assertThat(beneficiaryResult.getPhone()).isEqualTo(phone);
        assertThat(beneficiaryResult.getAddress()).isEqualTo(address);
        assertThat(beneficiaryResult.getPaymentMethod()).isEqualTo(holder.getPaymentMethod());
        assertThat(beneficiaryResult.getCompany().getId()).isEqualTo(holder.getCompany().getId());
        assertThat(beneficiaryResult.getFamilyId()).isEqualTo(holder.getFamilyId());
    }

    @Test
    public void testCreateWithValidValuesIsSuccessful() throws ObjectNotValidException, ObjectNotFoundException {
        BeneficiaryDTO beneficiaryDTO = new BeneficiaryDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(20));

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(PaymentMethodReference.PAYCHECK.getId());
        beneficiary.setPaymentMethod(paymentMethod);
        beneficiary.setCompany(new Company());

        RelationshipType holderRelationshipType = new RelationshipType();
        holderRelationshipType.setId(HOLDER.getId());

        Status beneficiaryWithCoverage = new Status();
        beneficiaryWithCoverage.setId(BENEFICIARY_WITH_COVERAGE.getId());

        when(beneficiaryService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(beneficiaryDTO, Beneficiary.class)).thenReturn(beneficiary);
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
        doNothing().when(beneficiaryService).validate(beneficiary);
        when(properties.getHolderBeneficiaryMinAge()).thenReturn(18);

        Beneficiary result = beneficiaryService.create(beneficiaryDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus().getId()).isEqualTo(beneficiaryWithCoverage.getId());
        assertThat(result.getRelationshipType().getId()).isEqualTo(holderRelationshipType.getId());
    }

    @Test(expected = ObjectNotValidException.class)
    public void testCreateNewbornThrowsExceptionWhenInvalidNewbornBirthDate() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary holder = new Beneficiary();
        holder.setId(1L);

        Beneficiary newborn = new Beneficiary();
        newborn.setBirthDate(LocalDate.now().minusYears(1));
        BeneficiaryRelativeDTO newbornDTO = new BeneficiaryRelativeDTO();

        RelationshipType newbornRelationshipType = new RelationshipType();
        newbornRelationshipType.setId(NEWBORN.getId());
        newborn.setRelationshipType(newbornRelationshipType);

        doReturn(holder).when(beneficiaryService).findById(1L);
        doReturn(objectMapper).when(beneficiaryService).getObjectMapper();
        when(objectMapper.convertValue(newbornDTO, Beneficiary.class)).thenReturn(newborn);

        beneficiaryService.createRelative(1L, newbornDTO);
    }

    @Test
    public void testCreateNewbornReturnSuccessfullyBeneficiaryWhenValidData() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary holder = new Beneficiary();
        holder.setIdNumber(123123123L);
        holder.setId(1L);

        Beneficiary newborn = new Beneficiary();
        newborn.setBirthDate(LocalDate.now().minusDays(1));
        BeneficiaryRelativeDTO newbornDTO = new BeneficiaryRelativeDTO();

        RelationshipType newbornRelationshipType = new RelationshipType();
        newbornRelationshipType.setId(NEWBORN.getId());
        newborn.setRelationshipType(newbornRelationshipType);

        Address address = new Address();
        address.setId(1L);

        BeneficiaryInsurancePlan beneficiaryPrimaryInsurancePlan = new BeneficiaryInsurancePlan();
        beneficiaryPrimaryInsurancePlan.setInsurancePlan(new InsurancePlan());

        PaymentMethod paymentMethodPaycheck = new PaymentMethod();
        paymentMethodPaycheck.setId(PaymentMethodReference.PAYCHECK.getId());

        Phone phone = new Phone();
        phone.setId(1L);

        Company company = new Company();
        company.setId(1L);

        holder.getBeneficiaryInsurancePlans().add(beneficiaryPrimaryInsurancePlan);
        holder.setPaymentMethod(paymentMethodPaycheck);
        holder.setCompany(company);
        holder.setFamilyId(UUID.randomUUID());
        holder.setPhone(phone);
        holder.setAddress(address);
        holder.setRelationshipType(HOLDER.getInstance());

        Status beneficiaryWithCoverage = new Status();
        beneficiaryWithCoverage.setId(BENEFICIARY_WITH_COVERAGE.getId());

        doReturn(holder).when(beneficiaryService).findById(1L);
        doReturn(objectMapper).when(beneficiaryService).getObjectMapper();
        when(objectMapper.convertValue(newbornDTO, Beneficiary.class)).thenReturn(newborn);
        when(beneficiaryRepository.save(newborn)).thenReturn(newborn);

        Beneficiary result = beneficiaryService.createRelative(1L, newbornDTO);

        assertThat(result.getRelatedBeneficiary()).isNotNull();
        assertThat(result.getRelatedBeneficiary().getId()).isEqualTo(holder.getId());
        assertThat(result.getPhone()).isNotNull();
        assertThat(result.getAddress().getId()).isEqualTo(holder.getAddress().getId());
        assertThat(result.getPaymentMethod().getId()).isEqualTo(holder.getPaymentMethod().getId());
        assertThat(result.getCompany().getId()).isEqualTo(holder.getCompany().getId());
        assertThat(result.getFamilyId()).isEqualTo(holder.getFamilyId());
        assertThat(result.getIdType().getId()).isEqualTo(TEMPORARY_ID.getId());
        assertThat(result.getRelationshipType().getId()).isEqualTo(NEWBORN.getId());
        assertThat(result.getIdNumber().toString()).hasSize(holder.getIdNumber().toString().length() + 3);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testUpdateStatusThrowsExceptionWhenInvalidStatusScope() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());
        StatusScope statusScope = new StatusScope();
        statusScope.setId(VALIDATION_APPROVED.getStatusScopeReference().getId());
        approved.setStatusScope(statusScope);

        IdDTO<Long> statusIdDTO = new IdDTO<>();
        statusIdDTO.setId(approved.getId());
        statusUpdateDTO.setStatus(statusIdDTO);

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doReturn(utils).when(beneficiaryService).getUtils();
        when(utils.getEntityReference(Status.class, approved.getId())).thenReturn(approved);

        beneficiaryService.updateStatus(1L, statusUpdateDTO);
    }

    @Test
    public void testUpdateStatusUpdatesSuccessfullyWhenValidStatusScope() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
        statusUpdateDTO.setStatusUpdateDescription("updated");

        Status noCoverage = new Status();
        noCoverage.setId(BENEFICIARY_WITHOUT_COVERAGE.getId());
        StatusScope statusScope = new StatusScope();
        statusScope.setId(BENEFICIARY_WITHOUT_COVERAGE.getStatusScopeReference().getId());
        noCoverage.setStatusScope(statusScope);

        IdDTO<Long> statusIdDTO = new IdDTO<>();
        statusIdDTO.setId(noCoverage.getId());
        statusUpdateDTO.setStatus(statusIdDTO);

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doReturn(utils).when(beneficiaryService).getUtils();
        when(utils.getEntityReference(Status.class, noCoverage.getId())).thenReturn(noCoverage);
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);

        BeneficiaryProjection result = beneficiaryService.updateStatus(1L, statusUpdateDTO);

        assertThat(result).isNotNull();
    }

    @Test
    public void testUpdateRelationshipDoNothingWhenInputHolderAndAlreadyAHolder() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setRelationshipType(HOLDER.getInstance());

        BeneficiaryRelationshipDTO input = new BeneficiaryRelationshipDTO();
        IdDTO<Long> holderRelationshipType = new IdDTO<>();
        holderRelationshipType.setId(HOLDER.getId());
        input.setRelationshipType(holderRelationshipType);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        when(beneficiaryRepository.saveAndFlush(beneficiary)).thenReturn(beneficiary);
        doReturn(new SpelAwareProxyProjectionFactory()).when(beneficiaryService).getProjectionFactory();

        BeneficiaryProjection result = beneficiaryService.updateRelationship(beneficiary.getId(), input);

        assertThat(result).isNotNull();
        verify(beneficiaryService, times(1)).findById(beneficiary.getId());
    }

    @Test
    public void testUpdateRelationshipMakesBeneficiaryAHolderWhenValidInput() throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setRelationshipType(SON.getInstance());

        BeneficiaryRelationshipDTO input = new BeneficiaryRelationshipDTO();
        IdDTO<Long> holderRelationshipType = new IdDTO<>();
        holderRelationshipType.setId(HOLDER.getId());
        input.setRelationshipType(holderRelationshipType);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        when(beneficiaryRepository.saveAndFlush(beneficiary)).thenReturn(beneficiary);
        when(beneficiaryService.getProjectionFactory()).thenReturn(new SpelAwareProxyProjectionFactory());

        BeneficiaryProjection result = beneficiaryService.updateRelationship(beneficiary.getId(), input);

        assertThat(result.getRelatedBeneficiary()).isNull();
        assertThat(result.getRelationshipType().getId()).isEqualTo(HOLDER.getId());
    }

    @Test
    public void testUpdateRelationshipThrowsExceptionWhenRequiredRelatedBeneficiaryIsNull() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setRelationshipType(HOLDER.getInstance());

        BeneficiaryRelationshipDTO input = new BeneficiaryRelationshipDTO();
        IdDTO<Long> holderRelationshipType = new IdDTO<>();
        holderRelationshipType.setId(SON.getId());
        input.setRelationshipType(holderRelationshipType);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.updateRelationship(beneficiary.getId(), input));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.relatedBeneficiaryRequired");
    }

    @Test
    public void testUpdateRelationshipThrowsExceptionWhenHolderToUpdateEqualsInput() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setRelationshipType(HOLDER.getInstance());

        BeneficiaryRelationshipDTO input = new BeneficiaryRelationshipDTO();
        IdDTO<Long> holderRelationshipType = new IdDTO<>();
        holderRelationshipType.setId(SON.getId());
        input.setRelationshipType(holderRelationshipType);
        IdDTO<Long> relateTo = new IdDTO<>();
        relateTo.setId(beneficiary.getId());
        input.setRelatedBeneficiary(relateTo);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.updateRelationship(beneficiary.getId(), input));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.cannotRelateToItself");
    }

    @Test
    public void testUpdateRelationshipThrowsExceptionWhenHolderToUpdateContainsRelatives() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setRelationshipType(HOLDER.getInstance());

        BeneficiaryRelationshipDTO input = new BeneficiaryRelationshipDTO();
        IdDTO<Long> holderRelationshipType = new IdDTO<>();
        holderRelationshipType.setId(SON.getId());
        input.setRelationshipType(holderRelationshipType);
        IdDTO<Long> relateTo = new IdDTO<>();
        relateTo.setId(2L);
        input.setRelatedBeneficiary(relateTo);

        when(beneficiaryRepository.findAllByFamilyIdAndIdIsNot(beneficiary.getFamilyId(), beneficiary.getId())).thenReturn(Collections.singletonList(new Beneficiary()));
        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.updateRelationship(beneficiary.getId(), input));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.cannotRemoveHolderConditionWithRelatives");
    }

    @Test
    public void testUpdateRelationshipUpdatesWhenHolderNowRelatedToNewBeneficiary() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setRelationshipType(HOLDER.getInstance());

        BeneficiaryRelationshipDTO input = new BeneficiaryRelationshipDTO();
        IdDTO<Long> holderRelationshipType = new IdDTO<>();
        holderRelationshipType.setId(SON.getId());
        input.setRelationshipType(holderRelationshipType);
        IdDTO<Long> relateTo = new IdDTO<>();
        relateTo.setId(2L);
        input.setRelatedBeneficiary(relateTo);

        Beneficiary newHolder = new Beneficiary();
        newHolder.setId(input.getRelatedBeneficiary().getId());
        newHolder.setRelationshipType(HOLDER.getInstance());
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(3L);
        newHolder.setPaymentMethod(paymentMethod);
        Company company = new Company();
        company.setId(4L);
        newHolder.setCompany(company);
        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setId(5L);
        newHolder.setBeneficiaryCategory(beneficiaryCategory);

        when(beneficiaryRepository.findAllByFamilyIdAndIdIsNot(beneficiary.getFamilyId(), beneficiary.getId())).thenReturn(Collections.emptyList());
        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        doReturn(newHolder).when(beneficiaryService).findById(input.getRelatedBeneficiary().getId());
        when(beneficiaryService.getUtils()).thenReturn(utils);
        when(utils.getGenericsEntityReference(RelationshipType.class, SON.getId())).thenReturn(SON.getInstance());
        when(beneficiaryRepository.saveAndFlush(beneficiary)).thenReturn(beneficiary);

        BeneficiaryProjection result = beneficiaryService.updateRelationship(beneficiary.getId(), input);

        assertThat(result.getRelationshipType().getId()).isEqualTo(input.getRelationshipType().getId());
        assertThat(result.getRelatedBeneficiary().getId()).isEqualTo(input.getRelatedBeneficiary().getId());
        assertThat(result.getCompany().getId()).isEqualTo(newHolder.getCompany().getId());
        assertThat(result.getPaymentMethod().getId()).isEqualTo(newHolder.getPaymentMethod().getId());
        assertThat(result.getBeneficiaryCategory().getId()).isEqualTo(newHolder.getBeneficiaryCategory().getId());
    }

    @Test
    public void testUpdateRelationshipUpdatesWhenNotHolderNowRelatedToNewBeneficiary() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setRelationshipType(SON.getInstance());

        BeneficiaryRelationshipDTO input = new BeneficiaryRelationshipDTO();
        IdDTO<Long> holderRelationshipType = new IdDTO<>();
        holderRelationshipType.setId(SON.getId());
        input.setRelationshipType(holderRelationshipType);
        IdDTO<Long> relateTo = new IdDTO<>();
        relateTo.setId(2L);
        input.setRelatedBeneficiary(relateTo);

        Beneficiary newHolder = new Beneficiary();
        newHolder.setId(input.getRelatedBeneficiary().getId());
        newHolder.setRelationshipType(HOLDER.getInstance());
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(3L);
        newHolder.setPaymentMethod(paymentMethod);
        Company company = new Company();
        company.setId(4L);
        newHolder.setCompany(company);
        BeneficiaryCategory beneficiaryCategory = new BeneficiaryCategory();
        beneficiaryCategory.setId(5L);
        newHolder.setBeneficiaryCategory(beneficiaryCategory);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        doReturn(newHolder).when(beneficiaryService).findById(input.getRelatedBeneficiary().getId());
        when(beneficiaryService.getUtils()).thenReturn(utils);
        when(utils.getGenericsEntityReference(RelationshipType.class, SON.getId())).thenReturn(SON.getInstance());
        when(beneficiaryRepository.saveAndFlush(beneficiary)).thenReturn(beneficiary);

        BeneficiaryProjection result = beneficiaryService.updateRelationship(beneficiary.getId(), input);

        assertThat(result.getRelationshipType().getId()).isEqualTo(input.getRelationshipType().getId());
        assertThat(result.getRelatedBeneficiary().getId()).isEqualTo(input.getRelatedBeneficiary().getId());
        assertThat(result.getCompany().getId()).isEqualTo(newHolder.getCompany().getId());
        assertThat(result.getPaymentMethod().getId()).isEqualTo(newHolder.getPaymentMethod().getId());
        assertThat(result.getBeneficiaryCategory().getId()).isEqualTo(newHolder.getBeneficiaryCategory().getId());
    }

    @Test
    public void testDeleteThrowsExceptionWhenNonDeletedRelativesAttached() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        when(beneficiaryRepository.findAllByRelatedBeneficiaryId(beneficiary.getId())).thenReturn(Collections.singleton(new Beneficiary()));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.delete(beneficiary.getId()));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.relativesAttached");
    }

    @Test
    public void testDeleteExecutesSuccessfullyWhenNoRelativesAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        when(beneficiaryRepository.findAllByRelatedBeneficiaryId(beneficiary.getId())).thenReturn(Collections.emptySet());

        testDeleteExecutesSuccessfully(beneficiary);
    }

    @Test
    public void testDeleteExecutesSuccessfullyWhenDeletedRelativesAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Beneficiary relative = new Beneficiary();
        relative.setDeleted(true);

        when(beneficiaryRepository.findAllByRelatedBeneficiaryId(beneficiary.getId())).thenReturn(Collections.singleton(relative));

        testDeleteExecutesSuccessfully(beneficiary);
    }

    private void testDeleteExecutesSuccessfully(Beneficiary beneficiary) throws ObjectNotValidException, ObjectNotFoundException {
        beneficiary.getBeneficiaryInsurancePlans().add(new BeneficiaryInsurancePlan());
        beneficiary.getExpirations().add(new Expiration());
        beneficiary.getDiseases().add(new ICD10Disease());

        Batch batch = new Batch();
        batch.setStatus(BATCH_ACTIVE.getInstance());
        beneficiary.getBatches().add(batch);
        beneficiary.setActiveBatch(true);

        BeneficiaryBudget beneficiaryBudget = new BeneficiaryBudget();
        beneficiaryBudget.setStatus(NOT_PAYED.getInstance());
        beneficiary.getBudgets().add(beneficiaryBudget);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        doReturn(objectMapperInstance).when(beneficiaryService).getObjectMapper();
        doReturn(applicationEventPublisher).when(beneficiaryService).getApplicationEventPublisher();

        JsonNode result = beneficiaryService.delete(beneficiary.getId());

        assertThat(result.get("id").asLong()).isEqualTo(beneficiary.getId());
        assertThat(beneficiary.getDeleted()).isTrue();
        assertThat(beneficiary.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(beneficiary.getBeneficiaryInsurancePlans()).isEmpty();
        assertThat(beneficiary.getExpirations()).isEmpty();
        assertThat(beneficiary.getDiseases()).isEmpty();
        assertThat(batch.isCancelled()).isTrue();
        assertThat(beneficiary.hasActiveHealthCoverage()).isFalse();
        assertThat(beneficiary.getActiveBatch()).isFalse();
        assertThat(beneficiaryBudget.isPayed()).isTrue();
        assertThat(beneficiaryBudget.getClosedAt()).isNotNull();

        verify(beneficiaryRepository, times(1)).save(beneficiary);
        verify(identityClientService, times(1)).deleteResourceIdAccounts(beneficiary.getResourceId());
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

    @Test
    public void testAssociateTradeUnionFailsWhenBeneficiaryNotAHolder() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setRelationshipType(DEFAULT_RELATIONSHIP_TYPE.getInstance());

        TradeUnion tradeUnion = new TradeUnion();
        tradeUnion.setId(1L);

        beneficiary.getTradeUnions().add(tradeUnion);

        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.associateTradeUnion(beneficiary.getId(), tradeUnion.getId()));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.cannotAssociateToAnonHolder");
    }

    @Test
    public void testAssociateTradeUnionFailsWhenBeneficiaryAlreadyBelongsToIt() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setRelationshipType(HOLDER.getInstance());

        TradeUnion tradeUnion = new TradeUnion();
        tradeUnion.setId(1L);

        beneficiary.getTradeUnions().add(tradeUnion);

        when(tradeUnionService.findById(tradeUnion.getId())).thenReturn(tradeUnion);
        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> beneficiaryService.associateTradeUnion(beneficiary.getId(), tradeUnion.getId()));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.alreadyContainsTradeUnion");
    }

    @Test
    public void testAssociateTradeUnionDoNotFailWhenBeneficiaryDoNotBelongToIt() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);
        beneficiary.setRelationshipType(HOLDER.getInstance());

        TradeUnion tradeUnion = new TradeUnion();
        tradeUnion.setId(1L);

        when(tradeUnionService.findById(tradeUnion.getId())).thenReturn(tradeUnion);
        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);

        BeneficiaryProjection result = beneficiaryService.associateTradeUnion(beneficiary.getId(), tradeUnion.getId());

        verify(beneficiaryRepository, times(1)).save(beneficiary);
        assertThat(result).isNotNull();
        assertThat(beneficiary.getTradeUnions().size()).isEqualTo(1);
    }

    @Test
    public void testDisassociateTradeUnionFailsWhenBeneficiaryDoesNotBelongsToIt() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);

        TradeUnion tradeUnion = new TradeUnion();
        tradeUnion.setId(1L);

        TradeUnion tradeUnion2 = new TradeUnion();
        tradeUnion2.setId(3L);

        TradeUnion tradeUnion3 = new TradeUnion();
        tradeUnion3.setId(4L);

        beneficiary.getTradeUnions().add(tradeUnion);
        beneficiary.getTradeUnions().add(tradeUnion2);

        when(tradeUnionService.findById(tradeUnion3.getId())).thenReturn(tradeUnion3);
        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryService.dissociateTradeUnion(beneficiary.getId(), tradeUnion3.getId()));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.doesNotContainsTradeUnion");
    }

    @Test
    public void testDisassociateTradeUnionDoNotFailWhenBeneficiaryBelongsToIt() throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);

        TradeUnion tradeUnion = new TradeUnion();
        tradeUnion.setId(1L);

        TradeUnion tradeUnion2 = new TradeUnion();
        tradeUnion2.setId(3L);

        beneficiary.getTradeUnions().add(tradeUnion);
        beneficiary.getTradeUnions().add(tradeUnion2);

        when(tradeUnionService.findById(tradeUnion2.getId())).thenReturn(tradeUnion2);
        doReturn(beneficiary).when(beneficiaryService).findById(beneficiary.getId());
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);

        BeneficiaryProjection result = beneficiaryService.dissociateTradeUnion(beneficiary.getId(), tradeUnion2.getId());

        verify(beneficiaryRepository, times(1)).save(beneficiary);
        assertThat(result).isNotNull();
        assertThat(result.getTradeUnions().size()).isEqualTo(1);
    }

}
