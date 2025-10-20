package com.capacidad.validationapi.module.beneficiary.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryInsurancePlanDTO;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryInsurancePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_BENEFICIARY_INSURANCE_PLANS;

@RestController
@RequestMapping(value = ENDPOINT_BENEFICIARY_INSURANCE_PLANS)
public class BeneficiaryInsurancePlanController extends ReducedBaseControllerImpl<BeneficiaryInsurancePlanDTO, Long> {

    @Autowired
    public BeneficiaryInsurancePlanController(BeneficiaryInsurancePlanService beneficiaryInsurancePlanService) {
        super(beneficiaryInsurancePlanService);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiaryInsurancePlan(#objectId)")
    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.getOne(objectId);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiaryInsurancePlan(#objectId)")
    @GetMapping(value = "{objectId}/audit-logs")
    @Override
    public ResponseEntity<Object> getAuditLogs(@PathVariable Long objectId) {
        return super.getAuditLogs(objectId);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiaryInsurancePlan(#objectId)")
    @PatchMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> updateOne(@PathVariable Long objectId, @NotEmpty @RequestBody Map<String, Object> update) throws ObjectNotFoundException, ObjectNotValidException {
        return super.updateOne(objectId, update);
    }

    @PreAuthorize("@beneficiaryChecker.hasAccessToBeneficiaryInsurancePlan(#objectId)")
    @DeleteMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> deleteOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return super.deleteOne(objectId);
    }

}
