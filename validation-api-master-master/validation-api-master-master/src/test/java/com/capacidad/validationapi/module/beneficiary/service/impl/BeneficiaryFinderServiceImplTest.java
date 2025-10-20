package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryVerificationDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.repository.BeneficiaryRepository;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryFinderServiceImplTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken authentication;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private PropertiesService propertiesService;

    @Mock
    private Properties properties;

    @Spy
    @InjectMocks
    private BeneficiaryFinderServiceImpl beneficiaryFinder;


    @Test
    public void testVerifyBeneficiaryFailsWhenSearchThrowEmptyResults() {
        IdType idType = new IdType();
        idType.setId(1L);

        when(beneficiaryRepository.findAllProjectedByIdNumberAndIdTypeIdAndBirthDateAndBeneficiaryCode(anyLong(), anyLong(), any(LocalDate.class), anyString()))
                .thenReturn(new ArrayList<>());

        BeneficiaryVerificationDTO verificationDTO = BeneficiaryVerificationDTO.builder()
                .idNumber(1)
                .idType(idType)
                .birthDate(LocalDate.now())
                .beneficiaryCode("code")
                .build();

        Exception exception = (Exception) catchThrowable(() -> beneficiaryFinder.verifyBeneficiary(verificationDTO));

        assertThat(exception).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void testVerifyBeneficiaryFailsWhenBeneficiaryIsUnderAge() {
        BeneficiaryProjection.Verification projection = mock(BeneficiaryProjection.Verification.class);
        List<BeneficiaryProjection.Verification> projectionList = new ArrayList<>();
        projectionList.add(projection);

        IdType idType = new IdType();
        idType.setId(1L);

        when(beneficiaryRepository.findAllProjectedByIdNumberAndIdTypeIdAndBirthDateAndBeneficiaryCode(anyLong(), anyLong(), any(LocalDate.class), anyString()))
                .thenReturn(projectionList);
        when(projection.getAge()).thenReturn(1);
        when(propertiesService.getProperties()).thenReturn(properties);
        when(properties.getBeneficiaryMinAccountAge()).thenReturn(16);

        BeneficiaryVerificationDTO verificationDTO = BeneficiaryVerificationDTO.builder()
                .idNumber(1)
                .idType(idType)
                .birthDate(LocalDate.now())
                .beneficiaryCode("code")
                .build();

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryFinder.verifyBeneficiary(verificationDTO));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.underAge");
    }

    @Test
    public void testVerifyBeneficiaryFailsWhenBeneficiaryStatusIsWithoutCoverage() {
        BeneficiaryProjection.Verification projection = mock(BeneficiaryProjection.Verification.class);
        List<BeneficiaryProjection.Verification> projectionList = new ArrayList<>();
        projectionList.add(projection);

        IdType idType = new IdType();
        idType.setId(1L);

        when(beneficiaryRepository.findAllProjectedByIdNumberAndIdTypeIdAndBirthDateAndBeneficiaryCode(anyLong(), anyLong(), any(LocalDate.class), anyString()))
                .thenReturn(projectionList);
        when(projection.getAge()).thenReturn(16);
        when(projection.hasActiveHealthCoverage()).thenReturn(false);
        when(propertiesService.getProperties()).thenReturn(properties);
        when(properties.getBeneficiaryMinAccountAge()).thenReturn(16);

        BeneficiaryVerificationDTO verificationDTO = BeneficiaryVerificationDTO.builder()
                .idNumber(1)
                .idType(idType)
                .birthDate(LocalDate.now())
                .beneficiaryCode("code")
                .build();

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> beneficiaryFinder.verifyBeneficiary(verificationDTO));

        assertThat(exception.getMessage()).isEqualTo("beneficiary.noCoverage");
    }

    @Test
    public void testVerifyBeneficiaryDoNotFailsWhenBeneficiaryStatusIsWithCoverage() throws ObjectNotValidException, ObjectNotFoundException {
        BeneficiaryProjection.Verification projection = mock(BeneficiaryProjection.Verification.class);
        List<BeneficiaryProjection.Verification> projectionList = new ArrayList<>();
        projectionList.add(projection);

        IdType idType = new IdType();
        idType.setId(1L);

        when(beneficiaryRepository.findAllProjectedByIdNumberAndIdTypeIdAndBirthDateAndBeneficiaryCode(anyLong(), anyLong(), any(LocalDate.class), anyString()))
                .thenReturn(projectionList);
        when(projection.getAge()).thenReturn(16);
        when(projection.hasActiveHealthCoverage()).thenReturn(true);
        when(propertiesService.getProperties()).thenReturn(properties);
        when(properties.getBeneficiaryMinAccountAge()).thenReturn(16);

        BeneficiaryVerificationDTO verificationDTO = BeneficiaryVerificationDTO.builder()
                .idNumber(1)
                .idType(idType)
                .birthDate(LocalDate.now())
                .beneficiaryCode("code")
                .build();

        BeneficiaryProjection.Verification result = beneficiaryFinder.verifyBeneficiary(verificationDTO);

        assertThat(result).isEqualTo(projection);
    }

    @Test
    public void testCorrespondsToAuthenticationIsFalseWhenBeneficiaryDoesNotExists() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getResourceId()).thenReturn(beneficiary.getResourceId());

        when(beneficiaryRepository.existsByIdAndResourceId(beneficiary.getId(), beneficiary.getResourceId())).thenReturn(false);
        doThrow(new ObjectNotFoundException("")).when(beneficiaryFinder).findById(anyLong());

        boolean exists = beneficiaryFinder.correspondsToAuthentication(1L);

        assertThat(exists).isFalse();
    }

    @Test
    public void testCorrespondsToAuthenticationIsFalseWhenBeneficiaryDoesNotBelongsToResourceIdNorFamilyId() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getResourceId()).thenReturn(beneficiary.getResourceId());

        when(beneficiaryRepository.existsByIdAndResourceId(beneficiary.getId(), beneficiary.getResourceId())).thenReturn(false);
        doReturn(beneficiary).when(beneficiaryFinder).findById(1L);
        when(beneficiaryRepository.existsByResourceIdAndFamilyId(beneficiary.getResourceId(), beneficiary.getFamilyId())).thenReturn(false);

        boolean exists = beneficiaryFinder.correspondsToAuthentication(1L);

        assertThat(exists).isFalse();
    }

    @Test
    public void testCorrespondsToAuthenticationIsTrueWhenBeneficiaryBelongsToResourceId() {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getResourceId()).thenReturn(beneficiary.getResourceId());

        when(beneficiaryRepository.existsByIdAndResourceId(beneficiary.getId(), beneficiary.getResourceId())).thenReturn(true);

        boolean exists = beneficiaryFinder.correspondsToAuthentication(1L);

        assertThat(exists).isTrue();
    }

    @Test
    public void testCorrespondsToAuthenticationIsFalseWhenBeneficiaryDoesNotBelongsToResourceIdButHasSameFamilyId() throws ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getResourceId()).thenReturn(beneficiary.getResourceId());

        when(beneficiaryRepository.existsByIdAndResourceId(beneficiary.getId(), beneficiary.getResourceId())).thenReturn(false);
        doReturn(beneficiary).when(beneficiaryFinder).findById(1L);
        when(beneficiaryRepository.existsByResourceIdAndFamilyId(beneficiary.getResourceId(), beneficiary.getFamilyId())).thenReturn(true);

        boolean exists = beneficiaryFinder.correspondsToAuthentication(1L);

        assertThat(exists).isTrue();
    }

}
