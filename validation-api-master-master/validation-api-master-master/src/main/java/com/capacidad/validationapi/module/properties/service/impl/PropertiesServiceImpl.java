package com.capacidad.validationapi.module.properties.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.properties.dto.PropertiesDTO;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.projection.PropertiesProjection;
import com.capacidad.validationapi.module.properties.repository.PropertiesRepository;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Map;

import static com.capacidad.validationapi.functional.ThrowingSupplier.throwingSupplier;

@Service
public class PropertiesServiceImpl extends BaseServiceImpl<Properties, PropertiesDTO, Long> implements PropertiesService {

    private static final String NOT_FOUND_CODE = "properties.notFound";
    private final PropertiesRepository propertiesRepository;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public PropertiesServiceImpl(PropertiesRepository repository,
                                 ApplicationProperties applicationProperties) {
        super(repository);
        propertiesRepository = repository;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Properties getProperties() {
        return propertiesRepository
                .findByTenantId(TenantContext.getTenant())
                .orElseGet(throwingSupplier(() -> propertiesRepository.findByTenantIdIsNull()
                        .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_CODE))));
    }

    @Override
    public Properties getPropertiesTypedQuery(EntityManager entityManager) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Properties> criteriaQuery = criteriaBuilder.createQuery(Properties.class);
        Root<Properties> root = criteriaQuery.from(Properties.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("tenantId"), TenantContext.getTenant()));
        TypedQuery<Properties> typedQuery = entityManager.createQuery(criteriaQuery);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException e) {
            criteriaQuery.where(criteriaBuilder.isNull(root.get("tenantId")));
            return typedQuery.getSingleResult();
        }
    }

    @Override
    public PropertiesProjection getPropertiesProjection() {
        return propertiesRepository
                .findOptionallyByTenantId(TenantContext.getTenant())
                .orElseGet(throwingSupplier(() -> propertiesRepository.findOptionallyByTenantIdIsNull()
                        .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_CODE))));
    }

    @Override
    public PropertiesProjection update(Map<String, Object> update) throws ObjectNotValidException, ObjectNotFoundException {
        Properties props = propertiesRepository
                .findByTenantId(TenantContext.getTenant())
                .orElseGet(throwingSupplier(this::createFromDefault));
        return (PropertiesProjection) this.update(update, props.getId()).getContent();
    }

    @Override
    public ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    private Properties createFromDefault() throws ObjectNotFoundException {
        Properties defProps = propertiesRepository
                .findByTenantIdIsNull()
                .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_CODE));
        Properties customProps = new Properties(defProps);
        customProps.setTenantId(TenantContext.getTenant());
        return propertiesRepository.saveAndFlush(customProps);
    }

}
