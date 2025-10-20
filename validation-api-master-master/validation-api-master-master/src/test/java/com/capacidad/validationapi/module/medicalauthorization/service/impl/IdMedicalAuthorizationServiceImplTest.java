package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.dto.IdMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.person.model.IdType;
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
public class IdMedicalAuthorizationServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

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

    @Spy
    @InjectMocks
    private IdMedicalAuthorizationServiceImpl idMedicalAuthorizationService;

    @Before
    public void init() throws ObjectNotFoundException {
        defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_MEDICAL_CENTER_INSTANCE));
        when(idMedicalAuthorizationService.getMedicalCenterService()).thenReturn(medicalCenterService);
        when(idMedicalAuthorizationService.getPractitionerService()).thenReturn(practitionerService);
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
        IdMedicalAuthorizationDTO idMedicalAuthorizationDTO = new IdMedicalAuthorizationDTO();

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(1111111L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        IdType idType = new IdType();
        idType.setId(1L);

        beneficiary.setIdType(idType);

        AuthorizationType idAuthorizationType = new AuthorizationType();
        idAuthorizationType.setId(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_ID_NUMBER.getId());

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPractitioner(practitioner);

        when(idMedicalAuthorizationService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(idMedicalAuthorizationDTO, MedicalAuthorization.class)).thenReturn(medicalAuthorization);
        when(beneficiaryFinder.findBeneficiaryLocked(beneficiary.getIdNumber(), beneficiary.getIdType())).thenReturn(beneficiary);
        doNothing().when(idMedicalAuthorizationService).validate(medicalAuthorization);

        MedicalAuthorization result = idMedicalAuthorizationService.initialize(idMedicalAuthorizationDTO);

        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getBeneficiary().getId()).isEqualTo(beneficiary.getId());
        assertThat(result.getAuthorizationType().getId()).isEqualTo(idAuthorizationType.getId());
    }

}
