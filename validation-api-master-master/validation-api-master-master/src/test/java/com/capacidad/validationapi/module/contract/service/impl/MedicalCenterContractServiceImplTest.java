package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.contract.model.MedicalCenterContract;
import com.capacidad.validationapi.module.contract.projection.MedicalCenterContractProjection;
import com.capacidad.validationapi.module.contract.repository.MedicalCenterContractRepository;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
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
public class MedicalCenterContractServiceImplTest {

    @Mock
    private SpecificationBuilder<MedicalCenterContract, Long> specificationBuilder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private MedicalCenterContractRepository medicalCenterContractRepository;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Spy
    @InjectMocks
    private MedicalCenterContractServiceImpl medicalCenterContractService;

    @Test
    public void testFindAllAuthMedicalCenterContractsWithoutSearchReturnsValidPage() {
        SecurityContextHolder.setContext(securityContext);

        doReturn(specificationBuilder).when(medicalCenterContractService).getSpecificationBuilder();
        when(specificationBuilder.parseAndBuild("")).thenReturn(Optional.empty());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());
        when(medicalCenterContractRepository.findAllProjectedBy(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<MedicalCenterContractProjection> pageResult = medicalCenterContractService.findAllAuthMedicalCenterContracts("", PageRequest.of(1, 10));

        verify(medicalCenterContractRepository, times(1)).findAllProjectedBy(any(), any(), any(Pageable.class));
        assertThat(pageResult).isNotNull();
    }

    @Test
    public void testFindAllAuthMedicalCenterContractsWithSearchReturnsValidPage() {
        SecurityContextHolder.setContext(securityContext);

        doReturn(specificationBuilder).when(medicalCenterContractService).getSpecificationBuilder();

        Optional<Specification<MedicalCenterContract>> optSpecPractitionerContract = Optional.of(mock(Specification.class));

        when(specificationBuilder.parseAndBuild("search")).thenReturn(optSpecPractitionerContract);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getResourceId()).thenReturn(UUID.randomUUID());
        when(medicalCenterContractRepository.findAllProjectedBy(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<MedicalCenterContractProjection> pageResult = medicalCenterContractService.findAllAuthMedicalCenterContracts("search", PageRequest.of(1, 10));

        verify(medicalCenterContractRepository, times(1)).findAllProjectedBy(any(), any(), any(Pageable.class));
        assertThat(pageResult).isNotNull();
    }

    @Test
    public void testValidateFailsWhenContractAlreadyExistForSpecifiedEntityAndDateRage() {
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();

        medicalCenterContract.setDateFrom(LocalDate.now());
        medicalCenterContract.setDateTo(LocalDate.now().plusDays(1));
        medicalCenterContract.setMedicalCenter(medicalCenter);

        when(medicalCenterContractRepository
                .existsByMedicalCenterIdAndPeriod
                        (medicalCenterContract.getMedicalCenter().getId(), medicalCenterContract.getDateFrom(), medicalCenterContract.getDateTo()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> medicalCenterContractService.validate(medicalCenterContract));

        assertThat(exception.getMessage()).contains("contract.medicalCenterContractAlreadyExists");
    }

    @Test
    public void testValidateDoNotFailsWhenContractDoesNotExistForSpecifiedEntityAndDateRage() throws ObjectNotValidException {
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();

        medicalCenterContract.setDateFrom(LocalDate.now());
        medicalCenterContract.setDateTo(LocalDate.now().plusDays(1));
        medicalCenterContract.setMedicalCenter(medicalCenter);

        when(medicalCenterContractRepository
                .existsByMedicalCenterIdAndPeriod
                        (medicalCenterContract.getMedicalCenter().getId(), medicalCenterContract.getDateFrom(), medicalCenterContract.getDateTo()))
                .thenReturn(false);

        medicalCenterContractService.validate(medicalCenterContract);
    }

    @Test
    public void testValidateUpdateFailsWhenContractAlreadyExistForSpecifiedEntityAndDateRage() {
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();

        medicalCenterContract.setDateFrom(LocalDate.now());
        medicalCenterContract.setDateTo(LocalDate.now().plusDays(1));
        medicalCenterContract.setMedicalCenter(medicalCenter);

        when(medicalCenterContractRepository
                .existsByIdNotAndMedicalCenterIdAndPeriod
                        (medicalCenterContract.getId(), medicalCenterContract.getMedicalCenter().getId(), medicalCenterContract.getDateFrom(), medicalCenterContract.getDateTo()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> medicalCenterContractService.validateUpdate(medicalCenterContract));

        assertThat(exception.getMessage()).contains("contract.medicalCenterContractAlreadyExists");
    }

    @Test
    public void testValidateUpdateDoNotFailsWhenContractDoesNotExistForSpecifiedEntityAndDateRage() throws ObjectNotValidException {
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();

        medicalCenterContract.setDateFrom(LocalDate.now());
        medicalCenterContract.setDateTo(LocalDate.now().plusDays(1));
        medicalCenterContract.setMedicalCenter(medicalCenter);

        when(medicalCenterContractRepository
                .existsByIdNotAndMedicalCenterIdAndPeriod
                        (medicalCenterContract.getId(), medicalCenterContract.getMedicalCenter().getId(), medicalCenterContract.getDateFrom(), medicalCenterContract.getDateTo()))
                .thenReturn(false);

        medicalCenterContractService.validateUpdate(medicalCenterContract);
    }


}
