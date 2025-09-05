package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.dto.CertificateProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.hateoas.CertificateProcedureResource;
import com.capacidad.validationapi.module.procedure.model.CertificateProcedure;
import com.capacidad.validationapi.module.procedure.model.CertificateType;
import com.capacidad.validationapi.module.procedure.projection.CertificateProcedureProjection;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.CertificateProcedureRepository;
import com.capacidad.validationapi.module.procedure.repository.CertificateTypeRepository;
import com.capacidad.validationapi.module.procedure.service.CertificateProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateProcedureServiceImpl extends BaseProcedureServiceImpl<CertificateProcedure, CertificateProcedureDTO, ProcedureResolutionDTO> implements CertificateProcedureService {

    private final CertificateTypeRepository certificateTypeRepository;

    @Autowired
    public CertificateProcedureServiceImpl(CertificateProcedureRepository repository,
                                           CertificateTypeRepository certificateTypeRepository) {
        super(repository);
        this.certificateTypeRepository = certificateTypeRepository;
    }

    @Override
    public List<CertificateType> getAllCertificateTypes() {
        return certificateTypeRepository.findAll();
    }

    @Override
    public EntityModel<ProcedureProjection> resolve(long procedureId, ProcedureResolutionDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        CertificateProcedure procedure = this.findById(procedureId);
        CertificateProcedure result = super.resolve(procedure, input);
        CertificateProcedureProjection projection = this.getProjectionFactory().createProjection(CertificateProcedureProjection.class, result);
        return new CertificateProcedureResource(projection);
    }
}
