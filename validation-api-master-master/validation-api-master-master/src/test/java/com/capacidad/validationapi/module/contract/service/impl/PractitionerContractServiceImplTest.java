package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import com.capacidad.validationapi.module.contract.projection.PractitionerContractProjection;
import com.capacidad.validationapi.module.contract.repository.PractitionerContractRepository;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
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
public class PractitionerContractServiceImplTest {

    @Mock
    private SpecificationBuilder<PractitionerContract, Long> specificationBuilder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private PractitionerContractRepository practitionerContractRepository;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;


    @Spy
    @InjectMocks
    private PractitionerContractServiceImpl practitionerContractService;

    @Test
    public void testFindAllAuthPractitionerContractsWithoutSearchReturnsValidPage() {
        SecurityContextHolder.setContext(securityContext);

        doReturn(specificationBuilder).when(practitionerContractService).getSpecificationBuilder();
        when(specificationBuilder.parseAndBuild("")).thenReturn(Optional.empty());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());
        when(practitionerContractRepository.findAllProjectedBy(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<PractitionerContractProjection> pageResult = practitionerContractService.findAllAuthPractitionerContracts("", PageRequest.of(1, 10));

        verify(practitionerContractRepository, times(1)).findAllProjectedBy(any(), any(), any(Pageable.class));
        assertThat(pageResult).isNotNull();
    }

    @Test
    public void testFindAllAuthPractitionerContractsWithSearchReturnsValidPage() {
        SecurityContextHolder.setContext(securityContext);

        doReturn(specificationBuilder).when(practitionerContractService).getSpecificationBuilder();

        Optional<Specification<PractitionerContract>> optSpecPractitionerContract = Optional.of(mock(Specification.class));

        when(specificationBuilder.parseAndBuild("search")).thenReturn(optSpecPractitionerContract);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());
        when(practitionerContractRepository.findAllProjectedBy(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<PractitionerContractProjection> pageResult = practitionerContractService.findAllAuthPractitionerContracts("search", PageRequest.of(1, 10));

        verify(practitionerContractRepository, times(1)).findAllProjectedBy(any(), any(), any(Pageable.class));
        assertThat(pageResult).isNotNull();
    }

    @Test
    public void testValidateFailsWhenContractAlreadyExistForSpecifiedEntityAndDateRage() {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        PractitionerContract practitionerContract = new PractitionerContract();

        practitionerContract.setDateFrom(LocalDate.now());
        practitionerContract.setDateTo(LocalDate.now().plusDays(1));
        practitionerContract.setPractitioner(practitioner);

        when(practitionerContractRepository
                .existsByPractitionerIdAndPeriod
                        (practitionerContract.getPractitioner().getId(), practitionerContract.getDateFrom(), practitionerContract.getDateTo()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> practitionerContractService.validate(practitionerContract));

        assertThat(exception.getMessage()).contains("contract.practitionerContractAlreadyExists");
    }

    @Test
    public void testValidateDoNotFailsWhenContractDoesNotExistForSpecifiedEntityAndDateRage() throws ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        PractitionerContract practitionerContract = new PractitionerContract();

        practitionerContract.setDateFrom(LocalDate.now());
        practitionerContract.setDateTo(LocalDate.now().plusDays(1));
        practitionerContract.setPractitioner(practitioner);

        when(practitionerContractRepository
                .existsByPractitionerIdAndPeriod
                        (practitionerContract.getPractitioner().getId(), practitionerContract.getDateFrom(), practitionerContract.getDateTo()))
                .thenReturn(false);

        practitionerContractService.validate(practitionerContract);
    }

    @Test
    public void testValidateUpdateFailsWhenContractAlreadyExistForSpecifiedEntityAndDateRage() {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        PractitionerContract practitionerContract = new PractitionerContract();

        practitionerContract.setDateFrom(LocalDate.now());
        practitionerContract.setDateTo(LocalDate.now().plusDays(1));
        practitionerContract.setPractitioner(practitioner);

        when(practitionerContractRepository
                .existsByIdNotAndPractitionerIdAndPeriod
                        (practitionerContract.getId(), practitionerContract.getPractitioner().getId(), practitionerContract.getDateFrom(), practitionerContract.getDateTo()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> practitionerContractService.validateUpdate(practitionerContract));

        assertThat(exception.getMessage()).contains("contract.practitionerContractAlreadyExists");
    }

    @Test
    public void testValidateUpdateDoNotFailsWhenContractDoesNotExistForSpecifiedEntityAndDateRage() throws ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        PractitionerContract practitionerContract = new PractitionerContract();

        practitionerContract.setDateFrom(LocalDate.now());
        practitionerContract.setDateTo(LocalDate.now().plusDays(1));
        practitionerContract.setPractitioner(practitioner);

        when(practitionerContractRepository
                .existsByIdNotAndPractitionerIdAndPeriod
                        (practitionerContract.getId(), practitionerContract.getPractitioner().getId(), practitionerContract.getDateFrom(), practitionerContract.getDateTo()))
                .thenReturn(false);

        practitionerContractService.validateUpdate(practitionerContract);
    }

}
