package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryService;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationValidator;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.service.PrescriptionRestrictionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_MEDICAL_CENTER_INSTANCE;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_PRACTITIONER_INSTANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrescriptionValidatorImplTest {

    @Mock
    private MedicalCenterService medicalCenterService;

    @Mock
    private MedicalAuthorizationValidator medicalAuthorizationValidator;

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private BeneficiaryService beneficiaryService;

    @Mock
    private PrescriptionRestrictionService prescriptionRestrictionService;

    @InjectMocks
    private PrescriptionValidatorImpl prescriptionValidator;


    @Test
    public void testValidateFailsWhenMoreThanAllowedItems() {
        Prescription prescription = new Prescription();
        prescription.getPrescriptionItems().add(new PrescriptionItem());
        prescription.getPrescriptionItems().add(new PrescriptionItem());
        prescription.getPrescriptionItems().add(new PrescriptionItem());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> prescriptionValidator.validate(prescription));

        assertThat(exception.getMessage()).isEqualTo("prescription.maxPrescriptionItems");
    }

    @Test
    public void testValidateFailsWhenInvalidTransactionKey() throws ObjectNotFoundException {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_MEDICAL_CENTER_INSTANCE));

        Prescription prescription = new Prescription();

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(3L);

        String expectedKey = "expectedKey";

        Practitioner practitioner = new Practitioner();
        practitioner.setTransactionKey("invalidKey");
        practitioner.setId(3L);

        prescription.setTransactionKey(expectedKey);
        prescription.setPractitioner(practitioner);
        prescription.getPrescriptionItems().add(new PrescriptionItem());
        prescription.getPrescriptionItems().add(new PrescriptionItem());

        when(medicalCenterService.getAuthMedicalCenter()).thenReturn(medicalCenter);
        when(practitionerService.findById((practitioner.getId()))).thenReturn(practitioner);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> prescriptionValidator.validate(prescription));

        assertThat(exception.getMessage()).isEqualTo("prescription.invalidTransactionKey");

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

    @Test
    public void testValidateIsSuccessfulWhenAuthMedicalCenter() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_MEDICAL_CENTER_INSTANCE));

        Prescription prescription = new Prescription();

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(3L);
        City city = new City();
        city.setId(5L);
        Address address = new Address();
        address.setCity(city);
        medicalCenter.setAddress(address);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);

        String expectedKey = "expectedKey";

        Practitioner practitioner = new Practitioner();
        practitioner.setTransactionKey(expectedKey);
        practitioner.setId(3L);

        Set<MedicalSpecialty> medicalSpecialties = new HashSet<>();
        medicalSpecialties.add(new MedicalSpecialty());

        practitioner.setMedicalSpecialties(medicalSpecialties);

        prescription.setTransactionKey(expectedKey);
        prescription.setMedicalCenter(medicalCenter);
        prescription.setPractitioner(practitioner);
        prescription.setBeneficiary(beneficiary);
        prescription.getPrescriptionItems().add(new PrescriptionItem());
        prescription.getPrescriptionItems().add(new PrescriptionItem());

        when(medicalCenterService.getAuthMedicalCenter()).thenReturn(medicalCenter);
        when(beneficiaryService.findById(beneficiary.getId())).thenReturn(beneficiary);
        when(practitionerService.findById((practitioner.getId()))).thenReturn(practitioner);

        prescriptionValidator.validate(prescription);

        verify(medicalAuthorizationValidator, times(1)).validatePractitionerStatus(practitioner);
        verify(medicalAuthorizationValidator, times(1)).validateBeneficiaryStatus(beneficiary);
        verify(medicalAuthorizationValidator, times(1)).validatePractitionerMedicalCenter(practitioner, medicalCenter);
        verify(prescriptionRestrictionService, times(1)).validateSpecialties(medicalSpecialties);

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

    @Test
    public void testValidateIsSuccessfulWhenAuthPractitioner() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_PRACTITIONER_INSTANCE));

        Prescription prescription = new Prescription();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(2L);

        String expectedKey = "expectedKey";

        Practitioner practitioner = new Practitioner();
        practitioner.setId(3L);
        practitioner.setTransactionKey(expectedKey);

        Set<MedicalSpecialty> medicalSpecialties = new HashSet<>();
        medicalSpecialties.add(new MedicalSpecialty());

        practitioner.setMedicalSpecialties(medicalSpecialties);

        prescription.setTransactionKey(expectedKey);
        prescription.setPractitioner(practitioner);
        prescription.setBeneficiary(beneficiary);
        prescription.getPrescriptionItems().add(new PrescriptionItem());
        prescription.getPrescriptionItems().add(new PrescriptionItem());

        when(practitionerService.getAuthPractitioner()).thenReturn(practitioner);
        when(beneficiaryService.findById(beneficiary.getId())).thenReturn(beneficiary);

        prescriptionValidator.validate(prescription);

        verify(medicalAuthorizationValidator, times(1)).validatePractitionerStatus(practitioner);
        verify(medicalAuthorizationValidator, times(1)).validateBeneficiaryStatus(beneficiary);
        verify(medicalAuthorizationValidator, never()).validatePractitionerMedicalCenter(any(), any());
        verify(prescriptionRestrictionService, times(1)).validateSpecialties(medicalSpecialties);

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

    @Test
    public void testValidateFromMedicalAuthorizationThrowsExceptionWhenAuthMedicalCenterAndNullTransactionKey() {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_MEDICAL_CENTER_INSTANCE));

        Prescription prescription = new Prescription();
        prescription.setTransactionKey(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> prescriptionValidator.validateFromMedicalAuthorization(prescription));

        assertThat(exception.getMessage()).isEqualTo("prescription.invalidTransactionKey");

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

    @Test
    public void testValidateFromMedicalAuthorizationDoNotVerifyTransactionKeyWhenAuthPractitioner() throws ObjectNotValidException {
        SecurityContext defaultSecurityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singleton(ROLE_PRACTITIONER_INSTANCE));

        Prescription prescription = new Prescription();
        prescription.setTransactionKey(null);
        prescription.setPractitioner(new Practitioner());

        prescriptionValidator.validateFromMedicalAuthorization(prescription);

        verify(prescriptionRestrictionService, times(1)).validateSpecialties(prescription.getPractitioner().getMedicalSpecialties());

        SecurityContextHolder.setContext(defaultSecurityContext);
    }

}
