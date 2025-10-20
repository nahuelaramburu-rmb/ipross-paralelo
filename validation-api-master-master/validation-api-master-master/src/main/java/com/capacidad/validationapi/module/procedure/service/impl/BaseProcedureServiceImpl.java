package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.procedure.dto.FileTagDTO;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.event.ProcedureCreationEvent;
import com.capacidad.validationapi.module.procedure.event.ProcedureNewMessageEvent;
import com.capacidad.validationapi.module.procedure.event.ProcedureResolutionEvent;
import com.capacidad.validationapi.module.procedure.model.FileTag;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import com.capacidad.validationapi.module.procedure.model.ProcedureResolution;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.BaseProcedureRepository;
import com.capacidad.validationapi.module.procedure.service.BaseProcedureService;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.model.MultipartFileListWrapper;
import com.capacidad.validationapi.module.storage.service.StorageService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.BENEFICIARY;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.FUNDER;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;

@Log4j2
@Transactional(rollbackFor = Exception.class)
public abstract class BaseProcedureServiceImpl<T extends Procedure, D extends ProcedureDTO, R extends ProcedureResolutionDTO> extends BaseServiceImpl<T, D, Long> implements BaseProcedureService<T, D, R> {

    private final BaseProcedureRepository<T> repository;
    @Autowired
    private BeneficiaryFinder beneficiaryFinder;
    @Autowired
    private StorageService storageService;

    public BaseProcedureServiceImpl(BaseProcedureRepository<T> repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public ProcedureProjection create(D procedureDTO, List<MultipartFile> files) throws ObjectNotValidException, ObjectNotFoundException {
        T mappedProcedure = this.mapDtoToInput(procedureDTO);
        this.validate(mappedProcedure);
        mappedProcedure.setStatus(this.getUtils().getEntityReference(Status.class, PROCEDURE_REVISION.getId()));
        mappedProcedure.setFileCount(files != null ? files.size() : 0);
        T result = repository.save(mappedProcedure);
        storeFiles(result.getId(), files);
        if (SecurityUtils.isBeneficiary())
            this.getApplicationEventPublisher().publishEvent(new ProcedureCreationEvent(result, Collections.singletonList(FUNDER)));
        return this.getProjectionFactory().createProjection(ProcedureProjection.class, result);
    }

    @Override
    public ProcedureProjection update(Map<String, Object> update, long procedureId, List<MultipartFile> files, List<String> filesToRemove) throws ObjectNotValidException, ObjectNotFoundException {
        T procedure = this.inMemoryUpdate(update, procedureId);
        this.validateStatus(procedure);
        int removedFiles = 0;
        if (filesToRemove != null && !filesToRemove.isEmpty()) {
            removeFiles(procedure, filesToRemove);
            removedFiles = filesToRemove.size();
        }
        storeFiles(procedure.getId(), files);
        int newFileCount = procedure.getFileCount() + files.size() - removedFiles;
        procedure.setFileCount(newFileCount);
        T result = repository.save(procedure);
        return this.getProjectionFactory().createProjection(ProcedureProjection.class, result);
    }

    @Override
    public void validate(T procedure) throws ObjectAlreadyExistsException {
        if (repository.findExistentProcedure(procedure.getBeneficiary().getId(), PROCEDURE_REVISION.getId()))
            throw new ObjectAlreadyExistsException("procedure.alreadyInRevision");
    }

    @Override
    public void validateUpdate(T procedure) throws ObjectAlreadyExistsException {
        if (repository.findExistentProcedureIdIsNot(procedure.getBeneficiary().getId(), PROCEDURE_REVISION.getId(), procedure.getId()))
            throw new ObjectAlreadyExistsException("procedure.alreadyInRevision");
    }

    private void storeFiles(long procedureId, List<MultipartFile> files) throws ObjectNotValidException {
        if (files != null && !files.isEmpty()) {
            MultipartFileListWrapper multipartFiles = new MultipartFileListWrapper(files, procedureId);
            this.getStorageService().storeFile(FileType.BENEFICIARY_PROCEDURE, multipartFiles, false);
        }
    }

    private void removeFiles(T procedure, List<String> filenames) throws ObjectNotValidException {
        this.getStorageService().deleteFilesSync(FileType.BENEFICIARY_PROCEDURE, procedure.getId(), filenames);
        filenames.forEach(f -> removeFileTag(procedure, f));
    }

    @Override
    public Message addMessage(long procedureId, MessageDTO message) throws ObjectNotFoundException, ObjectNotValidException {
        T procedure = this.findById(procedureId);
        validateStatus(procedure);
        Message mappedMessage = this.getObjectMapper().convertValue(message, Message.class);
        mappedMessage.setFrom(SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse(""));
        mappedMessage.setSentAt(LocalDateTime.now());
        procedure.getMessages().add(mappedMessage);
        T result = repository.save(procedure);
        sendNewMessageNotification(result);
        return mappedMessage;
    }

    private void sendNewMessageNotification(T procedure) {
        String role = SecurityUtils.isBeneficiary() ? FUNDER : BENEFICIARY;
        this.getApplicationEventPublisher().publishEvent(new ProcedureNewMessageEvent(procedure, Collections.singletonList(role)));
    }

    @Override
    public Set<Message> getMessages(long procedureId) throws ObjectNotFoundException {
        T procedure = this.findById(procedureId);
        return procedure.getMessages();
    }

    @Override
    public void addFiles(long procedureId, List<MultipartFile> files) throws ObjectNotFoundException, ObjectNotValidException {
        T procedure = this.findById(procedureId);
        validateStatus(procedure);
        storeFiles(procedureId, files);
        procedure.setFileCount(procedure.getFileCount() + files.size());
        repository.save(procedure);
    }

    @Override
    public void removeFile(long procedureId, String filename) throws ObjectNotValidException, ObjectNotFoundException {
        T procedure = this.findById(procedureId);
        validateStatus(procedure);
        this.getStorageService().deleteFileSync(FileType.BENEFICIARY_PROCEDURE, procedureId, filename);
        procedure.setFileCount(procedure.getFileCount() - 1);
        removeFileTag(procedure, filename);
        repository.save(procedure);
    }

    private void removeFileTag(T procedure, String filename) {
        procedure.getFileTags().stream()
                .filter(t -> StringUtils.equals(t.getFilename(), filename))
                .findFirst()
                .ifPresent(t -> procedure.getFileTags().remove(t));
    }

    @Override
    public FileTag addFileTag(long objectId, FileTagDTO input) throws ObjectNotFoundException {
        T procedure = this.findById(objectId);
        FileTag fileTag = this.getObjectMapper().convertValue(input, FileTag.class);
        Optional<FileTag> optExistentTag = procedure.getFileTags().stream()
                .filter(ft -> StringUtils.equals(ft.getFilename(), fileTag.getFilename()))
                .findFirst();
        if (optExistentTag.isPresent()) {
            FileTag existentTag = optExistentTag.get();
            existentTag.setTag(fileTag.getTag());
            procedure.getFileTags().add(existentTag);
        } else
            procedure.getFileTags().add(fileTag);
        repository.save(procedure);
        return fileTag;
    }

    @Override
    public List<SummaryDTO> listFiles(long procedureId) throws ObjectNotValidException, ObjectNotFoundException {
        List<SummaryDTO> summaries = this.getStorageService().getFileList(FileType.BENEFICIARY_PROCEDURE, procedureId);
        T procedure = this.findById(procedureId);
        procedure.getFileTags()
                .forEach(ft -> summaries.stream()
                        .filter(s -> StringUtils.equals(s.getName(), ft.getFilename()))
                        .forEach(filtered -> filtered.setTag(ft.getTag())));
        return summaries;
    }

    @Override
    public FileDTO getFile(long procedureId, String filename) throws ObjectNotValidException {
        return this.getStorageService().retrieveFileAsDTO(FileType.BENEFICIARY_PROCEDURE, procedureId, filename);
    }

    private void validateStatus(T procedure) throws ObjectNotValidException {
        Long statusId = procedure.getStatus().getId();
        if (!statusId.equals(PROCEDURE_REVISION.getId()))
            throw new ObjectNotValidException(
                    "procedure.invalidStatus",
                    procedure.getId().toString());
    }

    T resolve(T procedure, R input) throws ObjectNotValidException {
        validateStatus(procedure);
        if (input.getResolution().equals(ProcedureResolution.REJECT))
            rejectProcedure(procedure, input);
        if (input.getResolution().equals(ProcedureResolution.APPROVE))
            approveProcedure(procedure, input);
        procedure.setClosedAt(LocalDateTime.now());
        T result = repository.save(procedure);
        this.getApplicationEventPublisher().publishEvent(new ProcedureResolutionEvent(result));
        return result;
    }

    private void approveProcedure(T procedure, R input) {
        Status approved = this.getUtils().getEntityReference(Status.class, PROCEDURE_APPROVED.getId());
        procedure.setStatus(approved);
        procedure.setExpiration(input.getExpiration());
    }

    private void rejectProcedure(T procedure, R input) throws ObjectNotValidException {
        if (StringUtils.isBlank(input.getReason()))
            throw new ObjectNotValidException("procedure.rejectionReasonRequirement");
        Message finalMessage = new Message();
        finalMessage.setSentAt(LocalDateTime.now());
        finalMessage.setFrom(SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse(""));
        finalMessage.setText(input.getReason());
        procedure.getMessages().add(finalMessage);
        Status rejected = this.getUtils().getEntityReference(Status.class, PROCEDURE_REJECTED.getId());
        procedure.setStatus(rejected);
    }

    @Override
    public Optional<Specification<T>> appendCustomSpecification() {
        if (SecurityUtils.isBeneficiary()) {
            try {
                Beneficiary beneficiary = beneficiaryFinder.findAuthBeneficiary();
                return Optional.of((root, query, builder) -> {
                    var join = root.join("beneficiary");
                    return builder.equal(join.get("familyId"), beneficiary.getFamilyId());
                });
            } catch (ObjectNotFoundException e) {
                log.error("Authenticated Authority does not exist as beneficiary: {}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    protected StorageService getStorageService() {
        return this.storageService;
    }

}
