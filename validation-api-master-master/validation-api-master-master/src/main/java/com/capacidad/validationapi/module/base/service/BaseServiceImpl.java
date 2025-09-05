package com.capacidad.validationapi.module.base.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.annotation.Immutable;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.model.AuditLog;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.model.DTOType;
import com.capacidad.validationapi.module.base.model.ProjectionType;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.SmartValidator;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.BiThrowingConsumer.biThrowingConsumer;
import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.WHITESPACE;
import static com.capacidad.validationapi.misc.constant.ModelConstants.MODIFIED_AT;


@Log4j2
@Getter
public abstract class BaseServiceImpl<T extends BaseEntity<I>, R extends BaseDTO<I>, I extends Serializable> extends BaseServiceFinderImpl<T, I> implements BaseService<T, R, I> {

    private static final String NOT_FOUND_TEMPLATE = "Object not found with id: %s";
    private final ExtendedJpaRepository<T, I> repository;
    private final ProjectionFactory projectionFactory;
    private final Class<T> modelType;
    private final Class<R> dtoType;
    @Autowired
    @Qualifier("customLocaleValidator")
    private SmartValidator validator;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private Utils utils;

    @SuppressWarnings("unchecked")
    public BaseServiceImpl(ExtendedJpaRepository<T, I> repository) {
        super(repository);
        this.repository = repository;
        Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(getClass(), BaseServiceImpl.class);
        assert generics != null;
        this.modelType = (Class<T>) generics[0];
        this.dtoType = (Class<R>) generics[1];
        this.projectionFactory = new SpelAwareProxyProjectionFactory();
    }

    @Override
    public T create(R dto) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        T object = this.mapDtoToInput(dto);
        object.setId(null);
        this.validate(object);
        object.associateChildObjects();
        return repository.save(object);
    }

    @Override
    public T inMemoryUpdate(Map<String, Object> objectMap, I objectId) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("updateObject - args: {}({}), id({})", objectMap.getClass(), objectMap, objectId);
        T objectDb = this.findById(objectId);
        executeUpdate(objectMap, objectDb);
        return objectDb;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends BaseProjection<I>> EntityModel<P> update(Map<String, Object> objectMap, I objectId) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("update - args: {}({}), id({})", objectMap.getClass(), objectMap, objectId);
        T updatedObject = executeUpdate(objectMap, objectId);
        Class<P> projectionClazz = Utils.getEntityProjectionClass(updatedObject.getClass(), ProjectionType.ENTITY);
        return getResource(this.getProjectionFactory().createProjection(projectionClazz, updatedObject));
    }

    private T executeUpdate(Map<String, Object> objectMap, I objectId) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("update - args: {}({}), id({})", objectMap.getClass(), objectMap, objectId);
        T objectDb = this.findById(objectId);
        executeUpdate(objectMap, objectDb);
        return repository.save(objectDb);
    }

    @Override
    public <P extends BaseProjection<I>> EntityModel<P> update(Map<String, Object> objectMap, I objectId, Class<P> projectionClazz) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("update - args: {}({}), {}, id({})", objectMap.getClass(), objectMap, projectionClazz, objectId);
        T updatedObject = executeUpdate(objectMap, objectId);
        return getResource(this.getProjectionFactory().createProjection(projectionClazz, updatedObject));
    }

    private void executeUpdate(Map<String, Object> objectMap, T objectDb) throws ObjectNotValidException, ObjectNotFoundException {
        Class<R> updateDtoClass = Utils.getEntityDto(modelType, DTOType.UPDATE);
        Class<R> dtoClass = updateDtoClass == null ? dtoType : updateDtoClass;
        R objectDto = this.getObjectMapper().convertValue(objectMap, dtoClass);
        validateDtoFields(objectDto);
        updateValues(objectMap, objectDb, objectDto);
        this.validateUpdate(objectDb);
        objectDb.associateChildObjects();
    }

    private void validateDtoFields(R objectDto) throws ObjectNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(objectDto, objectDto.getClass().getSimpleName());
        this.getValidator().validate(objectDto, bindingResult);
        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                if (fieldError.getRejectedValue() != null) {
                    String defaultMessage = StringUtils.join(COMA, WHITESPACE, fieldError.getDefaultMessage());
                    throw new ObjectNotValidException("base.invalidFieldValue", fieldError.getField(), defaultMessage);
                }
            }
        }
    }

    private void updateValues(Map<String, Object> objectMap, T objectDb, R objectDto) {
        objectMap.forEach(biThrowingConsumer((k, v) -> {
            Field objectDtoField = ReflectionUtils.findField(objectDto.getClass(), k);
            if (objectDtoField == null || objectDtoField.getAnnotation(Immutable.class) != null)
                throw new ObjectNotValidException("base.invalidField", k);
            objectDtoField.setAccessible(true);
            Object fieldValue = objectDtoField.get(objectDto);
            Field objectDbField = ReflectionUtils.findField(objectDb.getClass(), k);
            if (objectDbField != null)
                updateValues(objectDbField, fieldValue, v, objectMap, objectDb);
        }));
    }

    @SuppressWarnings("unchecked")
    private void updateValues(Field objectDbField, Object fieldValue, Object currentValue, Map<String, Object> objectMap, T objectDb) throws IllegalAccessException, ObjectNotValidException {
        objectDbField.setAccessible(true);
        if (fieldValue instanceof IdDTO) {
            IdDTO<I> idDTO = (IdDTO<I>) fieldValue;
            updateObjectReference(idDTO, objectDbField, objectDb);
        }
        if (!(fieldValue instanceof IdDTO) && fieldValue instanceof BaseDTO)
            updateNestedObject(objectDbField, objectMap, objectDb);
        if (fieldValue instanceof Collection)
            updateCollection(objectDbField, objectDb, fieldValue);
        if (!(fieldValue instanceof BaseDTO) && !(fieldValue instanceof Collection))
            updateSimpleAttribute(currentValue, objectDbField, objectDb);
    }

    private void updateSimpleAttribute(Object fieldValue, Field field, T objectDb) throws ObjectNotValidException {
        try {
            fieldValue = this.getObjectMapper().readValue(this.getObjectMapper().writeValueAsBytes(fieldValue), field.getType());
        } catch (IOException e) {
            throw new ObjectNotValidException("base.invalidFieldValue", field.getName(), "");
        }
        ReflectionUtils.setField(field, objectDb, fieldValue);
    }

    @SuppressWarnings("unchecked")
    private void updateObjectReference(IdDTO<I> idDTO, Field field, T objectDb) {
        T entityReference = this.getUtils().getGenericsEntityReference((Class<T>) field.getType(), idDTO.getId());
        T instance = (T) Hibernate.unproxy(entityReference);
        ReflectionUtils.setField(field, objectDb, instance);
    }

    @SuppressWarnings("unchecked")
    private void updateNestedObject(Field field, Map<String, Object> v, T objectDb) {
        Object fieldValue = ReflectionUtils.getField(field, objectDb);
        T instance = (T) Hibernate.unproxy(fieldValue);
        if (instance == null)
            instance = createNewObjectInstance(field);
        Class<R> updateDtoClass = Utils.getEntityDto((Class<T>) field.getType(), DTOType.UPDATE);
        Class<R> dtoClass = updateDtoClass == null ? Utils.getEntityDto((Class<T>) field.getType(), DTOType.CREATION) : updateDtoClass;
        Object innerValue = v.get(field.getName());
        R objectDto = this.getObjectMapper().convertValue(innerValue, dtoClass);
        updateValues((Map<String, Object>) innerValue, instance, objectDto);
        ReflectionUtils.setField(field, objectDb, instance);
    }

    @SuppressWarnings("unchecked")
    private T createNewObjectInstance(Field field) {
        try {
            Class<T> fieldClass = (Class<T>) field.getType();
            Constructor<T> constructor = fieldClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            log.debug("Cannot create new instance of object ({}): {}", field.getType(), e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void updateCollection(Field objectDbField, T objectDb, Object fieldValue) throws IllegalAccessException {
        Collection<T> objectDbList = (Collection<T>) objectDbField.get(objectDb);
        ParameterizedType objectDbListType = (ParameterizedType) objectDbField.getGenericType();
        Class<T> listGenericType = (Class<T>) objectDbListType.getActualTypeArguments()[0];
        Collection<R> nestedList = (Collection<R>) fieldValue;
        objectDbList.clear();
        nestedList.forEach(throwingConsumer(element -> {
            try {
                T entity = this.getObjectMapper().readValue(this.getObjectMapper().writeValueAsBytes(element), listGenericType);
                if (element instanceof IdDTO)
                    entity = this.getUtils().getGenericsEntityReference(listGenericType, entity.getId());
                objectDbList.add(entity);
            } catch (IOException e) {
                throw new ObjectNotValidException("base.invalidFieldValue", element.getClass().getSimpleName(), "");
            }
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends BaseProjection<I>> List<AuditLog<P, I>> auditLogs(Class<P> projectionClass, I id) {
        Class<P> projectionClazz = projectionClass != null ? projectionClass : Utils.getEntityProjectionClass(modelType, ProjectionType.AUDIT);
        try {
            AuditReader reader = this.getUtils().getAuditReader();
            AuditQuery query = reader.createQuery()
                    .forRevisionsOfEntityWithChanges(modelType, true)
                    .add(AuditEntity.id().eq(id))
                    .addOrder(AuditEntity.property(MODIFIED_AT).asc());
            List<Object[]> revision = query.getResultList();
            return revision.stream()
                    .map(r -> buildAuditLogObject(projectionClazz, r))
                    .collect(Collectors.toUnmodifiableList());
        } catch (AuditException e) {
            log.error("Error reading audit of entity ({}): {}", modelType, e.getMessage());
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private <P extends BaseProjection<I>> AuditLog<P, I> buildAuditLogObject(Class<P> projectionClass, Object[] revisionList) {
        P projection = this.getProjectionFactory().createProjection(projectionClass, revisionList[0]);
        RevisionType revisionType = (RevisionType) revisionList[2];
        Set<String> modifiedFields = (Set<String>) revisionList[3];
        AuditLog<P, I> auditLog = new AuditLog<>();
        auditLog.setPayload(projection);
        auditLog.setModifiedFields(modifiedFields);
        auditLog.setOperation(revisionType.toString());
        return auditLog;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(I objectId) throws ObjectNotFoundException, ObjectNotValidException {
        T objectDb = this.findById(objectId);
        objectDb.setDeleted(true);
        objectDb.setDeletionToken(UUID.randomUUID());
        this.getRepository().save(objectDb);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(objectDb, this.getRepository()));
        return buildIdResponse(objectId);
    }

    protected JsonNode buildIdResponse(I objectId) {
        ObjectNode response = this.getObjectMapper().createObjectNode();
        if (objectId instanceof Long)
            response.put("id", (Long) objectId);
        else
            response.put("id", objectId.toString());
        return response;
    }

    @Override
    public T validateReference(I objectId) throws ObjectNotFoundException {
        validateId(objectId);
        T objectResult = null;
        try {
            Constructor<T> constructor = modelType.getConstructor();
            objectResult = constructor.newInstance();
            objectResult.setId(objectId);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            log.error("validateId - args: Entity: {}, error: {}", modelType, ex.getMessage());
        }
        return objectResult;
    }

    private void validateId(I objectId) throws ObjectNotFoundException {
        boolean exists = repository.existsById(objectId);
        if (!exists)
            throw new ObjectNotFoundException(String.format(NOT_FOUND_TEMPLATE, objectId));
    }

    @Override
    public T mapDtoToInput(R dto) {
        return this.getObjectMapper().convertValue(dto, this.modelType);
    }

    @Override
    public void validate(T object) throws ObjectNotValidException, ObjectNotFoundException {
        //Not default method implemented
    }

    @Override
    public void validateUpdate(T object) throws ObjectNotValidException, ObjectNotFoundException {
        this.validate(object);
    }

}
