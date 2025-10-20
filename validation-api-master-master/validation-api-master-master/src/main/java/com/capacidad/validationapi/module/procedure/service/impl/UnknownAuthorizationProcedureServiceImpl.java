package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.dto.UnknownAuthorizationProcedureDTO;
import com.capacidad.validationapi.module.procedure.hateoas.UnknownAuthorizationProcedureResource;
import com.capacidad.validationapi.module.procedure.model.UnknownAuthorizationProcedure;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.projection.UnknownAuthorizationProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.UnknownAuthorizationProcedureRepository;
import com.capacidad.validationapi.module.procedure.service.UnknownAuthorizationProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
public class UnknownAuthorizationProcedureServiceImpl extends BaseProcedureServiceImpl<UnknownAuthorizationProcedure, UnknownAuthorizationProcedureDTO, ProcedureResolutionDTO> implements UnknownAuthorizationProcedureService {

    private final UnknownAuthorizationProcedureRepository unknownAuthorizationProcedureRepository;

    @Autowired
    public UnknownAuthorizationProcedureServiceImpl(UnknownAuthorizationProcedureRepository unknownAuthorizationProcedureRepository) {
        super(unknownAuthorizationProcedureRepository);
        this.unknownAuthorizationProcedureRepository = unknownAuthorizationProcedureRepository;
    }

    @Override
    public UnknownAuthorizationProcedureProjection findProcedureByAuthorizationId(long authorizationId) throws ObjectNotFoundException {
        return unknownAuthorizationProcedureRepository.findByMedicalAuthorizationId(authorizationId)
                .orElseThrow(() -> new ObjectNotFoundException("procedure.relatedAuthorizationNotFound", String.valueOf(authorizationId)));
    }

    @Override
    public EntityModel<ProcedureProjection> resolve(long procedureId, ProcedureResolutionDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        UnknownAuthorizationProcedure procedure = this.findById(procedureId);
        UnknownAuthorizationProcedure result = super.resolve(procedure, input);
        UnknownAuthorizationProcedureProjection projection = this.getProjectionFactory().createProjection(UnknownAuthorizationProcedureProjection.class, result);
        return new UnknownAuthorizationProcedureResource(projection);
    }
}
