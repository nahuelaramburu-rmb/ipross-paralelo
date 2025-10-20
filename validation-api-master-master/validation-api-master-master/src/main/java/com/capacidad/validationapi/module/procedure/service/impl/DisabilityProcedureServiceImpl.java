package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.hateoas.DisabilityProcedureResource;
import com.capacidad.validationapi.module.procedure.model.DisabilityProcedure;
import com.capacidad.validationapi.module.procedure.projection.DisabilityProcedureProjection;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.DisabilityProcedureRepository;
import com.capacidad.validationapi.module.procedure.service.DisabilityProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
public class DisabilityProcedureServiceImpl extends BaseProcedureServiceImpl<DisabilityProcedure, ProcedureDTO, ProcedureResolutionDTO> implements DisabilityProcedureService {

    @Autowired
    public DisabilityProcedureServiceImpl(DisabilityProcedureRepository repository) {
        super(repository);
    }

    @Override
    public EntityModel<ProcedureProjection> resolve(long procedureId, ProcedureResolutionDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        DisabilityProcedure procedure = this.findById(procedureId);
        DisabilityProcedure result = super.resolve(procedure, input);
        DisabilityProcedureProjection projection = this.getProjectionFactory().createProjection(DisabilityProcedureProjection.class, result);
        return new DisabilityProcedureResource(projection);
    }
}
