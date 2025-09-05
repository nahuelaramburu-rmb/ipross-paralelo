package com.capacidad.validationapi.module.company.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.company.dto.CompanyDTO;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.company.projection.CompanyProjection;
import com.capacidad.validationapi.module.company.repository.CompanyRepository;
import com.capacidad.validationapi.module.company.service.CompanyService;
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

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;

@Service
public class CompanyServiceImpl extends BaseServiceImpl<Company, CompanyDTO, Long> implements CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyServiceImpl(CompanyRepository repository) {
        super(repository);
        this.companyRepository = repository;
    }

    @Override
    public Set<CompanyProjection> getCompanies(String name) {
        return companyRepository
                .findAllProjectedByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Company> findAllCompaniesTypedQuery(EntityManager entityManager) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Company> criteriaQuery = criteriaBuilder.createQuery(Company.class);
        Root<Company> root = criteriaQuery.from(Company.class);
        criteriaQuery.select(root);
        TypedQuery<Company> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException {
        Company company = this.findById(objectId);
        company.getBeneficiaries().forEach(b -> {
            b.setCompany(null);
            b.setPaymentMethod(VOLUNTARY.getInstance());
        });
        company.setDeleted(true);
        company.setDeletionToken(UUID.randomUUID());
        company.getAddress().setDeleted(true);
        company.getAddress().setDeletionToken(company.getDeletionToken());
        this.getRepository().save(company);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(company, this.getRepository()));
        return buildIdResponse(objectId);
    }

}
