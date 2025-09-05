package com.capacidad.validationapi.module.procedure.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;

import java.util.Map;
import java.util.Set;

public interface ProcedureService extends BaseProcedureService<Procedure, ProcedureDTO, ProcedureResolutionDTO> {

    Page<ProcedureProjection> findAllProcedures(Pageable pageable, String search);

    Map<String, PageModelWrapper<EntityModel<ProcedureProjection>>> findAllGroupedProcedures(Pageable pageable, String groups) throws ObjectNotValidException;

    Set<Procedure> findAllApprovedNotExpiredById(Set<Long> procedureIds) throws ObjectNotValidException;

    void resolveProcedureStatus();

    long findProcedureAndGetBeneficiaryId(long procedureId) throws ObjectNotFoundException;

}
