package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryInsurancePlanDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryInsurancePlanProjection;
import com.capacidad.validationapi.module.beneficiary.repository.BeneficiaryInsurancePlanRepository;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryInsurancePlanService;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.service.InsurancePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;

@Transactional
@Service
public class BeneficiaryInsurancePlanServiceImpl extends BaseServiceImpl<BeneficiaryInsurancePlan, BeneficiaryInsurancePlanDTO, Long> implements BeneficiaryInsurancePlanService {

    private final BeneficiaryInsurancePlanRepository beneficiaryInsurancePlanRepository;
    private final InsurancePlanService insurancePlanService;

    @Autowired
    public BeneficiaryInsurancePlanServiceImpl(BeneficiaryInsurancePlanRepository repository,
                                               InsurancePlanService insurancePlanService) {
        super(repository);
        this.beneficiaryInsurancePlanRepository = repository;
        this.insurancePlanService = insurancePlanService;
    }

    @Override
    public BeneficiaryInsurancePlanProjection create(BeneficiaryInsurancePlanDTO dto, Beneficiary beneficiary) throws ObjectNotFoundException, ObjectNotValidException {
        BeneficiaryInsurancePlan beneficiaryInsurancePlan = this.mapDtoToInput(dto);
        beneficiaryInsurancePlan.setBeneficiary(beneficiary);
        setDefaultPriority(beneficiaryInsurancePlan);
        this.validate(beneficiaryInsurancePlan);
        BeneficiaryInsurancePlan result = beneficiaryInsurancePlanRepository.saveAndFlush(beneficiaryInsurancePlan);
        beneficiaryInsurancePlanRepository.refresh(result);
        return this.getProjectionFactory().createProjection(BeneficiaryInsurancePlanProjection.class, result);
    }

    private void setDefaultPriority(BeneficiaryInsurancePlan beneficiaryInsurancePlan) throws ObjectNotFoundException {
        if (beneficiaryInsurancePlan.getPriority() == null) {
            InsurancePlan insurancePlan = insurancePlanService.findById(beneficiaryInsurancePlan.getInsurancePlan().getId());
            beneficiaryInsurancePlan.setPriority(insurancePlan.getPriority());
        }
    }

    @Override
    public void validate(Set<BeneficiaryInsurancePlan> beneficiaryInsurancePlans) {
        beneficiaryInsurancePlans.forEach(throwingConsumer(this::setDefaultPriority));
    }

    @Override
    public Set<BeneficiaryInsurancePlanProjection> getBeneficiaryInsurancePlans(long beneficiaryId) {
        return beneficiaryInsurancePlanRepository.findAllProjectedByBeneficiaryId(beneficiaryId);
    }

    @Override
    public void removeExpired() {
        Set<BeneficiaryInsurancePlan> expiredPlans = beneficiaryInsurancePlanRepository
                .findAllByExpirationDateNotNullAndExpirationDateLessThan(LocalDate.now());
        beneficiaryInsurancePlanRepository.deleteAll(expiredPlans);
    }

    @Override
    public long findBeneficiaryInsurancePlanAndGetBeneficiaryId(long beneficiaryInsurancePlanId) throws ObjectNotFoundException {
        return beneficiaryInsurancePlanRepository.findBeneficiaryIdProjectionById(beneficiaryInsurancePlanId)
                .map(i -> i.getBeneficiary().getId())
                .orElseThrow(() -> new ObjectNotFoundException("beneficiaryInsurancePlan.notFound"));
    }

    @Override
    public void removeAllRelatedToInsurancePlan(InsurancePlan insurancePlan) {
        List<BeneficiaryInsurancePlan> beneficiaryInsurancePlans = beneficiaryInsurancePlanRepository
                .findAllByInsurancePlanId(insurancePlan.getId());
        beneficiaryInsurancePlanRepository.deleteAll(beneficiaryInsurancePlans);
    }

}
