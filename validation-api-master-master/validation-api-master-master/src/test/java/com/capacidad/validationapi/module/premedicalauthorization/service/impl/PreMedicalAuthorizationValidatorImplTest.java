package com.capacidad.validationapi.module.premedicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorizationItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreMedicalAuthorizationValidatorImplTest {

    @Mock
    private Utils utils;

    @InjectMocks
    private PreMedicalAuthorizationValidatorImpl preMedicalAuthorizationValidator;

    @Test
    public void testValidateThrowsExceptionWhenInvalidStatus() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status expired = new Status();
        expired.setId(PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId());
        expired.setName(PRE_MEDICAL_AUTHORIZATION_EXPIRED.name());

        preMedicalAuthorization.setStatus(expired);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> preMedicalAuthorizationValidator.validate(medicalAuthorization));

        assertThat(exception.getMessage()).contains("preMedicalAuthorization.invalidStatus");
    }

    @Test
    public void testValidateThrowsExceptionWhenExpired() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().minusDays(1));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> preMedicalAuthorizationValidator.validate(medicalAuthorization));

        assertThat(exception.getMessage()).contains("preMedicalAuthorization.expired");
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidBeneficiary() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Beneficiary beneficiary2 = new Beneficiary();
        beneficiary2.setId(2L);

        preMedicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setBeneficiary(beneficiary2);

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(1));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> preMedicalAuthorizationValidator.validate(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("preMedicalAuthorization.invalidBeneficiary");
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidItemAmount() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        preMedicalAuthorization.setBeneficiary(beneficiary);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(new PreMedicalAuthorizationItem());
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(new PreMedicalAuthorizationItem());

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());
        medicalAuthorization.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());
        medicalAuthorization.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(1));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> preMedicalAuthorizationValidator.validate(medicalAuthorization));

        assertThat(exception.getMessage()).isEqualTo("preMedicalAuthorization.invalidItemAmount");
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidItem() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);

        preMedicalAuthorization.setBeneficiary(beneficiary);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator2);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(1));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> preMedicalAuthorizationValidator.validate(medicalAuthorization));

        assertThat(exception.getMessage()).contains("preMedicalAuthorization.itemDoesNotExists");
    }

    @Test
    public void testValidateThrowsExceptionWhenItemAuditing() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setAuditing(true);

        preMedicalAuthorization.setBeneficiary(beneficiary);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(1));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> preMedicalAuthorizationValidator.validate(medicalAuthorization));

        assertThat(exception.getMessage()).contains("preMedicalAuthorization.itemAuditing");
    }

    @Test
    public void testValidateSetAuditingOnPendingStatus() throws ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setAuditing(false);

        preMedicalAuthorization.setBeneficiary(beneficiary);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        Status pending = new Status();
        pending.setId(VALIDATION_PENDING.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setStatus(pending);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(1));

        preMedicalAuthorizationValidator.validate(medicalAuthorization);

        assertThat(preMedicalAuthorizationItem.getAuditing()).isTrue();
    }

    @Test
    public void testValidateThrowsExceptionWhenNoRemaining() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setAuditing(false);
        preMedicalAuthorizationItem.setRemaining(1);

        preMedicalAuthorization.setBeneficiary(beneficiary);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setQuantity(2);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(1));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> preMedicalAuthorizationValidator.validate(medicalAuthorization));

        assertThat(exception.getMessage()).contains("preMedicalAuthorization.itemAlreadyConsumed");
    }

    @Test
    public void testValidateSubstractRemainingWhenItemApprovedAndValid() throws ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPreMedicalAuthorization(preMedicalAuthorization);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setNomenclator(nomenclator);
        preMedicalAuthorizationItem.setAuditing(false);
        preMedicalAuthorizationItem.setRemaining(2);

        preMedicalAuthorization.setBeneficiary(beneficiary);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setQuantity(1);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(1));

        preMedicalAuthorizationValidator.validate(medicalAuthorization);

        assertThat(preMedicalAuthorizationItem.getRemaining()).isEqualTo(1);
    }

    @Test
    public void testDetermineStatusReturnsCancelledWhenAlreadyCancelled() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();

        Status cancelled = new Status();
        cancelled.setId(PRE_MEDICAL_AUTHORIZATION_CANCELLED.getId());

        preMedicalAuthorization.setStatus(cancelled);

        Status result = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);

        assertThat(result).isEqualTo(cancelled);
    }

    @Test
    public void testDetermineStatusReturnsConsumedWhenAllItemsConsumed() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setRemaining(0);
        PreMedicalAuthorizationItem preMedicalAuthorizationItem2 = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem2.setRemaining(0);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem2);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        preMedicalAuthorization.setStatus(active);

        Status consumed = new Status();
        consumed.setId(PRE_MEDICAL_AUTHORIZATION_CONSUMED.getId());

        when(utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_CONSUMED.getId())).thenReturn(consumed);

        Status result = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);

        assertThat(result).isEqualTo(consumed);
    }

    @Test
    public void testDetermineStatusReturnsExpiredWhenExpiredStatus() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setRemaining(0);
        PreMedicalAuthorizationItem preMedicalAuthorizationItem2 = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem2.setRemaining(2);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem2);

        Status expired = new Status();
        expired.setId(PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId());

        preMedicalAuthorization.setStatus(expired);

        when(utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId())).thenReturn(expired);

        Status result = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);

        assertThat(result).isEqualTo(expired);
    }

    @Test
    public void testDetermineStatusReturnsExpiredWhenActiveButExpiredDate() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setRemaining(0);
        PreMedicalAuthorizationItem preMedicalAuthorizationItem2 = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem2.setRemaining(2);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem2);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().minusDays(10));

        Status expired = new Status();
        expired.setId(PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId());

        when(utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId())).thenReturn(expired);

        Status result = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);

        assertThat(result).isEqualTo(expired);
    }

    @Test
    public void testDetermineStatusReturnsActiveByDefault() {
        PreMedicalAuthorization preMedicalAuthorization = new PreMedicalAuthorization();

        PreMedicalAuthorizationItem preMedicalAuthorizationItem = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem.setRemaining(0);
        PreMedicalAuthorizationItem preMedicalAuthorizationItem2 = new PreMedicalAuthorizationItem();
        preMedicalAuthorizationItem2.setRemaining(2);

        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem);
        preMedicalAuthorization.getPreMedicalAuthorizationItems().add(preMedicalAuthorizationItem2);

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        preMedicalAuthorization.setStatus(active);
        preMedicalAuthorization.setExpirationDate(LocalDate.now().plusDays(10));

        when(utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId())).thenReturn(active);

        Status result = preMedicalAuthorizationValidator.determineStatus(preMedicalAuthorization);

        assertThat(result).isEqualTo(active);
    }

}
