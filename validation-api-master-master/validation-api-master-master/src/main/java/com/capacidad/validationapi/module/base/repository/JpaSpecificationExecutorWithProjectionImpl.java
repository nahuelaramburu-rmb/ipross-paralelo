package com.capacidad.validationapi.module.base.repository;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/***
 * Workaround for OPEN feature request https://jira.spring.io/browse/DATAJPA-1033
 */
@Log4j2
public class JpaSpecificationExecutorWithProjectionImpl<T, I extends Serializable> extends SimpleJpaRepository<T, I> implements JpaSpecificationExecutorWithProjection<T> {

    private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    private final EntityManager em;

    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "unchecked"})
    public JpaSpecificationExecutorWithProjectionImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
    }

    @Override
    public <P> List<P> findAllProjectedBy(Specification<T> spec, Class<P> projectionClass) {
        TypedQuery<T> query = getQuery(spec, Sort.unsorted());
        return query.getResultList().stream().map(item -> projectionFactory.createProjection(projectionClass, item)).collect(Collectors.toList());
    }

    @Override
    public <P> Page<P> findAllProjectedBy(Specification<T> spec, Class<P> projectionType, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable);
        return readPageWithProjection(spec, projectionType, pageable, query);
    }

    @Override
    @Transactional
    public void refresh(T entity) {
        em.refresh(entity);
    }

    @Override
    public Stream<T> findAll(Specification<T> spec, Map<String, Object> queryHints, Sort sort) {
        TypedQuery<T> query = getQuery(spec, sort);
        queryHints.forEach(query::setHint);
        return query.getResultStream();
    }

    @Override
    public Optional<T> findProjectedByIdWithHints(Class<T> entityClazz, Serializable id, Map<String, Object> queryHints) {
        return Optional.of(em.find(entityClazz, id, queryHints));
    }

    private <R> Page<R> readPageWithProjection(Specification<T> spec, Class<R> projectionType, Pageable pageable, TypedQuery<T> query) {
        query.getHints().forEach((key, value) -> log.debug("apply query hints -> {} : {}", key, value));
        Page<T> result = pageable.isUnpaged() ? new PageImpl<>(query.getResultList()) : readPage(query, getDomainClass(), pageable, spec);
        return result.map(item -> projectionFactory.createProjection(projectionType, item));
    }

}
