package com.capacidad.validationapi.module.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@NoRepositoryBean
public interface JpaSpecificationExecutorWithProjection<T> extends JpaSpecificationExecutor<T> {

    <P> List<P> findAllProjectedBy(Specification<T> spec, Class<P> projectionClass);

    <P> Page<P> findAllProjectedBy(Specification<T> spec, Class<P> projectionClass, Pageable pageable);

    Stream<T> findAll(Specification<T> spec, Map<String, Object> queryHints, Sort sort);

    Optional<T> findProjectedByIdWithHints(Class<T> entityClazz, Serializable id, Map<String, Object> queryHints);

    void refresh(T entity);

}
