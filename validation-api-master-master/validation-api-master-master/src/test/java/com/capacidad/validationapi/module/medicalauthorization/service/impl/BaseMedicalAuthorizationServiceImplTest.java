package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.dto.IdMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.model.QRMedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationValidator;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
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
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_PRACTITIONER_INSTANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BaseMedicalAuthorizationServiceImplTest {

    @Mock
    private MedicalAuthorizationValidator medicalAuthorizationValidator;

    @Mock
    private MedicalCenterService medicalCenterService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Spy
    @InjectMocks
    private IdMedicalAuthorizationServiceImpl medicalAuthorizationService;

    @Before
    public void init() {
        when(medicalAuthorizationService.getMedicalCenterService()).thenReturn(medicalCenterService);
        when(medicalAuthorizationService.getPractitionerService()).thenReturn(practitionerService);
        when(medicalAuthorizationService.getMedicalAuthorizationValidator()).thenReturn(medicalAuthorizationValidator);
    }

    @After
    public void end() {
        SecurityContextHolder.clearContext();
    }


    @Test
    public void testValidateExecutesSuccessfully() throws ObjectNotFoundException, ObjectNotValidException {
        QRMedicalAuthorization medicalAuthorization = new QRMedicalAuthorization();

        Nomenclator nomenclator = new Nomenclator();
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setName("medicalPracticeTest");
        nomenclator.setId(1L);
        nomenclator.setMedicalPractice(medicalPractice);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        City city = new City();
        city.setId(10L);
        Address address = new Address();
        address.setCity(city);
        medicalCenter.setAddress(address);

        medicalAuthorization.setMedicalCenter(medicalCenter);
        medicalAuthorization.setBeneficiary(new Beneficiary());
        medicalAuthorization.setPractitioner(new Practitioner());
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        when(medicalAuthorizationValidator.getValidatedNomenclator(medicalAuthorization.getPractitioner(), nomenclator.getId())).thenReturn(nomenclator);
        doNothing().when(medicalAuthorizationValidator).validatePractitionerMedicalCenter(medicalAuthorization.getPractitioner(), medicalCenter);
        doNothing().when(medicalAuthorizationValidator).validatePractitionerStatus(medicalAuthorization.getPractitioner());
        doNothing().when(medicalAuthorizationValidator).validateBeneficiaryStatus(medicalAuthorization.getBeneficiary());

        medicalAuthorizationService.validate(medicalAuthorization);

        verify(medicalAuthorizationValidator, times(1)).getValidatedNomenclator(medicalAuthorization.getPractitioner(), nomenclator.getId());
        verify(medicalAuthorizationValidator, times(1)).validatePractitionerMedicalCenter(medicalAuthorization.getPractitioner(), medicalCenter);
        verify(medicalAuthorizationValidator, times(1)).validatePractitionerStatus(medicalAuthorization.getPractitioner());
        verify(medicalAuthorizationValidator, times(1)).validateBeneficiaryStatus(medicalAuthorization.getBeneficiary());
    }

    @Test
    public void testInitializeFailsWhenAuthPractitionerAndNullMedicalCenter() throws ObjectNotFoundException {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_PRACTITIONER_INSTANCE));

        IdMedicalAuthorizationDTO medicalAuthorizationDTO = new IdMedicalAuthorizationDTO();
        MedicalAuthorization medicalAuthorization = new QRMedicalAuthorization();
        Practitioner practitioner = new Practitioner();
        medicalAuthorization.setMedicalCenter(null);

        doReturn(objectMapper).when(medicalAuthorizationService).getObjectMapper();
        when(objectMapper.convertValue(medicalAuthorizationDTO, MedicalAuthorization.class)).thenReturn(medicalAuthorization);
        when(practitionerService.getAuthPractitionerLocked()).thenReturn(practitioner);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationService.initialize(medicalAuthorizationDTO));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.missingMedicalCenter");

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

    @Test
    public void testInitializeDoNotFailsWhenAuthPractitionerAndNotNullMedicalCenter() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_PRACTITIONER_INSTANCE));

        IdMedicalAuthorizationDTO medicalAuthorizationDTO = new IdMedicalAuthorizationDTO();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Practitioner practitioner = new Practitioner();
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        Address address = new Address();
        address.setCity(new City());
        medicalCenter.setAddress(address);
        medicalAuthorization.setMedicalCenter(medicalCenter);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setBeneficiary(beneficiary);

        when(beneficiaryFinder.findBeneficiaryLocked(anyLong(), any(IdType.class))).thenReturn(beneficiary);
        doNothing().when(medicalAuthorizationService).validate(medicalAuthorization);

        doReturn(objectMapper).when(medicalAuthorizationService).getObjectMapper();
        when(objectMapper.convertValue(medicalAuthorizationDTO, MedicalAuthorization.class)).thenReturn(medicalAuthorization);
        when(practitionerService.getAuthPractitionerLocked()).thenReturn(practitioner);
        when(medicalCenterService.findById(medicalCenter.getId())).thenReturn(medicalCenter);
        when(beneficiaryFinder.findBeneficiaryLocked(beneficiary.getIdNumber(), beneficiary.getIdType())).thenReturn(beneficiary);

        MedicalAuthorization result = medicalAuthorizationService.initialize(medicalAuthorizationDTO);

        assertThat(result.getMedicalCenter()).isEqualTo(medicalCenter);
        assertThat(result.getCity()).isEqualTo(medicalCenter.getAddress().getCity());

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

    @Test
    public void testInitializeDoNotFailsWhenValidAuthMedicalCenter() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_MEDICAL_CENTER_INSTANCE));

        IdMedicalAuthorizationDTO medicalAuthorizationDTO = new IdMedicalAuthorizationDTO();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        Address address = new Address();
        address.setCity(new City());
        medicalCenter.setAddress(address);
        medicalAuthorization.setMedicalCenter(medicalCenter);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setIdNumber(11111111L);
        IdType idType = new IdType();
        idType.setId(1L);
        beneficiary.setIdType(idType);

        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setBeneficiary(beneficiary);

        when(beneficiaryFinder.findBeneficiaryLocked(anyLong(), any(IdType.class))).thenReturn(beneficiary);
        doNothing().when(medicalAuthorizationService).validate(medicalAuthorization);

        doReturn(objectMapper).when(medicalAuthorizationService).getObjectMapper();
        when(objectMapper.convertValue(medicalAuthorizationDTO, MedicalAuthorization.class)).thenReturn(medicalAuthorization);
        when(practitionerService.findByIdLocked(practitioner.getId())).thenReturn(practitioner);
        when(medicalCenterService.getAuthMedicalCenter()).thenReturn(medicalCenter);
        when(beneficiaryFinder.findBeneficiaryLocked(beneficiary.getIdNumber(), beneficiary.getIdType())).thenReturn(beneficiary);

        MedicalAuthorization result = medicalAuthorizationService.initialize(medicalAuthorizationDTO);

        assertThat(result.getMedicalCenter()).isEqualTo(medicalCenter);
        assertThat(result.getCity()).isEqualTo(medicalCenter.getAddress().getCity());

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

}
