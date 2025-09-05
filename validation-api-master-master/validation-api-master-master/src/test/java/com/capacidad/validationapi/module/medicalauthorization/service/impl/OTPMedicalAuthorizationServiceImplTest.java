package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.dto.OTPMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;
import com.capacidad.validationapi.module.medicalauthorization.model.OTPMedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalauthorization.repository.OTPMedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.totp.Authenticator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hashids.Hashids;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_MEDICAL_CENTER_INSTANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OTPMedicalAuthorizationServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Authenticator authenticator;

    @Mock
    private Hashids hashids;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Mock
    private SecurityContext securityContext;

    private SecurityContext defaultContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private MedicalCenterService medicalCenterService;

    @Mock
    private OTPMedicalAuthorizationRepository otpMedicalAuthorizationRepository;

    @Spy
    @InjectMocks
    private OTPMedicalAuthorizationServiceImpl otpMedicalAuthorizationService;

    @Before
    public void init() throws ObjectNotFoundException {
        defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE));
        when(otpMedicalAuthorizationService.getMedicalCenterService()).thenReturn(medicalCenterService);
        when(otpMedicalAuthorizationService.getPractitionerService()).thenReturn(practitionerService);
        MedicalCenter medicalCenter = new MedicalCenter();
        Address address = new Address();
        address.setCity(new City());
        medicalCenter.setAddress(address);
        when(medicalCenterService.getAuthMedicalCenter()).thenReturn(medicalCenter);
        when(practitionerService.findByIdLocked(anyLong())).thenReturn(new Practitioner());
    }

    @After
    public void destroy() {
        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testInitializeFailsWhenOtpIsInvalid() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        OTPMedicalAuthorizationDTO otpMedicalAuthorizationDTO = new OTPMedicalAuthorizationDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setIdNumber(11111111L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        OTPMedicalAuthorization otpMedicalAuthorization = new OTPMedicalAuthorization();
        otpMedicalAuthorization.setOtp("123456");
        otpMedicalAuthorization.setBeneficiary(beneficiary);
        otpMedicalAuthorization.setPractitioner(practitioner);

        when(otpMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(otpMedicalAuthorizationDTO, OTPMedicalAuthorization.class)).thenReturn(otpMedicalAuthorization);
        when(beneficiaryFinder.findByIdLocked(beneficiary.getId())).thenReturn(beneficiary).thenReturn(beneficiary);
        when(hashids.encode(beneficiary.getIdNumber(), beneficiary.getIdNumber())).thenReturn("EncodedValue");
        when(authenticator.authorize(anyString(), anyInt(), anyLong())).thenReturn(false);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> otpMedicalAuthorizationService.initialize(otpMedicalAuthorizationDTO));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.invalidOTP");
        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testInitializeFailsWhenOtpIsValidButAlreadyUsedByBeneficiaryInLessThanADay() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        OTPMedicalAuthorizationDTO otpMedicalAuthorizationDTO = new OTPMedicalAuthorizationDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setIdNumber(11111111L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        OTPMedicalAuthorization otpMedicalAuthorization = new OTPMedicalAuthorization();
        otpMedicalAuthorization.setOtp("123456");
        otpMedicalAuthorization.setBeneficiary(beneficiary);
        otpMedicalAuthorization.setPractitioner(practitioner);
        otpMedicalAuthorization.setCreatedAt(LocalDateTime.now());

        when(otpMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(otpMedicalAuthorizationDTO, OTPMedicalAuthorization.class)).thenReturn(otpMedicalAuthorization);
        when(beneficiaryFinder.findByIdLocked(beneficiary.getId())).thenReturn(beneficiary).thenReturn(beneficiary);
        when(hashids.encode(beneficiary.getIdNumber(), beneficiary.getIdNumber())).thenReturn("EncodedValue");
        when(authenticator.authorize(anyString(), anyInt(), anyLong())).thenReturn(true);
        when(otpMedicalAuthorizationRepository.findByOtpAndBeneficiaryId(otpMedicalAuthorization.getOtp(), beneficiary.getId()))
                .thenReturn(Optional.of(otpMedicalAuthorization));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> otpMedicalAuthorizationService.initialize(otpMedicalAuthorizationDTO));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.otpAlreadyUsed");
        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testInitializeReturnsValidAuthorizationTypeWhenOtpCodeUsedByBeneficiaryInDifferentDate() throws ObjectNotFoundException, ObjectNotValidException {
        OTPMedicalAuthorizationDTO otpMedicalAuthorizationDTO = new OTPMedicalAuthorizationDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setIdNumber(11111111L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        OTPMedicalAuthorization otpMedicalAuthorization = new OTPMedicalAuthorization();
        otpMedicalAuthorization.setOtp("123456");
        otpMedicalAuthorization.setBeneficiary(beneficiary);
        otpMedicalAuthorization.setPractitioner(practitioner);
        otpMedicalAuthorization.setCreatedAt(LocalDateTime.now().minusDays(2));

        AuthorizationType otpAuthorizationType = new AuthorizationType();
        otpAuthorizationType.setId(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_CODE.getId());

        when(otpMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(otpMedicalAuthorizationDTO, OTPMedicalAuthorization.class)).thenReturn(otpMedicalAuthorization);
        when(beneficiaryFinder.findByIdLocked(beneficiary.getId())).thenReturn(beneficiary).thenReturn(beneficiary);
        when(hashids.encode(beneficiary.getIdNumber(), beneficiary.getIdNumber())).thenReturn("EncodedValue");
        when(authenticator.authorize(anyString(), anyInt(), anyLong())).thenReturn(true);
        when(otpMedicalAuthorizationRepository.findByOtpAndBeneficiaryId(otpMedicalAuthorization.getOtp(), beneficiary.getId()))
                .thenReturn(Optional.of(otpMedicalAuthorization));
        doNothing().when(otpMedicalAuthorizationService).validate(otpMedicalAuthorization);

        OTPMedicalAuthorization result = otpMedicalAuthorizationService.initialize(otpMedicalAuthorizationDTO);

        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getBeneficiary().getId()).isEqualTo(beneficiary.getId());
        assertThat(result.getAuthorizationType().getId()).isEqualTo(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_CODE.getId());
    }

    @Test
    public void testInitializeReturnsValidAuthorizationTypeWhenOtpCodeNotUsed() throws ObjectNotFoundException, ObjectNotValidException {
        OTPMedicalAuthorizationDTO otpMedicalAuthorizationDTO = new OTPMedicalAuthorizationDTO();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setIdNumber(11111111L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        OTPMedicalAuthorization otpMedicalAuthorization = new OTPMedicalAuthorization();
        otpMedicalAuthorization.setOtp("123456");
        otpMedicalAuthorization.setBeneficiary(beneficiary);
        otpMedicalAuthorization.setPractitioner(practitioner);

        AuthorizationType otpAuthorizationType = new AuthorizationType();
        otpAuthorizationType.setId(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_CODE.getId());

        when(otpMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(otpMedicalAuthorizationDTO, OTPMedicalAuthorization.class)).thenReturn(otpMedicalAuthorization);
        when(beneficiaryFinder.findByIdLocked(beneficiary.getId())).thenReturn(beneficiary).thenReturn(beneficiary);
        when(hashids.encode(beneficiary.getIdNumber(), beneficiary.getIdNumber())).thenReturn("EncodedValue");
        when(authenticator.authorize(anyString(), anyInt(), anyLong())).thenReturn(true);
        when(otpMedicalAuthorizationRepository.findByOtpAndBeneficiaryId(otpMedicalAuthorization.getOtp(), beneficiary.getId()))
                .thenReturn(Optional.empty());
        doNothing().when(otpMedicalAuthorizationService).validate(otpMedicalAuthorization);

        OTPMedicalAuthorization result = otpMedicalAuthorizationService.initialize(otpMedicalAuthorizationDTO);

        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getBeneficiary().getId()).isEqualTo(beneficiary.getId());
        assertThat(result.getAuthorizationType().getId()).isEqualTo(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_CODE.getId());
    }

}
