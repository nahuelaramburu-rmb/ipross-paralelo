package com.capacidad.validationapi.module.batch.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.batch.dto.BatchItemDTO;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.projection.BatchItemProjection;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;

import java.util.Optional;

public interface BatchItemService extends BaseService<BatchItem, BatchItemDTO, Long> {

    BatchItemProjection create(BatchItemDTO input, Batch batch) throws ObjectNotValidException, ObjectNotFoundException;

    Optional<BatchItem> applyBatchItemCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem);

    Optional<BatchItem> findApplicableBatchItem(Batch batch, MedicalAuthorizationItem medicalAuthorizationItem);

    long findBatchItemAndGetParentId(long batchItemId) throws ObjectNotFoundException;

}
