package com.capacidad.validationapi.module.batch.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.batch.dto.BatchDTO;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.projection.BatchProjection;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BatchService extends BaseService<Batch, BatchDTO, Long> {

    void addFiles(long procedureId, List<MultipartFile> files) throws ObjectNotFoundException, ObjectNotValidException;

    void removeFile(long procedureId, String filename) throws ObjectNotValidException, ObjectNotFoundException;

    List<SummaryDTO> listFiles(long procedureId) throws ObjectNotValidException;

    FileDTO getFile(long procedureId, String filename) throws ObjectNotValidException;

    BatchProjection.Full updateStatus(long batchId, StatusUpdateDTO input) throws ObjectNotValidException, ObjectNotFoundException;

    Map<String, PageModelWrapper<EntityModel<BatchProjection.Minor>>> findAllGroupedProcedures(Pageable pageable, String groups, long beneficiaryId) throws ObjectNotValidException;

    void resolveBatchStatus();

    Optional<Batch> findApplicableBatch(MedicalAuthorization medicalAuthorization);

    Optional<BatchItem> applyBatchItemCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem);

    Optional<BatchItem> findApplicableBatchItem(MedicalAuthorizationItem medicalAuthorizationItem);

    long findBatchAndGetBeneficiaryId(long batchId) throws ObjectNotFoundException;

}
