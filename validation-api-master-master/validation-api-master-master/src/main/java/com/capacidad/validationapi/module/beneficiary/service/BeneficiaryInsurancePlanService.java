package com.capacidad.validationapi.module.beneficiary.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryInsurancePlanDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryInsurancePlanProjection;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;

import java.util.Set;

public interface BeneficiaryInsurancePlanService extends BaseService<BeneficiaryInsurancePlan, BeneficiaryInsurancePlanDTO, Long> {

    BeneficiaryInsurancePlanProjection create(BeneficiaryInsurancePlanDTO dto, Beneficiary beneficiary) throws ObjectNotFoundException, ObjectNotValidException;

    void validate(Set<BeneficiaryInsurancePlan> beneficiaryInsurancePlans);

    Set<BeneficiaryInsurancePlanProjection> getBeneficiaryInsurancePlans(long beneficiaryId);

    void removeExpired();

    long findBeneficiaryInsurancePlanAndGetBeneficiaryId(long beneficiaryInsurancePlanId) throws ObjectNotFoundException;

    void removeAllRelatedToInsurancePlan(InsurancePlan insurancePlan);

}
