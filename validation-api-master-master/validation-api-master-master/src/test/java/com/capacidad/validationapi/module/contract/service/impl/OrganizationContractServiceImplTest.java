package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.contract.projection.OrganizationContractProjection;
import com.capacidad.validationapi.module.contract.repository.OrganizationContractRepository;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.specification.SpecificationBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationContractServiceImplTest {

    @Mock
    private SpecificationBuilder<OrganizationContract, Long> specificationBuilder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private OrganizationContractRepository organizationContractRepository;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Spy
    @InjectMocks
    private OrganizationContractServiceImpl organizationContractService;

    @Test
    public void testFindAllAuthOrganizationContractsWithoutSearchReturnsValidPage() {
        SecurityContextHolder.setContext(securityContext);

        doReturn(specificationBuilder).when(organizationContractService).getSpecificationBuilder();
        when(specificationBuilder.parseAndBuild("")).thenReturn(Optional.empty());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());
        when(organizationContractRepository.findAllProjectedBy(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<OrganizationContractProjection> pageResult = organizationContractService.findAllAuthOrganizationContracts("", PageRequest.of(1, 10));

        verify(organizationContractRepository, times(1)).findAllProjectedBy(any(), any(), any(Pageable.class));
        assertThat(pageResult).isNotNull();
    }

    @Test
    public void testFindAllAuthOrganizationContractsWithSearchReturnsValidPage() {
        SecurityContextHolder.setContext(securityContext);

        doReturn(specificationBuilder).when(organizationContractService).getSpecificationBuilder();

        Optional<Specification<OrganizationContract>> optSpecPractitionerContract = Optional.of(mock(Specification.class));

        when(specificationBuilder.parseAndBuild("search")).thenReturn(optSpecPractitionerContract);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());
        when(organizationContractRepository.findAllProjectedBy(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<OrganizationContractProjection> pageResult = organizationContractService.findAllAuthOrganizationContracts("search", PageRequest.of(1, 10));

        verify(organizationContractRepository, times(1)).findAllProjectedBy(any(), any(), any(Pageable.class));
        assertThat(pageResult).isNotNull();
    }

    @Test
    public void testValidateFailsWhenContractAlreadyExistForSpecifiedEntityAndDateRage() {
        Organization organization = new Organization();
        organization.setId(1L);

        OrganizationContract organizationContract = new OrganizationContract();

        organizationContract.setDateFrom(LocalDate.now());
        organizationContract.setDateTo(LocalDate.now().plusDays(1));
        organizationContract.setOrganization(organization);

        when(organizationContractRepository
                .existsByOrganizationIdAndPeriod
                        (organizationContract.getOrganization().getId(), organizationContract.getDateFrom(), organizationContract.getDateTo()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> organizationContractService.validate(organizationContract));

        assertThat(exception.getMessage()).contains("contract.organizationContractAlreadyExists");
    }

    @Test
    public void testValidateFailsWhenInvalidDates() {
        Organization organization = new Organization();
        organization.setId(1L);

        OrganizationContract organizationContract = new OrganizationContract();

        organizationContract.setDateFrom(LocalDate.now().plusDays(10));
        organizationContract.setDateTo(LocalDate.now());
        organizationContract.setOrganization(organization);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> organizationContractService.validateUpdate(organizationContract));

        assertThat(exception.getMessage()).contains("generic.dateFromDateTo");
    }

    @Test
    public void testValidateDoNotFailsWhenContractDoesNotExistForSpecifiedEntityAndDateRage() throws ObjectNotValidException {
        Organization organization = new Organization();
        organization.setId(1L);

        OrganizationContract organizationContract = new OrganizationContract();

        organizationContract.setDateFrom(LocalDate.now());
        organizationContract.setDateTo(LocalDate.now().plusDays(1));
        organizationContract.setOrganization(organization);

        when(organizationContractRepository
                .existsByOrganizationIdAndPeriod
                        (organizationContract.getOrganization().getId(), organizationContract.getDateFrom(), organizationContract.getDateTo()))
                .thenReturn(false);

        organizationContractService.validate(organizationContract);
    }

    @Test
    public void testValidateUpdateFailsWhenContractAlreadyExistForSpecifiedEntityAndDateRage() {
        Organization organization = new Organization();
        organization.setId(1L);

        OrganizationContract organizationContract = new OrganizationContract();

        organizationContract.setDateFrom(LocalDate.now());
        organizationContract.setDateTo(LocalDate.now().plusDays(1));
        organizationContract.setOrganization(organization);

        when(organizationContractRepository
                .existsByIdNotAndOrganizationIdAndPeriod
                        (organizationContract.getId(), organizationContract.getOrganization().getId(), organizationContract.getDateFrom(), organizationContract.getDateTo()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> organizationContractService.validateUpdate(organizationContract));

        assertThat(exception.getMessage()).contains("contract.organizationContractAlreadyExists");
    }

    @Test
    public void testValidateUpdateDoNotFailsWhenContractDoesNotExistForSpecifiedEntityAndDateRage() throws ObjectNotValidException {
        Organization organization = new Organization();
        organization.setId(1L);

        OrganizationContract organizationContract = new OrganizationContract();

        organizationContract.setDateFrom(LocalDate.now());
        organizationContract.setDateTo(LocalDate.now().plusDays(1));
        organizationContract.setOrganization(organization);

        when(organizationContractRepository
                .existsByIdNotAndOrganizationIdAndPeriod
                        (organizationContract.getId(), organizationContract.getOrganization().getId(), organizationContract.getDateFrom(), organizationContract.getDateTo()))
                .thenReturn(false);

        organizationContractService.validateUpdate(organizationContract);
    }

}
