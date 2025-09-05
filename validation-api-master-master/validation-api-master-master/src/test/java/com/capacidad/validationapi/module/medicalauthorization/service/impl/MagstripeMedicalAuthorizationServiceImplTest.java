package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.dto.MagstripeMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Collections;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_MEDICAL_CENTER_INSTANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MagstripeMedicalAuthorizationServiceImplTest {

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
    private ObjectMapper objectMapper;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Spy
    @InjectMocks
    private MagstripeMedicalAuthorizationServiceImpl magstripeMedicalAuthorizationService;

    @Before
    public void init() throws ObjectNotFoundException {
        defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE));
        when(magstripeMedicalAuthorizationService.getMedicalCenterService()).thenReturn(medicalCenterService);
        when(magstripeMedicalAuthorizationService.getPractitionerService()).thenReturn(practitionerService);
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
    public void testInitializeReturnsValidAuthorizationType() throws ObjectNotFoundException, ObjectNotValidException {
        MagstripeMedicalAuthorizationDTO magstripeMedicalAuthorizationDTO = new MagstripeMedicalAuthorizationDTO();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode("123abc");

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        AuthorizationType magstripeAuthorizationType = new AuthorizationType();
        magstripeAuthorizationType.setId(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_MAGSTRIPE.getId());

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPractitioner(practitioner);

        when(magstripeMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(magstripeMedicalAuthorizationDTO, MedicalAuthorization.class)).thenReturn(medicalAuthorization);
        when(beneficiaryFinder.findBeneficiaryLocked(beneficiary.getBeneficiaryCode())).thenReturn(beneficiary);
        doNothing().when(magstripeMedicalAuthorizationService).validate(medicalAuthorization);

        MedicalAuthorization result = magstripeMedicalAuthorizationService.initialize(magstripeMedicalAuthorizationDTO);

        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getBeneficiary().getId()).isEqualTo(beneficiary.getId());
        assertThat(result.getAuthorizationType().getId()).isEqualTo(magstripeAuthorizationType.getId());
    }
}
