package com.capacidad.validationapi.module.base.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.BaseController;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.EntityModel;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class ResourceAssembler<P extends BaseProjection<I>, I extends Serializable> {

    private final Class<? extends BaseController<? extends BaseDTO<I>, I>> controllerClass;
    private Class<? extends EntityModel<P>> resourceType;

    public <D extends BaseDTO<I>> ResourceAssembler(Class<? extends BaseController<? extends BaseDTO<I>, I>> controllerClass, Class<? extends EntityModel<P>> resourceType) {
        this.resourceType = resourceType;
        this.controllerClass = controllerClass;
    }

    public <D extends BaseDTO<I>> ResourceAssembler(Class<? extends BaseController<? extends BaseDTO<I>, I>> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public List<EntityModel<P>> toResources(Collection<P> content) {
        return content.stream()
                .map(this::toResource)
                .collect(Collectors.toUnmodifiableList());
    }

    @SuppressWarnings("unchecked")
    public EntityModel<P> toResource(P entity) {
        try {
            if (resourceType == null)
                return toSimpleResource(entity);
            Constructor<EntityModel<P>> constructor = (Constructor<EntityModel<P>>) resourceType.getConstructor(entity.getClass().getInterfaces()[0]);
            return constructor.newInstance(entity);
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException | ObjectNotValidException | ObjectNotFoundException e) {
            log.error("Object Resource ({}) could not be created: {}", resourceType, e.getMessage());
        }
        return null;
    }

    private EntityModel<P> toSimpleResource(P entity) throws ObjectNotValidException, ObjectNotFoundException {
        return new SelfModel<>(entity, controllerClass);
    }
}
