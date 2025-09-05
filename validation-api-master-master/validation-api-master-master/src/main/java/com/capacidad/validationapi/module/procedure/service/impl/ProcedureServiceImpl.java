package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.model.ProjectionType;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.ProcedureRepository;
import com.capacidad.validationapi.module.procedure.service.ProcedureService;
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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.general.reference.StatusReference.PROCEDURE_APPROVED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PROCEDURE_EXPIRED;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProcedureServiceImpl extends BaseProcedureServiceImpl<Procedure, ProcedureDTO, ProcedureResolutionDTO> implements ProcedureService {

    private final ProcedureRepository procedureRepository;

    @Autowired
    public ProcedureServiceImpl(ProcedureRepository repository) {
        super(repository);
        this.procedureRepository = repository;
    }

    @Override
    public Page<ProcedureProjection> findAllProcedures(Pageable pageable, String search) {
        PageRequest pageRequest = this.buildPageRequest(pageable);
        return buildGenericProcedurePage(search, pageRequest);
    }

    @Override
    public Map<String, PageModelWrapper<EntityModel<ProcedureProjection>>> findAllGroupedProcedures(Pageable pageable, String groups) throws ObjectNotValidException {
        PageRequest pageRequest = this.buildPageRequest(pageable);
        List<SpecificationWrapper<Procedure>> specificationGroups = this.buildSpecificationGroups(groups);
        Map<String, PageModelWrapper<EntityModel<ProcedureProjection>>> pageResourceMap = new HashMap<>();
        for (SpecificationWrapper<Procedure> spec : specificationGroups) {
            Page<Procedure> procedurePage = procedureRepository.findAll(spec.getSpecification(), pageRequest);
            List<ProcedureProjection> procedureProjections = procedurePage.getContent().stream()
                    .map(this::buildProcedureProjection)
                    .collect(Collectors.toUnmodifiableList());
            Page<ProcedureProjection> projectionPage = new PageImpl<>(procedureProjections, procedurePage.getPageable(), procedurePage.getTotalElements());
            PageModelWrapper<EntityModel<ProcedureProjection>> pageModelWrapper = this.buildPageResource(projectionPage, pageable, spec.getSearch());
            pageResourceMap.put(spec.getKey(), pageModelWrapper);
        }
        return pageResourceMap;
    }

    @Transactional(readOnly = true)
    @Override
    public Set<Procedure> findAllApprovedNotExpiredById(Set<Long> procedureIds) throws ObjectNotValidException {
        Set<Procedure> results = procedureRepository.findAllApprovedNotExpiredByIdIn(procedureIds, PROCEDURE_APPROVED.getId());
        if (results.size() != procedureIds.size())
            throw new ObjectNotValidException("procedure.invalidProcedureItems");
        return results;
    }

    @Override
    public void resolveProcedureStatus() {
        Set<Procedure> procedures = procedureRepository
                .findAllByStatusIdAndExpirationNotNull(PROCEDURE_APPROVED.getId());
        Status expired = this.getUtils().getGenericsEntityReference(Status.class, PROCEDURE_EXPIRED.getId());
        procedures.forEach(p -> {
            if (LocalDate.now().isEqual(p.getExpiration()))
                p.setStatus(expired);
        });
        procedureRepository.saveAll(procedures);
    }

    @Override
    public long findProcedureAndGetBeneficiaryId(long procedureId) throws ObjectNotFoundException {
        return procedureRepository.findBeneficiaryIdProjectionById(procedureId)
                .map(i -> i.getBeneficiary().getId())
                .orElseThrow(() -> new ObjectNotFoundException("procedure.notFound"));
    }

    private Page<ProcedureProjection> buildGenericProcedurePage(String search, Pageable pageable) {
        Optional<Specification<Procedure>> specification = this.buildSpecification(search);
        Page<Procedure> procedurePage = specification.map(procedureSpecification -> procedureRepository.findAll(procedureSpecification, pageable)).orElseGet(() -> procedureRepository.findAll(pageable));
        List<ProcedureProjection> procedureProjections = procedurePage.getContent().stream()
                .map(this::buildProcedureProjection)
                .collect(Collectors.toUnmodifiableList());
        return new PageImpl<>(procedureProjections, pageable, procedurePage.getTotalElements());
    }

    private ProcedureProjection buildProcedureProjection(Procedure procedure) {
        Class<ProcedureProjection> projectionClazz = Utils.getEntityProjectionClass(procedure.getClass(), ProjectionType.ENTITY);
        return this.getProjectionFactory().createProjection(projectionClazz, procedure);
    }

    @Override
    public EntityModel<ProcedureProjection> resolve(long procedureId, ProcedureResolutionDTO input) {
        return null;
    }
}
