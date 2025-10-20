package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorConfig;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationValidatorImplTest {

    @Mock
    private NomenclatorService nomenclatorService;

    @Mock
    private PractitionerService practitionerService;

    @InjectMocks
    private MedicalAuthorizationValidatorImpl medicalAuthorizationValidator;

    @Test(expected = ObjectNotValidException.class)
    public void testValidateBeneficiaryStatusFailsWhenBeneficiaryStatusIsWithoutCoverage() throws ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        Status beneficiaryWithoutCoverageStatus = new Status();
        beneficiaryWithoutCoverageStatus.setId(StatusReference.BENEFICIARY_WITHOUT_COVERAGE.getId());
        beneficiary.setStatus(beneficiaryWithoutCoverageStatus);

        medicalAuthorizationValidator.validateBeneficiaryStatus(beneficiary);
    }

    @Test
    public void testValidateBeneficiaryStatusDoNotFailWhenBeneficiaryStatusIsWithCoverage() throws ObjectNotValidException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        Status beneficiaryWithCoverageStatus = new Status();
        beneficiaryWithCoverageStatus.setId(StatusReference.BENEFICIARY_WITH_COVERAGE.getId());
        beneficiary.setStatus(beneficiaryWithCoverageStatus);

        medicalAuthorizationValidator.validateBeneficiaryStatus(beneficiary);
    }


    @Test(expected = ObjectNotValidException.class)
    public void testValidatePractitionerStatusFailsWhenPractitionerStatusIsDisabled() throws ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        Status disabled = new Status();
        disabled.setId(StatusReference.DISABLED.getId());
        practitioner.setId(1L);
        practitioner.setStatus(disabled);

        medicalAuthorizationValidator.validatePractitionerStatus(practitioner);
    }

    @Test
    public void testValidatePractitionerStatusDoNotFailWhenPractitionerStatusIsAvailable() throws ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        Status available = new Status();
        available.setId(StatusReference.AVAILABLE.getId());
        practitioner.setId(1L);
        practitioner.setStatus(available);

        medicalAuthorizationValidator.validatePractitionerStatus(practitioner);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testGetValidatedNomenclatorFailsWhenPractitionerCannotPerformMedicalPractice() throws ObjectNotValidException, ObjectNotFoundException {
        Nomenclator nomenclator = new Nomenclator();
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setName("medicalPracticeTest");
        nomenclator.setId(1L);
        nomenclator.setMedicalPractice(medicalPractice);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        when(practitionerService.canPerformMedicalPractice(practitioner, nomenclator.getMedicalPractice())).thenReturn(false);
        when(nomenclatorService.findById(nomenclator.getId())).thenReturn(nomenclator);

        medicalAuthorizationValidator.getValidatedNomenclator(practitioner, nomenclator.getId());
    }

    @Test
    public void testGetValidatedNomenclatorDoNotFailWhenPractitionerCanPerformMedicalPractice() throws ObjectNotValidException, ObjectNotFoundException {
        Nomenclator nomenclator = new Nomenclator();
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setName("medicalPracticeTest");
        nomenclator.setId(1L);
        nomenclator.setMedicalPractice(medicalPractice);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        when(practitionerService.canPerformMedicalPractice(practitioner, nomenclator.getMedicalPractice())).thenReturn(true);
        when(nomenclatorService.findById(nomenclator.getId())).thenReturn(nomenclator);

        Nomenclator result = medicalAuthorizationValidator.getValidatedNomenclator(practitioner, nomenclator.getId());

        assertThat(result).isEqualTo(nomenclator);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testValidatePractitionerMedicalCenterFailsWhenPractitionerDoesNotBelongsToMedicalCenter() throws ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        when(practitionerService.belongsToMedicalCenter(practitioner, medicalCenter)).thenReturn(false);

        medicalAuthorizationValidator.validatePractitionerMedicalCenter(practitioner, medicalCenter);
    }

    @Test
    public void testValidatePractitionerMedicalCenterDoNotFailWhenPractitionerDoesNotBelongsToMedicalCenter() throws ObjectNotValidException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        when(practitionerService.belongsToMedicalCenter(practitioner, medicalCenter)).thenReturn(true);

        medicalAuthorizationValidator.validatePractitionerMedicalCenter(practitioner, medicalCenter);
    }

    @Test
    public void testValidateMaxQuantityThrowsExceptionWhenInvalidQuantity() {
        Nomenclator nomenclator = new Nomenclator();
        NomenclatorConfig nomenclatorConfig = new NomenclatorConfig();
        nomenclatorConfig.setMaxInTransaction(1);
        nomenclator.setNomenclatorConfig(nomenclatorConfig);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(2);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalAuthorizationValidator.validateMaxQuantity(medicalAuthorizationItem));

        assertThat(exception.getMessage()).isEqualTo("medicalAuthorization.itemMaxQuantityValidation");
    }

    @Test
    public void testValidateMaxQuantityDoNotFailsWhenValidQuantity() throws ObjectNotValidException {
        Nomenclator nomenclator = new Nomenclator();
        NomenclatorConfig nomenclatorConfig = new NomenclatorConfig();
        nomenclatorConfig.setMaxInTransaction(1);
        nomenclator.setNomenclatorConfig(nomenclatorConfig);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setQuantity(1);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        medicalAuthorizationValidator.validateMaxQuantity(medicalAuthorizationItem);
    }

    @Test
    public void testValidateAndSetPetitionerSetsPractitionerWhenNullPetitioner() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPractitioner(new Practitioner());

        medicalAuthorizationValidator.validateAndSetPetitioner(medicalAuthorization);

        assertThat(medicalAuthorization.getPetitioner()).isEqualTo(medicalAuthorization.getPractitioner());
    }

    @Test
    public void testValidateAndSetPetitionerFindsPetitionerWhenNotNullPetitioner() throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Practitioner petitioner = new Practitioner();
        petitioner.setId(1L);
        medicalAuthorization.setPetitioner(petitioner);

        Practitioner expected = new Practitioner();
        expected.setId(2L);

        when(practitionerService.findById(petitioner.getId())).thenReturn(expected);
        medicalAuthorizationValidator.validateAndSetPetitioner(medicalAuthorization);

        assertThat(medicalAuthorization.getPetitioner()).isEqualTo(expected);
    }

}
