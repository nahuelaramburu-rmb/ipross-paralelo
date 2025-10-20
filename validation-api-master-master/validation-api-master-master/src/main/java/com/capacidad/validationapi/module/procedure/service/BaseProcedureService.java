package com.capacidad.validationapi.module.procedure.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.procedure.dto.FileTagDTO;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.model.FileTag;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.storage.dto.FileDTO;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BaseProcedureService<T extends Procedure, D extends ProcedureDTO, R extends ProcedureResolutionDTO> extends BaseService<T, D, Long> {

    ProcedureProjection create(D procedureDTO, List<MultipartFile> files) throws ObjectNotValidException, ObjectNotFoundException;

    ProcedureProjection update(Map<String, Object> update, long procedureId, List<MultipartFile> files, List<String> filesToRemove) throws ObjectNotValidException, ObjectNotFoundException;

    Message addMessage(long procedureId, MessageDTO message) throws ObjectNotFoundException, ObjectNotValidException;

    FileTag addFileTag(long objectId, FileTagDTO input) throws ObjectNotFoundException;

    void addFiles(long procedureId, List<MultipartFile> files) throws ObjectNotFoundException, ObjectNotValidException;

    void removeFile(long procedureId, String filename) throws ObjectNotValidException, ObjectNotFoundException;

    List<SummaryDTO> listFiles(long procedureId) throws ObjectNotValidException, ObjectNotFoundException;

    FileDTO getFile(long procedureId, String filename) throws ObjectNotValidException;

    EntityModel<ProcedureProjection> resolve(long procedureId, R input) throws ObjectNotFoundException, ObjectNotValidException;

    Set<Message> getMessages(long procedureId) throws ObjectNotFoundException;

}
