package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryCategory;
import com.capacidad.validationapi.module.beneficiary.repository.BeneficiaryCategoryRepository;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryCategoryService;
import com.capacidad.validationapi.module.general.dto.NameDTO;
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
import java.util.UUID;

@Service
public class BeneficiaryCategoryServiceImpl extends BaseServiceImpl<BeneficiaryCategory, NameDTO, Long> implements BeneficiaryCategoryService {

    @Autowired
    public BeneficiaryCategoryServiceImpl(BeneficiaryCategoryRepository repository) {
        super(repository);
    }

    @Override
    public List<BeneficiaryCategory> findAllBeneficiaryCategoriesTypedQuery(EntityManager entityManager) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BeneficiaryCategory> criteriaQuery = criteriaBuilder.createQuery(BeneficiaryCategory.class);
        Root<BeneficiaryCategory> root = criteriaQuery.from(BeneficiaryCategory.class);
        criteriaQuery.select(root);
        TypedQuery<BeneficiaryCategory> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException {
        BeneficiaryCategory beneficiaryCategory = this.findById(objectId);
        beneficiaryCategory.getBeneficiaries().forEach(b -> b.setBeneficiaryCategory(null));
        beneficiaryCategory.setDeleted(true);
        beneficiaryCategory.setDeletionToken(UUID.randomUUID());
        this.getRepository().save(beneficiaryCategory);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(beneficiaryCategory, this.getRepository()));
        return this.buildIdResponse(objectId);
    }

}
