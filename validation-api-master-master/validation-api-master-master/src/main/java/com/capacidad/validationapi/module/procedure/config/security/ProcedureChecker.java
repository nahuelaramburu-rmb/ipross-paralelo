package com.capacidad.validationapi.module.procedure.config.security;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.beneficiary.config.security.BeneficiaryChecker;
import com.capacidad.validationapi.module.procedure.service.ProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcedureChecker {

    private final ProcedureService procedureService;
    private final BeneficiaryChecker beneficiaryChecker;

    @Autowired
    public ProcedureChecker(ProcedureService procedureService,
                            BeneficiaryChecker beneficiaryChecker) {
        this.procedureService = procedureService;
        this.beneficiaryChecker = beneficiaryChecker;
    }

    public boolean hasAccessToProcedure(long procedureId) throws ObjectNotFoundException {
        long beneficiaryId = procedureService.findProcedureAndGetBeneficiaryId(procedureId);
        return beneficiaryChecker.hasAccessToBeneficiary(beneficiaryId);
    }

}
