package com.capacidad.validationapi.module.batch.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.batch.dto.BatchDTO;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.batch.projection.BatchProjection;
import com.capacidad.validationapi.module.batch.repository.BatchRepository;
import com.capacidad.validationapi.module.batch.service.BatchItemService;
import com.capacidad.validationapi.module.batch.service.BatchRoleSpecificationBuilder;
import com.capacidad.validationapi.module.batch.service.BatchService;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusScopeReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.model.MultipartFileListWrapper;
import com.capacidad.validationapi.module.storage.service.StorageService;
import com.capacidad.validationapi.specification.SpecificationWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class BatchServiceImpl extends BaseServiceImpl<Batch, BatchDTO, Long> implements BatchService {

    private final BatchRepository batchRepository;
    private final StorageService storageService;
    private final BatchItemService batchItemService;
    private final BatchRoleSpecificationBuilder roleSpecificationBuilder;

    @Autowired
    public BatchServiceImpl(BatchRepository batchRepository,
                            StorageService storageService,
                            BatchItemService batchItemService,
                            BatchRoleSpecificationBuilder roleSpecificationBuilder) {
        super(batchRepository);
        this.batchRepository = batchRepository;
        this.storageService = storageService;
        this.batchItemService = batchItemService;
        this.roleSpecificationBuilder = roleSpecificationBuilder;
    }

    @Override
    public void validate(Batch batch) throws ObjectNotValidException {
        validateDates(batch);
        if (batchRepository.existsByBeneficiaryAndStatusInPeriod
                (getValidBatchStatusIds(), batch.getDateFrom(), batch.getDateTo(), batch.getBeneficiary().getId()))
            throw new ObjectAlreadyExistsException("batch.alreadyExists");
        resolveStatus(batch);
        batch.getBatchItems().forEach(throwingConsumer(batchItemService::validate));
    }

    private Set<Long> getValidBatchStatusIds() {
        Set<Long> statusIds = new HashSet<>();
        statusIds.add(BATCH_ACTIVE.getId());
        statusIds.add(BATCH_PENDING.getId());
        return statusIds;
    }

    private void validateDates(Batch batch) throws ObjectNotValidException {
        if (batch.getDateFrom().isAfter(batch.getDateTo()))
            throw new ObjectNotValidException("generic.dateFromDateTo");
    }

    private void resolveStatus(Batch batch) {
        if (dateIsEqualOrBeforeNow(batch.getDateFrom()) && dateIsEqualOrAfterNow(batch.getDateTo())) {
            batch.setStatus(this.getUtils().getGenericsEntityReference(Status.class, BATCH_ACTIVE.getId()));
            Beneficiary beneficiary = this.getUtils().getGenericsEntityReference(Beneficiary.class, batch.getBeneficiary().getId());
            beneficiary.setActiveBatch(true);
        } else
            batch.setStatus(this.getUtils().getGenericsEntityReference(Status.class, BATCH_PENDING.getId()));
    }

    private boolean dateIsEqualOrBeforeNow(LocalDate date) {
        return LocalDate.now().isEqual(date) || LocalDate.now().isAfter(date);
    }

    private boolean dateIsEqualOrAfterNow(LocalDate date) {
        return LocalDate.now().isEqual(date) || LocalDate.now().isBefore(date);
    }

    @Override
    public void validateUpdate(Batch batch) throws ObjectNotValidException {
        validateDates(batch);
        Long beneficiaryId = batch.getBeneficiary().getId();
        if (batchRepository.existsByBeneficiaryAndStatusInPeriodAndIdIsNot
                (getValidBatchStatusIds(), batch.getDateFrom(), batch.getDateTo(), batch.getId(), beneficiaryId))
            throw new ObjectAlreadyExistsException("batch.alreadyExists", beneficiaryId.toString());
        resolveStatus(batch);
    }

    @Override
    public void addFiles(long batchId, List<MultipartFile> files) throws ObjectNotFoundException, ObjectNotValidException {
        Batch batch = this.findById(batchId);
        validateStatus(batch);
        MultipartFileListWrapper multipartFiles = new MultipartFileListWrapper(files, batchId);
        storageService.storeFile(FileType.BATCH_ATTACHMENT, multipartFiles, false);
    }

    private void validateStatus(Batch batch) throws ObjectNotValidException {
        if (batch.isCancelled() || batch.isExpired())
            throw new ObjectNotValidException("batch.notActiveNorPending", batch.getId().toString());
    }

    @Override
    public void removeFile(long batchId, String filename) throws ObjectNotValidException, ObjectNotFoundException {
        Batch batch = this.findById(batchId);
        validateStatus(batch);
        storageService.deleteFileSync(FileType.BATCH_ATTACHMENT, batchId, filename);
    }

    @Override
    public List<SummaryDTO> listFiles(long batchId) throws ObjectNotValidException {
        return storageService.getFileList(FileType.BATCH_ATTACHMENT, batchId);
    }

    @Override
    public FileDTO getFile(long batchId, String filename) throws ObjectNotValidException {
        return storageService.retrieveFileAsDTO(FileType.BATCH_ATTACHMENT, batchId, filename);
    }

    @Override
    public BatchProjection.Full updateStatus(long batchId, StatusUpdateDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        Status newStatus = this.getUtils().getGenericsEntityReference(Status.class, input.getStatus().getId());
        if (!newStatus.getStatusScope().getId().equals(StatusScopeReference.BATCH.getId()) || newStatus.getId().equals(BATCH_EXPIRED.getId()))
            throw new ObjectNotValidException("batch.invalidStatus");
        Batch batch = this.findById(batchId);
        validateStatus(batch);
        batch.getBeneficiary().setActiveBatch(false);
        batch.setStatus(newStatus);
        batch.setStatusUpdateDescription(input.getStatusUpdateDescription());
        Batch result = batchRepository.save(batch);
        return this.getProjectionFactory().createProjection(BatchProjection.Full.class, result);
    }

    @Override
    public Map<String, PageModelWrapper<EntityModel<BatchProjection.Minor>>> findAllGroupedProcedures(Pageable pageable, String groups, long beneficiaryId) throws ObjectNotValidException {
        PageRequest pageRequest = this.buildPageRequest(pageable);
        String search = String.format("beneficiary:{id=%d}", beneficiaryId);
        Optional<Specification<Batch>> searchSpec = this.buildSpecification(search);
        List<SpecificationWrapper<Batch>> specificationGroups = this.buildSpecificationGroups(groups);
        Map<String, PageModelWrapper<EntityModel<BatchProjection.Minor>>> pageResourceMap = new HashMap<>();
        for (SpecificationWrapper<Batch> spec : specificationGroups) {
            Specification<Batch> groupSpec = spec.getSpecification();
            if (searchSpec.isPresent())
                groupSpec = groupSpec.and(searchSpec.get());
            Page<Batch> batchPage = batchRepository.findAll(groupSpec, pageRequest);
            List<BatchProjection.Minor> batchProjections = batchPage.getContent().stream()
                    .map(b -> this.getProjectionFactory().createProjection(BatchProjection.Minor.class, b))
                    .collect(Collectors.toUnmodifiableList());
            Page<BatchProjection.Minor> projectionPage = new PageImpl<>(batchProjections, batchPage.getPageable(), batchPage.getTotalElements());
            PageModelWrapper<EntityModel<BatchProjection.Minor>> pageModelWrapper = this.buildPageResource(projectionPage, pageable, spec.getSearch());
            pageModelWrapper.addQueryParam("minor", String.valueOf(Boolean.TRUE));
            pageResourceMap.put(spec.getKey(), pageModelWrapper);
        }
        return pageResourceMap;
    }

    @Override
    public void resolveBatchStatus() {
        Set<Batch> batches = batchRepository.findAllByStatusIdIn(getValidBatchStatusIds());
        batches.forEach(b -> {
            if (b.isActive() && b.nowIsAfterDateTo()) {
                b.setStatus(this.getUtils().getGenericsEntityReference(Status.class, BATCH_EXPIRED.getId()));
                b.getBeneficiary().setActiveBatch(false);
            }
            if (b.isPending() && b.nowIsEqualDateFrom()) {
                b.setStatus(this.getUtils().getGenericsEntityReference(Status.class, BATCH_ACTIVE.getId()));
                b.getBeneficiary().setActiveBatch(true);
            }
        });
        batchRepository.saveAll(batches);
    }

    @Override
    public Optional<Batch> findApplicableBatch(MedicalAuthorization medicalAuthorization) {
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();
        return batchRepository.findByBeneficiaryIdAndStatusId(beneficiary.getId(), BATCH_ACTIVE.getId());
    }

    @Override
    public Optional<BatchItem> applyBatchItemCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        return batchItemService.applyBatchItemCoverageToMedicalAuthorizationItem(medicalAuthorizationItem);
    }

    @Override
    public Optional<BatchItem> findApplicableBatchItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        Optional<Batch> optBatch = Optional.ofNullable(medicalAuthorizationItem.getMedicalAuthorization().getBatch());
        if (optBatch.isEmpty())
            return Optional.empty();
        return batchItemService.findApplicableBatchItem(optBatch.get(), medicalAuthorizationItem);
    }

    @Override
    public long findBatchAndGetBeneficiaryId(long batchId) throws ObjectNotFoundException {
        return batchRepository.findBeneficiaryIdProjectionById(batchId)
                .map(i -> i.getBeneficiary().getId())
                .orElseThrow(() -> new ObjectNotFoundException("batch.notFound", String.valueOf(batchId)));
    }

    @Override
    public Optional<Specification<Batch>> appendCustomSpecification() {
        return roleSpecificationBuilder.buildSpecification();
    }

}
