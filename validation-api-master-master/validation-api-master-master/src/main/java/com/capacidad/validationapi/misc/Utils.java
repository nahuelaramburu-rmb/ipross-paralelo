package com.capacidad.validationapi.misc;

import com.capacidad.validationapi.module.base.controller.BaseController;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.model.DTOType;
import com.capacidad.validationapi.module.base.model.ProjectionType;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;

@Log4j2
@Component
public class Utils {

    private static final String MODULE_PACKAGE_NAME = "com.capacidad.validationapi.module";

    private static final String HAL_PACKAGE = "hateoas";
    private static final String RESOURCE_PREFIX = "Resource";

    private static final String PROJECTION_PACKAGE = "projection";
    private static final String PROJECTION_PREFIX = "Projection";
    private static final String AUDIT_LOG_PROJECTION_PREFIX = "AuditLogProjection";

    private static final String DTO_PACKAGE = "dto";
    private static final String UPDATE_DTO_PREFIX = "UpdateDTO";
    private static final String DTO_PREFIX = "DTO";

    private static final String CONTROLLER_PACKAGE = "controller";
    private static final String CONTROLLER_PREFIX = "Controller";

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public static <P extends BaseProjection<I>, I extends Serializable, T extends BaseEntity<I>> EntityModel<P> projectionToResourceMapping(Class<T> modelClass, Object source) {
        String resourceClassName = StringUtils.join(MODULE_PACKAGE_NAME, DOT, parsePackageModuleName(modelClass.getName()), DOT, HAL_PACKAGE, DOT, modelClass.getSimpleName(), RESOURCE_PREFIX);
        EntityModel<P> resource = null;
        try {
            Class<EntityModel<P>> resourceSupportClass = (Class<EntityModel<P>>) Class.forName(resourceClassName);
            Constructor<EntityModel<P>> constructor = resourceSupportClass.getConstructor(source.getClass().getInterfaces()[0]);
            resource = constructor.newInstance(source);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            log.debug("Object Resource not implemented - modelClass:({})", modelClass);
        }
        log.debug("projectionToResourceMapping - args: modelClass:({}), source:({}) - return: ({})", modelClass, resourceClassName, resource);
        return resource;
    }

    @SuppressWarnings("unchecked")
    public static <P extends BaseProjection<I>, I extends Serializable, T extends BaseEntity<I>> Class<P> getEntityProjectionClass(Class<T> modelClass, ProjectionType projectionType) {
        String projectionClassName = projectionType == ProjectionType.ENTITY ? StringUtils.join(MODULE_PACKAGE_NAME, DOT, parsePackageModuleName(modelClass.getName()), DOT, PROJECTION_PACKAGE, DOT, modelClass.getSimpleName(), PROJECTION_PREFIX)
                : StringUtils.join(MODULE_PACKAGE_NAME, DOT, parsePackageModuleName(modelClass.getName()), DOT, PROJECTION_PACKAGE, DOT, modelClass.getSimpleName(), AUDIT_LOG_PROJECTION_PREFIX);
        Class<P> projectionClass = null;
        try {
            projectionClass = (Class<P>) Class.forName(projectionClassName);
        } catch (ClassNotFoundException e) {
            log.debug("Object Projection ({}) not implemented - modelClass:({})", modelClass, projectionType.toString());
        }
        log.debug("getEntityProjectionClass - args modelClass:({}) - return: ({})", modelClass, projectionClass);
        return projectionClass;
    }

    @SuppressWarnings("unchecked")
    public static <I extends Serializable, D extends BaseDTO<I>, T extends BaseEntity<I>, C extends BaseController<D, I>> Class<C> getEntityControllerClass(Class<T> modelClass) {
        String controllerClassName = StringUtils.join(MODULE_PACKAGE_NAME, DOT, parsePackageModuleName(modelClass.getName()), DOT, CONTROLLER_PACKAGE, DOT, modelClass.getSimpleName(), CONTROLLER_PREFIX);
        Class<C> controllerClass = null;
        try {
            controllerClass = (Class<C>) Class.forName(controllerClassName);
        } catch (ClassNotFoundException e) {
            log.debug("Object Controller not implemented - modelClass:({})", modelClass);
        }
        log.debug("getEntityControllerClass - args modelClass:({}), return: ({})", modelClass, controllerClass);
        return controllerClass;
    }

    @SuppressWarnings("unchecked")
    public static <I extends Serializable, D extends BaseDTO<I>, T extends BaseEntity<I>> Class<D> getEntityDto(Class<T> modelClass, DTOType dtoType) {
        String dtoClassName = dtoType == DTOType.CREATION ? StringUtils.join(MODULE_PACKAGE_NAME, DOT, parsePackageModuleName(modelClass.getName()), DOT, DTO_PACKAGE, DOT, modelClass.getSimpleName(), DTO_PREFIX)
                : StringUtils.join(MODULE_PACKAGE_NAME, DOT, parsePackageModuleName(modelClass.getName()), DOT, DTO_PACKAGE, DOT, modelClass.getSimpleName(), UPDATE_DTO_PREFIX);
        Class<D> dtoClass = null;
        try {
            dtoClass = (Class<D>) Class.forName(dtoClassName);
        } catch (ClassNotFoundException e) {
            log.debug("Object DTO not implemented ({}) - modelClass:({})", dtoType.toString(), modelClass);
        }
        log.debug("getEntityDto ({}) - args modelClass:({}), return: ({})", dtoType.toString(), modelClass, dtoClass);
        return dtoClass;
    }

    private static String parsePackageModuleName(String name) {
        String packageName = "";
        Pattern pattern = Pattern.compile("(?<=.module.)(.*)(?=.model.)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())
            packageName = matcher.group(0);
        return packageName;
    }

    public static String buildCompoundSearchString(String innerSearch, String search) {
        return StringUtils.isBlank(search) ?
                innerSearch :
                StringUtils.join(search, COMA, innerSearch);
    }

    public static String readResourceAsString(String filename) {
        try {
            Resource resource = readResourceFromPattern(filename).orElseThrow(() -> new IOException("readResourceAndReturnFile returned empty"));
            return readResourceAsString(resource);
        } catch (IOException e) {
            log.error("({}) - readResourceAndReturnString: {}", Utils.class, e.getMessage());
        }
        return "";
    }

    public static String readResourceAsString(Resource resource) {
        try (InputStream resourceStream = resource.getInputStream()) {
            return IOUtils.toString(resourceStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("({}) - readResourceAsString: resource: {}, error: {}", Utils.class, resource, e.getMessage());
        }
        return "";
    }

    public static Optional<Resource> readResourceFromPattern(String pattern) {
        try {
            ClassLoader classLoader = Utils.class.getClassLoader();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
            return Optional.of(resolver.getResource(StringUtils.join(ResourceUtils.CLASSPATH_URL_PREFIX, pattern)));
        } catch (Exception e) {
            log.error("({}) - readResourceFromPattern: filename: {}, error: {}", Utils.class, pattern, e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<Resource[]> readResourcesFromPattern(String pattern) {
        try {
            ClassLoader classLoader = Utils.class.getClassLoader();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
            return Optional.of(resolver.getResources(StringUtils.join(ResourceUtils.CLASSPATH_URL_PREFIX, pattern)));
        } catch (Exception e) {
            log.error("({}) - readResourcesFromPattern: filename: {}, error: {}", Utils.class, pattern, e.getMessage());
        }
        return Optional.empty();
    }

    public JSONObject readFileToJsonObject(String filename) {
        JSONObject result = null;
        try (InputStream resourceStream = readResource(filename)) {
            String content = IOUtils.toString(resourceStream, StandardCharsets.UTF_8);
            result = new JSONObject(content);
        } catch (IOException e) {
            log.error("Could not read File '{}' to Json Object: {}", filename, e.getMessage());
        }
        return result;
    }

    public AuditReader getAuditReader() {
        return AuditReaderFactory.get(entityManager);
    }

    private InputStream readResource(String resourceName) throws IOException {
        return ResourceUtils.getURL(StringUtils.join("classpath:", resourceName)).openStream();
    }

    @Deprecated
    public <T extends BaseEntity<I>, I extends Serializable> T getEntityReference(Class<T> entityClass, Long primaryKey) {
        return entityManager.getReference(entityClass, primaryKey);
    }

    public <T extends BaseEntity<I>, I extends Serializable> T getGenericsEntityReference(Class<T> entityClass, I primaryKey) {
        return entityManager.getReference(entityClass, primaryKey);
    }

}
