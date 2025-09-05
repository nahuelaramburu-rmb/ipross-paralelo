package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.procedure.dto.CUDProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.hateoas.CUDProcedureResource;
import com.capacidad.validationapi.module.procedure.model.CUDProcedure;
import com.capacidad.validationapi.module.procedure.model.ProcedureResolution;
import com.capacidad.validationapi.module.procedure.projection.CUDProcedureProjection;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.CUDProcedureRepository;
import com.capacidad.validationapi.module.procedure.service.CUDProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CUDProcedureServiceImpl extends BaseProcedureServiceImpl<CUDProcedure, ProcedureDTO, CUDProcedureResolutionDTO> implements CUDProcedureService {

    @Autowired
    public CUDProcedureServiceImpl(CUDProcedureRepository repository) {
        super(repository);
    }

    @Override
    public EntityModel<ProcedureProjection> resolve(long procedureId, CUDProcedureResolutionDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        CUDProcedure procedure = this.findById(procedureId);
        if (input.getResolution().equals(ProcedureResolution.APPROVE)) {
            if (input.getDiagnosis() == null || input.getDiagnosis().isEmpty())
                throw new ObjectNotValidException("procedure.diagnosisRequirement");
            procedure.setExpiration(input.getExpiration());
            Set<ICD10Disease> mappedDiagnosis = input.getDiagnosis().stream()
                    .map(dg -> this.getUtils().getEntityReference(ICD10Disease.class, dg.getId()))
                    .collect(Collectors.toUnmodifiableSet());
            procedure.getDiagnosis().addAll(mappedDiagnosis);
        }
        CUDProcedure result = super.resolve(procedure, input);
        CUDProcedureProjection projection = this.getProjectionFactory().createProjection(CUDProcedureProjection.class, result);
        return new CUDProcedureResource(projection);
    }
}
