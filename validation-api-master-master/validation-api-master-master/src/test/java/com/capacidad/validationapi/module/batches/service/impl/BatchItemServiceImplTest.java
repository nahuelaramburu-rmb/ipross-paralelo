package com.capacidad.validationapi.module.batches.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.service.impl.BatchItemServiceImpl;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationValidator;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BatchItemServiceImplTest {

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private MedicalAuthorizationValidator medicalAuthorizationValidator;

    @Mock
    private MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Mock
    private RestrictionTypeValidator restrictionTypeValidator;

    @Spy
    @InjectMocks
    private BatchItemServiceImpl batchItemService;

    @Test
    public void testApplyBatchItemCoverageReturnsEmptyWhenNullBatchItem() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setBatchItem(null);

        Optional<BatchItem> batchItem = batchItemService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        assertThat(batchItem).isNotPresent();
    }

    @Test
    public void testApplyBatchItemCoverageFailsWhenInvalidMedicalCenter() {
        Batch batch = new Batch();
        batch.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorCode("123456");
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenter invalidMedicalCenter = new MedicalCenter();
        invalidMedicalCenter.setId(2L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setMedicalCenter(invalidMedicalCenter);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        BatchItem batchItem = new BatchItem();
        batchItem.getMedicalCenters().add(medicalCenter);
        batchItem.setNomenclator(nomenclator);
        batchItem.setBatch(batch);

        medicalAuthorizationItem.setBatchItem(batchItem);

        RestrictionMessage restrictionMessage = mock(RestrictionMessage.class);
        Failure failure = new Failure();

        when(restrictionTypeValidator.buildRestrictionMessage("invalidMedicalCenter", "", null, null))
                .thenReturn(restrictionMessage);
        when(restrictionTypeValidator.buildFailure(any(RestrictionType.class), any(FailureType.class), any(RestrictionMessage.class)))
                .thenReturn(failure);

        Optional<BatchItem> result = batchItemService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        assertThat(result).contains(batchItem);
        assertThat(medicalAuthorizationItem.getFailures()).containsExactly(failure);
    }

    @Test
    public void testApplyBatchItemCoverageFailsWhenValidMedicalCenterInvalidPractitioner() {
        Batch batch = new Batch();
        batch.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorCode("12345");
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        Practitioner invalidPractitioner = new Practitioner();
        invalidPractitioner.setId(2L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setMedicalCenter(medicalCenter);
        medicalAuthorization.setPractitioner(invalidPractitioner);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        BatchItem batchItem = new BatchItem();
        batchItem.getMedicalCenters().add(medicalCenter);
        batchItem.getPractitioners().add(practitioner);
        batchItem.setBatch(batch);
        batchItem.setNomenclator(nomenclator);

        medicalAuthorizationItem.setBatchItem(batchItem);

        RestrictionMessage restrictionMessage = mock(RestrictionMessage.class);
        Failure failure = new Failure();

        when(restrictionTypeValidator.buildRestrictionMessage("invalidPractitioner", "", ", ", null))
                .thenReturn(restrictionMessage);
        when(restrictionTypeValidator.buildFailure(any(RestrictionType.class), any(FailureType.class), any(RestrictionMessage.class)))
                .thenReturn(failure);

        Optional<BatchItem> result = batchItemService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        assertThat(result).contains(batchItem);
        assertThat(medicalAuthorizationItem.getFailures()).containsExactly(failure);
    }

    @Test
    public void testApplyBatchItemCoverageFailsWhenValidMedicalCenterValidPractitionerInvalidPeriod() {
        Batch batch = new Batch();
        batch.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorCode("123456");
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setMedicalCenter(medicalCenter);
        medicalAuthorization.setPractitioner(practitioner);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        BatchItem batchItem = new BatchItem();
        batchItem.setAmount(2);
        batchItem.setPeriod(Period.MONTHLY);
        batchItem.getMedicalCenters().add(medicalCenter);
        batchItem.getPractitioners().add(practitioner);
        batchItem.setNomenclator(nomenclator);
        batchItem.setBatch(batch);

        medicalAuthorizationItem.setBatchItem(batchItem);

        long count = 3L;

        when(medicalAuthorizationItemService.countAllByBatchItemInPeriod(batchItem)).thenReturn(count);

        RestrictionMessage restrictionMessage = mock(RestrictionMessage.class);
        Failure failure = new Failure();

        when(restrictionTypeValidator.buildRestrictionMessage("limitExceeded", batchItem.getAmount().toString(), String.valueOf(count), null))
                .thenReturn(restrictionMessage);
        when(restrictionTypeValidator.buildFailure(any(RestrictionType.class), any(FailureType.class), any(RestrictionMessage.class)))
                .thenReturn(failure);

        Optional<BatchItem> result = batchItemService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        assertThat(result).contains(batchItem);
        assertThat(medicalAuthorizationItem.getFailures()).containsExactly(failure);
    }

    @Test
    public void testApplyBatchItemCoverageFailsWhenValidMedicalCenterNullPractitionerInvalidPeriod() {
        Batch batch = new Batch();
        batch.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorCode("12345");
        nomenclator.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setMedicalCenter(medicalCenter);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        BatchItem batchItem = new BatchItem();
        batchItem.setAmount(2);
        batchItem.setPeriod(Period.MONTHLY);
        batchItem.getMedicalCenters().add(medicalCenter);
        batchItem.setNomenclator(nomenclator);
        batchItem.setBatch(batch);

        medicalAuthorizationItem.setBatchItem(batchItem);

        long count = 3L;

        when(medicalAuthorizationItemService.countAllByBatchItemInPeriod(batchItem)).thenReturn(count);

        RestrictionMessage restrictionMessage = mock(RestrictionMessage.class);
        Failure failure = new Failure();

        when(restrictionTypeValidator.buildRestrictionMessage("limitExceeded", batchItem.getAmount().toString(), String.valueOf(count), null))
                .thenReturn(restrictionMessage);
        when(restrictionTypeValidator.buildFailure(any(RestrictionType.class), any(FailureType.class), any(RestrictionMessage.class)))
                .thenReturn(failure);

        Optional<BatchItem> result = batchItemService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        assertThat(result).contains(batchItem);
        assertThat(medicalAuthorizationItem.getFailures()).containsExactly(failure);
    }

    @Test
    public void testApplyBatchItemCoverageFailsWhenNullMedicalCenterValidPractitionerInvalidPeriod() {
        Batch batch = new Batch();
        batch.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorCode("123456");
        nomenclator.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setPractitioner(practitioner);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        BatchItem batchItem = new BatchItem();
        batchItem.setAmount(2);
        batchItem.setPeriod(Period.MONTHLY);
        batchItem.setNomenclator(nomenclator);
        batchItem.getPractitioners().add(practitioner);
        batchItem.setBatch(batch);

        medicalAuthorizationItem.setBatchItem(batchItem);

        long count = 3L;

        when(medicalAuthorizationItemService.countAllByBatchItemInPeriod(batchItem)).thenReturn(count);

        RestrictionMessage restrictionMessage = mock(RestrictionMessage.class);
        Failure failure = new Failure();

        when(restrictionTypeValidator.buildRestrictionMessage("limitExceeded", batchItem.getAmount().toString(), String.valueOf(count), null))
                .thenReturn(restrictionMessage);
        when(restrictionTypeValidator.buildFailure(any(RestrictionType.class), any(FailureType.class), any(RestrictionMessage.class)))
                .thenReturn(failure);

        Optional<BatchItem> result = batchItemService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);

        assertThat(result).contains(batchItem);
        assertThat(medicalAuthorizationItem.getFailures()).containsExactly(failure);
    }

    @Test
    public void testValidateExecuteValidatorWhenPractitionerIsNotNull() throws ObjectNotValidException, ObjectNotFoundException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        BatchItem batchItem = new BatchItem();
        batchItem.getPractitioners().add(practitioner);
        batchItem.setNomenclator(nomenclator);

        when(practitionerService.findById(practitioner.getId())).thenReturn(practitioner);
        doNothing().when(medicalAuthorizationValidator).validatePractitionerStatus(practitioner);
        when(medicalAuthorizationValidator.getValidatedNomenclator(practitioner, nomenclator.getId())).thenReturn(nomenclator);

        batchItemService.validate(batchItem);

        verify(medicalAuthorizationValidator, times(1)).validatePractitionerStatus(practitioner);
        verify(medicalAuthorizationValidator, times(1)).getValidatedNomenclator(practitioner, nomenclator.getId());
    }

    @Test
    public void testValidateDoNothingWhenPractitionerIsNull() throws ObjectNotValidException, ObjectNotFoundException {
        BatchItem batchItem = new BatchItem();
        batchItem.setPractitioners(null);

        batchItemService.validate(batchItem);

        verify(medicalAuthorizationValidator, never()).validatePractitionerStatus(any(Practitioner.class));
        verify(medicalAuthorizationValidator, never()).getValidatedNomenclator(any(Practitioner.class), anyLong());
    }

    @Test
    public void testValidateDoNothingWhenPractitionerIsEmpty() throws ObjectNotValidException, ObjectNotFoundException {
        BatchItem batchItem = new BatchItem();
        batchItem.setPractitioners(new HashSet<>());

        batchItemService.validate(batchItem);

        verify(medicalAuthorizationValidator, never()).validatePractitionerStatus(any(Practitioner.class));
        verify(medicalAuthorizationValidator, never()).getValidatedNomenclator(any(Practitioner.class), anyLong());
    }


}
