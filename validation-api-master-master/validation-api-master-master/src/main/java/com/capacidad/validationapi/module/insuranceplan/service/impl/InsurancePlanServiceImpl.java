package com.capacidad.validationapi.module.insuranceplan.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.insuranceplan.dto.InsurancePlanDTO;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlanType;
import com.capacidad.validationapi.module.insuranceplan.repository.InsurancePlanRepository;
import com.capacidad.validationapi.module.insuranceplan.repository.InsurancePlanTypeRepository;
import com.capacidad.validationapi.module.insuranceplan.service.InsurancePlanService;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class InsurancePlanServiceImpl extends BaseServiceImpl<InsurancePlan, InsurancePlanDTO, Long> implements InsurancePlanService {

    private final InsurancePlanTypeRepository insurancePlanTypeRepository;

    @Autowired
    public InsurancePlanServiceImpl(InsurancePlanRepository repository,
                                    InsurancePlanTypeRepository insurancePlanTypeRepository) {
        super(repository);
        this.insurancePlanTypeRepository = insurancePlanTypeRepository;
    }

    @Override
    public List<InsurancePlanType> getAllInsurancePlanTypes() {
        return insurancePlanTypeRepository.findAll();
    }

    @Override
    public List<InsurancePlan> findAllInsurancePlansTypedQuery(EntityManager entityManager) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<InsurancePlan> criteriaQuery = criteriaBuilder.createQuery(InsurancePlan.class);
        Root<InsurancePlan> root = criteriaQuery.from(InsurancePlan.class);
        criteriaQuery.select(root);
        TypedQuery<InsurancePlan> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        InsurancePlan insurancePlan = this.findById(objectId);
        if (!insurancePlan.getMedicalCoverages().isEmpty() && !allMedicalCoveragesDeleted(insurancePlan.getMedicalCoverages()))
            throw new ObjectNotValidException("insurancePlan.removeMedicalCoveragesFirst");
        insurancePlan.setDeleted(true);
        insurancePlan.setDeletionToken(UUID.randomUUID());
        insurancePlan.getBeneficiaryInsurancePlans().clear();
        this.getRepository().save(insurancePlan);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(insurancePlan, this.getRepository()));
        return buildIdResponse(objectId);
    }

    private boolean allMedicalCoveragesDeleted(Set<MedicalCoverage> medicalCoverages) {
        return medicalCoverages.size() == medicalCoverages.stream()
                .filter(BaseEntity::getDeleted)
                .count();
    }

}
