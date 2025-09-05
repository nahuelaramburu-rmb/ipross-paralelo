package com.capacidad.validationapi.module.batches.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.projection.BatchProjection;
import com.capacidad.validationapi.module.batch.repository.BatchRepository;
import com.capacidad.validationapi.module.batch.service.BatchItemService;
import com.capacidad.validationapi.module.batch.service.impl.BatchServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.model.StatusScope;
import com.capacidad.validationapi.module.general.reference.StatusScopeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BatchServiceImplTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private Utils utils;

    @Mock
    private BatchItemService batchItemService;

    @Spy
    @InjectMocks
    private BatchServiceImpl batchService;

    @Before
    public void init() {
        when(batchService.getUtils()).thenReturn(utils);
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidDates() {
        Batch batch = new Batch();
        batch.setDateFrom(LocalDate.now().plusDays(10));
        batch.setDateTo(LocalDate.now().plusDays(5));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> batchService.validate(batch));

        assertThat(exception.getMessage()).isEqualTo("generic.dateFromDateTo");
    }

    @Test
    public void testValidateThrowsExceptionWhenAlreadyExistsActive() {
        Batch batch = new Batch();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        batch.setBeneficiary(beneficiary);
        batch.setDateFrom(LocalDate.now().plusDays(5));
        batch.setDateTo(LocalDate.now().plusDays(10));

        Set<Long> statusIds = new HashSet<>();
        statusIds.add(BATCH_ACTIVE.getId());
        statusIds.add(BATCH_PENDING.getId());

        when(batchRepository
                .existsByBeneficiaryAndStatusInPeriod(statusIds,
                        batch.getDateFrom(),
                        batch.getDateTo(),
                        batch.getBeneficiary().getId()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> batchService.validate(batch));

        assertThat(exception.getMessage()).isEqualTo("batch.alreadyExists");
    }

    @Test
    public void testValidateDoNotFailsAndResolvesPendingStatus() throws ObjectNotValidException, ObjectNotFoundException {
        Batch batch = new Batch();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        batch.setBeneficiary(beneficiary);
        batch.setDateFrom(LocalDate.now().plusDays(10));
        batch.setDateTo(LocalDate.now().plusDays(10));
        BatchItem batchItem = new BatchItem();
        batch.getBatchItems().add(batchItem);

        Set<Long> statusIds = new HashSet<>();
        statusIds.add(BATCH_ACTIVE.getId());
        statusIds.add(BATCH_PENDING.getId());

        when(batchRepository
                .existsByBeneficiaryAndStatusInPeriod(statusIds,
                        batch.getDateFrom(),
                        batch.getDateTo(),
                        batch.getBeneficiary().getId()))
                .thenReturn(false);

        when(utils.getGenericsEntityReference(Status.class, BATCH_PENDING.getId())).thenReturn(BATCH_PENDING.getInstance());

        batchService.validate(batch);

        verify(batchItemService, times(1)).validate(batchItem);
        assertThat(batch.isPending()).isTrue();
    }

    @Test
    public void testUpdateStatusThrowsExceptionWhenBatchIsCancelled() throws ObjectNotFoundException {
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
        IdDTO<Long> idDTO = new IdDTO<>();
        idDTO.setId(BATCH_CANCELLED.getId());
        statusUpdateDTO.setStatus(idDTO);
        statusUpdateDTO.setStatusUpdateDescription("description");

        Status cancelled = new Status();
        cancelled.setId(BATCH_CANCELLED.getId());
        StatusScope batchScope = new StatusScope();
        batchScope.setId(StatusScopeReference.BATCH.getId());
        cancelled.setStatusScope(batchScope);

        Status batchCancelled = new Status();
        batchCancelled.setId(BATCH_CANCELLED.getId());

        Batch batch = new Batch();
        batch.setId(1L);
        batch.setStatus(batchCancelled);

        when(utils.getGenericsEntityReference(Status.class, statusUpdateDTO.getStatus().getId())).thenReturn(cancelled);
        doReturn(batch).when(batchService).findById(batch.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> batchService.updateStatus(batch.getId(), statusUpdateDTO));

        assertThat(exception.getMessage()).isEqualTo("batch.notActiveNorPending");
    }

    @Test
    public void testUpdateStatusThrowsExceptionWhenBatchIsExpired() throws ObjectNotFoundException {
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
        IdDTO<Long> idDTO = new IdDTO<>();
        idDTO.setId(BATCH_CANCELLED.getId());
        statusUpdateDTO.setStatus(idDTO);
        statusUpdateDTO.setStatusUpdateDescription("description");

        Status cancelled = new Status();
        cancelled.setId(BATCH_CANCELLED.getId());
        StatusScope batchScope = new StatusScope();
        batchScope.setId(StatusScopeReference.BATCH.getId());
        cancelled.setStatusScope(batchScope);

        Status batchExpired = new Status();
        batchExpired.setId(BATCH_EXPIRED.getId());

        Batch batch = new Batch();
        batch.setId(1L);
        batch.setStatus(batchExpired);

        when(utils.getGenericsEntityReference(Status.class, statusUpdateDTO.getStatus().getId())).thenReturn(cancelled);
        doReturn(batch).when(batchService).findById(batch.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> batchService.updateStatus(batch.getId(), statusUpdateDTO));

        assertThat(exception.getMessage()).isEqualTo("batch.notActiveNorPending");
    }

    @Test
    public void testUpdateStatusThrowsExceptionWhenInvalidStatusScope() {
        Status batchActive = new Status();
        batchActive.setId(BATCH_ACTIVE.getId());

        Batch batch = new Batch();
        batch.setId(1L);
        batch.setStatus(batchActive);

        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
        IdDTO<Long> idDTO = new IdDTO<>();
        idDTO.setId(PROCEDURE_REJECTED.getId());
        statusUpdateDTO.setStatus(idDTO);
        statusUpdateDTO.setStatusUpdateDescription("description");

        Status invalidStatus = new Status();
        invalidStatus.setId(PROCEDURE_REJECTED.getId());
        StatusScope invalidScope = new StatusScope();
        invalidScope.setId(StatusScopeReference.PROCEDURE.getId());
        invalidStatus.setStatusScope(invalidScope);

        when(utils.getGenericsEntityReference(Status.class, statusUpdateDTO.getStatus().getId())).thenReturn(invalidStatus);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> batchService.updateStatus(batch.getId(), statusUpdateDTO));

        assertThat(exception.getMessage()).isEqualTo("batch.invalidStatus");
    }

    @Test
    public void testUpdateStatusThrowsExceptionWhenStatusToUpdateIsExpired() {
        Status batchActive = new Status();
        batchActive.setId(BATCH_ACTIVE.getId());

        Batch batch = new Batch();
        batch.setId(1L);
        batch.setStatus(batchActive);

        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
        IdDTO<Long> idDTO = new IdDTO<>();
        idDTO.setId(BATCH_EXPIRED.getId());
        statusUpdateDTO.setStatus(idDTO);
        statusUpdateDTO.setStatusUpdateDescription("description");

        Status expired = new Status();
        expired.setId(BATCH_EXPIRED.getId());
        StatusScope batchScope = new StatusScope();
        batchScope.setId(StatusScopeReference.BATCH.getId());
        expired.setStatusScope(batchScope);

        when(utils.getGenericsEntityReference(Status.class, statusUpdateDTO.getStatus().getId())).thenReturn(expired);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> batchService.updateStatus(batch.getId(), statusUpdateDTO));

        assertThat(exception.getMessage()).isEqualTo("batch.invalidStatus");
    }

    @Test
    public void testUpdateStatusDoNotFailsWhenValidStatus() throws ObjectNotFoundException, ObjectNotValidException {
        Status batchActive = new Status();
        batchActive.setId(BATCH_ACTIVE.getId());

        Batch batch = new Batch();
        batch.setId(1L);
        batch.setStatus(batchActive);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setActiveBatch(true);

        batch.setBeneficiary(beneficiary);

        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
        IdDTO<Long> idDTO = new IdDTO<>();
        idDTO.setId(BATCH_CANCELLED.getId());
        statusUpdateDTO.setStatus(idDTO);
        statusUpdateDTO.setStatusUpdateDescription("description");

        Status cancelled = new Status();
        cancelled.setId(BATCH_CANCELLED.getId());
        StatusScope batchScope = new StatusScope();
        batchScope.setId(StatusScopeReference.BATCH.getId());
        cancelled.setStatusScope(batchScope);

        when(utils.getGenericsEntityReference(Status.class, statusUpdateDTO.getStatus().getId())).thenReturn(cancelled);
        when(batchRepository.save(batch)).thenReturn(batch);
        doReturn(batch).when(batchService).findById(batch.getId());

        BatchProjection.Full result = batchService.updateStatus(batch.getId(), statusUpdateDTO);

        assertThat(result.getStatus().getId()).isEqualTo(cancelled.getId());
        assertThat(result.getStatusUpdateDescription()).isEqualTo(statusUpdateDTO.getStatusUpdateDescription());
        assertThat(result.getBeneficiary().getActiveBatch()).isFalse();
    }

    @Test
    public void testResolveBatchStatusUpdatesExpiredAndPendingStatus() {
        Status batchActive = new Status();
        batchActive.setId(BATCH_ACTIVE.getId());

        Status batchPending = new Status();
        batchPending.setId(BATCH_PENDING.getId());

        Status batchExpired = new Status();
        batchExpired.setId(BATCH_EXPIRED.getId());

        Set<Batch> batches = new HashSet<>();

        Batch pendingBatch = new Batch();
        pendingBatch.setDateFrom(LocalDate.now());
        pendingBatch.setStatus(batchPending);
        pendingBatch.setBeneficiary(new Beneficiary());

        Batch activeBatch = new Batch();
        activeBatch.setDateTo(LocalDate.now().minusDays(1));
        activeBatch.setStatus(batchActive);
        activeBatch.setBeneficiary(new Beneficiary());

        batches.add(activeBatch);
        batches.add(pendingBatch);

        Set<Long> statusIds = new HashSet<>();
        statusIds.add(BATCH_ACTIVE.getId());
        statusIds.add(BATCH_PENDING.getId());

        when(batchRepository.findAllByStatusIdIn(statusIds)).thenReturn(batches);
        when(batchRepository.saveAll(batches)).thenReturn(new ArrayList<>(batches));
        when(utils.getGenericsEntityReference(Status.class, batchExpired.getId())).thenReturn(batchExpired);
        when(utils.getGenericsEntityReference(Status.class, batchActive.getId())).thenReturn(batchActive);

        batchService.resolveBatchStatus();

        assertThat(activeBatch.getStatus().getId()).isEqualTo(batchExpired.getId());
        assertThat(pendingBatch.getStatus().getId()).isEqualTo(batchActive.getId());
        assertThat(activeBatch.getBeneficiary().getActiveBatch()).isFalse();
        assertThat(pendingBatch.getBeneficiary().getActiveBatch()).isTrue();
    }

    @Test
    public void testResolveBatchStatusDoNotUpdatesNonExpiredAndPendingStatus() {
        Status batchActive = new Status();
        batchActive.setId(BATCH_ACTIVE.getId());

        Status batchPending = new Status();
        batchPending.setId(BATCH_PENDING.getId());

        Status batchExpired = new Status();
        batchExpired.setId(BATCH_EXPIRED.getId());

        Set<Batch> batches = new HashSet<>();

        Batch pendingBatch = new Batch();
        pendingBatch.setDateFrom(LocalDate.now().plusDays(2));
        pendingBatch.setStatus(batchPending);

        Batch activeBatch = new Batch();
        activeBatch.setDateTo(LocalDate.now().plusDays(1));
        activeBatch.setStatus(batchActive);

        batches.add(activeBatch);
        batches.add(pendingBatch);

        Set<Long> statusIds = new HashSet<>();
        statusIds.add(BATCH_ACTIVE.getId());
        statusIds.add(BATCH_PENDING.getId());

        when(batchRepository.findAllByStatusIdIn(statusIds)).thenReturn(batches);
        when(batchRepository.saveAll(batches)).thenReturn(new ArrayList<>(batches));

        batchService.resolveBatchStatus();

        assertThat(activeBatch.getStatus().getId()).isEqualTo(batchActive.getId());
        assertThat(pendingBatch.getStatus().getId()).isEqualTo(batchPending.getId());
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testValidateUpdateThrowsExceptionWhenBatchExistForSpecifiedDates() throws ObjectNotValidException {
        Batch batch = new Batch();
        batch.setId(1L);
        batch.setDateFrom(LocalDate.now());
        batch.setDateTo(LocalDate.now().plusDays(10));

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        batch.setBeneficiary(beneficiary);

        Set<Long> statusIds = new HashSet<>();
        statusIds.add(BATCH_ACTIVE.getId());
        statusIds.add(BATCH_PENDING.getId());

        when(batchRepository
                .existsByBeneficiaryAndStatusInPeriodAndIdIsNot(statusIds,
                        batch.getDateFrom(),
                        batch.getDateTo(),
                        batch.getId(),
                        beneficiary.getId()))
                .thenReturn(true);

        batchService.validateUpdate(batch);
    }

    @Test
    public void testValidateUpdateDoNotFailsWhenBatchDoesNotExistForSpecifiedDates() throws ObjectNotValidException {
        Batch batch = new Batch();
        batch.setId(1L);
        batch.setDateFrom(LocalDate.now());
        batch.setDateTo(LocalDate.now().plusDays(10));

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(1L);

        batch.setBeneficiary(beneficiary);

        Set<Long> statusIds = new HashSet<>();
        statusIds.add(BATCH_ACTIVE.getId());
        statusIds.add(BATCH_PENDING.getId());

        when(batchRepository
                .existsByBeneficiaryAndStatusInPeriodAndIdIsNot(statusIds,
                        batch.getDateFrom(),
                        batch.getDateTo(),
                        batch.getId(),
                        beneficiary.getId()))
                .thenReturn(false);
        when(utils.getGenericsEntityReference(Beneficiary.class, beneficiary.getId())).thenReturn(beneficiary);

        batchService.validateUpdate(batch);

        assertThat(beneficiary.getActiveBatch()).isTrue();
    }

}
