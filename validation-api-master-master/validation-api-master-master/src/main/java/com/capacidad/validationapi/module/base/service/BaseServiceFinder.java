package com.capacidad.validationapi.module.base.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface BaseServiceFinder<T extends BaseEntity<I>, I extends Serializable> {

    T findById(I objectId) throws ObjectNotFoundException;

    <P extends BaseProjection<I>> EntityModel<P> findProjectedById(I objectId) throws ObjectNotFoundException, ObjectNotValidException;

    <P extends BaseProjection<I>> EntityModel<P> findProjectedById(I objectId, Class<P> projectionClazz) throws ObjectNotFoundException, ObjectNotValidException;

    <P extends BaseProjection<I>> CollectionModel<EntityModel<P>> findAll();

    <P extends BaseProjection<I>> CollectionModel<EntityModel<P>> findAll(Class<P> projectionClazz);

    <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable);

    <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, String search);

    <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, Class<P> projectionClass, String search);

    <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, String outerSearch, String innerSearch);

    <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, Class<P> projectionClass, String outerSearch, String innerSearch);

    <P extends BaseProjection<I>> EntityModel<Map<String, PageModelWrapper<EntityModel<P>>>> findAllGrouped(Pageable pageable, String groups) throws ObjectNotValidException;

    PageRequest buildPageRequest(Pageable pageable);

    Optional<Specification<T>> appendCustomSpecification();

    Optional<Specification<T>> buildSpecification(String search);

    <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> buildPageResource(Page<P> page, Pageable pageable, String search);

    <P extends BaseProjection<I>> EntityModel<P> getResource(P projection) throws ObjectNotValidException, ObjectNotFoundException;

    Stream<T> findAll(Specification<T> spec, Map<String, Object> queryHints, Sort sort);

}
