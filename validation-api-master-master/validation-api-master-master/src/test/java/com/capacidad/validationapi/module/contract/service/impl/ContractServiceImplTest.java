package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.projection.ContractProjection;
import com.capacidad.validationapi.module.contract.repository.ContractRepository;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.specification.SpecificationBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class ContractServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private SpecificationBuilder<Contract, Long> specificationBuilder;

    @Mock
    private ContractMediator contractMediator;

    @Spy
    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    public void testBuildContractPageIsValidWhenRoleIsPractitioner() {
        SecurityContextHolder.setContext(securityContext);

        Pageable pageable = PageRequest.of(1, 10);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_PRACTITIONER_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(contractMediator.findAllAuthPractitionerContracts(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        contractService.findAllContracts(pageable, "");

        verify(contractMediator, times(1)).findAllAuthPractitionerContracts(anyString(), any(Pageable.class));
    }

    @Test
    public void testBuildContractPageIsValidWhenRoleIsMedicalCenter() {
        SecurityContextHolder.setContext(securityContext);

        Pageable pageable = PageRequest.of(1, 10);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_MEDICAL_CENTER_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(contractMediator.findAllAuthMedicalCenterContracts(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        contractService.findAllContracts(pageable, "");

        verify(contractMediator, times(1)).findAllAuthMedicalCenterContracts(anyString(), any(Pageable.class));
    }

    @Test
    public void testBuildContractPageIsValidWhenRoleIsOrganization() {
        SecurityContextHolder.setContext(securityContext);

        Pageable pageable = PageRequest.of(1, 10);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_ORGANIZATION_INSTANCE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(contractMediator.findAllAuthOrganizationContracts(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);

        contractService.findAllContracts(pageable, "");

        verify(contractMediator, times(1)).findAllAuthOrganizationContracts(anyString(), any(Pageable.class));
    }

    @Test
    public void testBuildContractPageIsValidWhenRoleIsAdminAndEmptySearch() {
        SecurityContextHolder.setContext(securityContext);

        Pageable pageable = PageRequest.of(1, 10);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_ADMIN_INSTANCE);

        List<Contract> contractPageList = new ArrayList<>();
        contractPageList.add(new Contract());

        doReturn(specificationBuilder).when(contractService).getSpecificationBuilder();
        when(specificationBuilder.parseAndBuild("")).thenReturn(Optional.empty());
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);
        when(contractRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(contractPageList));

        Page<ContractProjection> pageResult = contractService.findAllContracts(pageable, "");

        verify(contractRepository, times(1)).findAll(any(Pageable.class));
        assertThat(pageResult.getContent().size()).isEqualTo(contractPageList.size());
    }

    @Test
    public void testBuildContractPageIsValidWhenRoleIsAdminAndNonEmptySearch() {
        SecurityContextHolder.setContext(securityContext);

        Specification spec = mock(Specification.class);

        Pageable pageable = PageRequest.of(1, 10);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(ROLE_ADMIN_INSTANCE);

        List<Contract> contractPageList = new ArrayList<>();
        contractPageList.add(new Contract());

        doReturn(specificationBuilder).when(contractService).getSpecificationBuilder();
        when(specificationBuilder.parseAndBuild("search")).thenReturn(Optional.of(spec));
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(grantedAuthorities);
        when(contractRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(contractPageList));

        Page<ContractProjection> pageResult = contractService.findAllContracts(pageable, "search");

        verify(contractRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        assertThat(pageResult.getContent().size()).isEqualTo(contractPageList.size());
    }

    @Test
    public void testDeleteExecuteSuccessfully() throws ObjectNotFoundException {
        Contract contract = new Contract();
        contract.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.getContracts().add(contract);
        contract.getPractitioners().add(practitioner);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contract.getContractAdjustments().add(contractAdjustment);

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        contract.getRuleConfigurations().add(ruleConfiguration);

        ContractItem contractItem = new ContractItem();
        contractItem.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());
        ContractItem contractItem1 = new ContractItem();
        ContractItem contractItem2 = new ContractItem();
        contractItem2.setDeleted(true);

        contract.getContractItems().add(contractItem);
        contract.getContractItems().add(contractItem1);
        contract.getContractItems().add(contractItem2);

        doReturn(contract).when(contractService).findById(contract.getId());

        doReturn(objectMapper).when(contractService).getObjectMapper();
        doReturn(contractRepository).when(contractService).getRepository();
        doReturn(applicationEventPublisher).when(contractService).getApplicationEventPublisher();

        JsonNode result = contractService.delete(contract.getId());

        assertThat(result.get("id").asLong()).isEqualTo(contract.getId());
        assertThat(contract.getDeleted()).isTrue();
        assertThat(contract.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(practitioner.getContracts()).isEmpty();
        assertThat(contract.getContractAdjustments()).isEmpty();
        assertThat(contract.getRuleConfigurations()).isEmpty();
        assertThat(contract.getContractItems()).hasSize(1);
        ContractItem contractItemRes = contract.getContractItems().iterator().next();
        assertThat(contractItemRes.getDeleted()).isTrue();
        assertThat(contractItemRes.getDeletionToken()).isEqualTo(contract.getDeletionToken());
        verify(contractRepository, Mockito.times(1)).save(contract);
        verify(applicationEventPublisher, Mockito.times(1)).publishEvent(any(AfterSoftDeleteEvent.class));

    }

}
