package com.capacidad.validationapi.module.procedure.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseProcedureRepository<T extends Procedure> extends ExtendedJpaRepository<T, Long> {

    boolean findExistentProcedure(Long beneficiaryId, Long statusId);

    boolean findExistentProcedureIdIsNot(Long beneficiaryId, Long statusId, Long procedureId);

}
