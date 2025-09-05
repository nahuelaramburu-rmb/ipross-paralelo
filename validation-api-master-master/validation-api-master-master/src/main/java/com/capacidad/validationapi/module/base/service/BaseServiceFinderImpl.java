package com.capacidad.validationapi.module.base.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.misc.constant.ModelConstants;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.PageModelAssembler;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.model.ProjectionType;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.specification.SpecificationBuilder;
import com.capacidad.validationapi.specification.SpecificationWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.capacidad.validationapi.functional.ThrowingFunction.throwingFunction;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;

@Log4j2
public class BaseServiceFinderImpl<T extends BaseEntity<I>, I extends Serializable> implements BaseServiceFinder<T, I> {

    private final ExtendedJpaRepository<T, I> repository;
    private final Class<T> modelType;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    @Qualifier("specBuilder")
    private SpecificationBuilder<T, I> specificationBuilder;

    @SuppressWarnings("unchecked")
    public BaseServiceFinderImpl(ExtendedJpaRepository<T, I> repository) {
        this.repository = repository;
        Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(getClass(), BaseServiceFinderImpl.class);
        assert generics != null;
        this.modelType = (Class<T>) generics[0];
    }

    @Override
    public <P extends BaseProjection<I>> CollectionModel<EntityModel<P>> findAll() {
        Class<P> projectionClazz = Utils.getEntityProjectionClass(modelType, ProjectionType.ENTITY);
        log.info("findAll - no args, projection: {}", projectionClazz);
        List<P> objects = repository.findAllProjectedBy(projectionClazz);
        return getResources(objects);
    }

    @Override
    public <P extends BaseProjection<I>> CollectionModel<EntityModel<P>> findAll(Class<P> projectionClazz) {
        log.info("findAll - args: ({})", projectionClazz);
        List<P> objects = repository.findAllProjectedBy(projectionClazz);
        return getResources(objects);
    }

    @Override
    public <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable) {
        PageRequest pageRequest = buildPageRequest(pageable);
        Class<P> projectionClazz = Utils.getEntityProjectionClass(modelType, ProjectionType.ENTITY);
        log.info("findAll - args: ({})", projectionClazz);
        Page<P> page = repository.findAllProjectedBy(projectionClazz, pageRequest);
        return buildPageResource(page, pageable, "");
    }

    @Override
    public <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, String search) {
        Class<P> projectionClazz = Utils.getEntityProjectionClass(modelType, ProjectionType.ENTITY);
        log.info("findAll - args: ({}, {})", projectionClazz, search);
        return executeFindAll(pageable, projectionClazz, search, "");
    }

    @Override
    public <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, Class<P> projectionClass, String search) {
        log.info("findAll - args ({}, {}, {})", pageable.getClass(), projectionClass, search);
        return executeFindAll(pageable, projectionClass, search, "");
    }

    @Override
    public <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, String outerSearch, String innerSearch) {
        Class<P> projectionClazz = Utils.getEntityProjectionClass(modelType, ProjectionType.ENTITY);
        log.info("findAll - args: ({}, {}, {})", projectionClazz, outerSearch, innerSearch);
        return executeFindAll(pageable, projectionClazz, outerSearch, innerSearch);
    }

    @Override
    public <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> findAll(Pageable pageable, Class<P> projectionClass, String outerSearch, String innerSearch) {
        log.info("findAll - args: ({}, {}, {})", projectionClass, outerSearch, innerSearch);
        return executeFindAll(pageable, projectionClass, outerSearch, innerSearch);
    }

    @Override
    public <P extends BaseProjection<I>> EntityModel<Map<String, PageModelWrapper<EntityModel<P>>>> findAllGrouped(Pageable pageable, String groups) throws ObjectNotValidException {
        PageRequest pageRequest = this.buildPageRequest(pageable);
        List<SpecificationWrapper<T>> specificationGroups = this.buildSpecificationGroups(groups);
        Map<String, PageModelWrapper<EntityModel<P>>> pageResourceMap = new HashMap<>();
        for (SpecificationWrapper<T> specWrapper : specificationGroups) {
            Page<P> page = repository.findAllProjectedBy(specWrapper.getSpecification(), Utils.getEntityProjectionClass(modelType, ProjectionType.ENTITY), pageRequest);
            PageModelWrapper<EntityModel<P>> pageModelWrapper = this.buildPageResource(page, pageable, specWrapper.getSearch());
            pageResourceMap.put(specWrapper.getKey(), pageModelWrapper);
        }
        return new EntityModel<>(pageResourceMap);
    }

    @Override
    public Stream<T> findAll(Specification<T> spec, Map<String, Object> queryHints, Sort sort) {
        return repository.findAll(spec, queryHints, sort);
    }

    protected List<SpecificationWrapper<T>> buildSpecificationGroups(String groups) throws ObjectNotValidException {
        String[] groupList = groups.split(COMA);
        List<SpecificationWrapper<T>> specificationGroups = new ArrayList<>();
        for (String group : groupList) {
            String key = StringUtils.substringBefore(group, DASH);
            String value = StringUtils.substringAfter(group, DASH);
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value))
                throw new ObjectNotValidException(
                        "base.malformedGroupKey",
                        group);
            Optional<Specification<T>> specificationGroup = this.buildSpecification(value);
            specificationGroup.ifPresent(sp -> specificationGroups.add(new SpecificationWrapper<>(sp, value, key)));
        }
        return specificationGroups;
    }

    @Override
    public T findById(I objectId) throws ObjectNotFoundException {
        log.info("findById - args: id({})", objectId);
        return repository.findById(objectId)
                .orElseThrow(() -> new ObjectNotFoundException("base.notFound", String.valueOf(objectId)));
    }

    @Override
    public <P extends BaseProjection<I>> EntityModel<P> findProjectedById(I objectId) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("findProjectedById - args: {}({})", modelType.getSimpleName(), objectId);
        Class<P> projectionClass = Utils.getEntityProjectionClass(modelType, ProjectionType.ENTITY);
        P projection = findProjection(objectId, projectionClass);
        return getResource(projection);
    }

    @Override
    public <P extends BaseProjection<I>> EntityModel<P> findProjectedById(I objectId, Class<P> projectionClass) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("findProjectedById - args: {}({})", modelType.getSimpleName(), objectId);
        P projection = findProjection(objectId, projectionClass);
        return getResource(projection);
    }

    private <P extends BaseProjection<I>> P findProjection(I objectId, Class<P> projectionClass) throws ObjectNotFoundException {
        return repository.findProjectedById(objectId, projectionClass)
                .orElseThrow(() -> new ObjectNotFoundException("base.notFound", String.valueOf(objectId)));
    }

    @Override
    public Optional<Specification<T>> appendCustomSpecification() {
        return Optional.empty();
    }

    @Override
    public PageRequest buildPageRequest(Pageable pageable) {
        Sort sort = pageable.getSort().equals(Sort.unsorted()) ? Sort.by(ModelConstants.CREATED_AT).descending() : pageable.getSort();
        return PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), sort);
    }

    private <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> executeFindAll(Pageable pageable, Class<P> projectionClass, String outerSearch, String innerSearch) {
        PageRequest pageRequest = buildPageRequest(pageable);
        Optional<Specification<T>> specification = this.buildSpecification(StringUtils.isNotBlank(innerSearch) ? innerSearch : outerSearch);
        Page<P> page = specification.isPresent() ? repository.findAllProjectedBy(specification.get(), projectionClass, pageRequest)
                : repository.findAllProjectedBy(projectionClass, pageRequest);
        return buildPageResource(page, pageable, outerSearch);
    }

    @Override
    public Optional<Specification<T>> buildSpecification(String search) {
        Optional<Specification<T>> searchSpecification = this.getSpecificationBuilder().parseAndBuild(search);
        Optional<Specification<T>> customSpecification = this.appendCustomSpecification();
        if (searchSpecification.isPresent()) {
            if (customSpecification.isPresent()) {
                Specification<T> roleSpecificationObj = customSpecification.get();
                return Optional.ofNullable(searchSpecification.get().and(roleSpecificationObj));
            }
            return searchSpecification;
        }
        return customSpecification;
    }

    @Override
    public <P extends BaseProjection<I>> EntityModel<P> getResource(P projection) throws ObjectNotValidException, ObjectNotFoundException {
        EntityModel<P> resource = Utils.projectionToResourceMapping(modelType, projection);
        if (resource == null) {
            resource = new SelfModel<>(projection, Utils.getEntityControllerClass(modelType));
        }
        return resource;
    }

    @Override
    public <P extends BaseProjection<I>> PageModelWrapper<EntityModel<P>> buildPageResource(Page<P> page, Pageable pageable, String search) {
        CollectionModel<EntityModel<P>> resources = getResources(page.getContent());
        PageModelAssembler<EntityModel<P>> pageAssembler = new PageModelAssembler<>();
        if (StringUtils.isNotBlank(search)) {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add(SEARCH, search);
            return pageAssembler.toPageResource(getModelRelation(), resources, page, pageable, queryParams);
        }
        return pageAssembler.toPageResource(getModelRelation(), resources, page, pageable);
    }

    private <P extends BaseProjection<I>> CollectionModel<EntityModel<P>> getResources(List<P> projections) {
        List<EntityModel<P>> resourceList = projections.stream()
                .map(throwingFunction(this::getResource))
                .collect(Collectors.toUnmodifiableList());
        return new CollectionModelWrapper<>(getModelRelation(), resourceList);
    }

    private String getModelRelation() {
        Relation relation = modelType.getAnnotation(Relation.class);
        return relation != null ? relation.collectionRelation() : "content";
    }

    public SpecificationBuilder<T, I> getSpecificationBuilder() {
        return this.specificationBuilder;
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

}
