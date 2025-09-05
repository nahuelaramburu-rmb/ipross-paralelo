package com.capacidad.validationapi.config.multitenancy;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DELETION_FILTER;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.TENANT_FILTER;

@Log4j2
@Aspect
@Component
@Transactional
public class TenantAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Pointcut("execution(public !void org.springframework.data.repository.Repository+.*(..))")
    public void publicNonVoidRepositoryMethod() {
    }

    @Before("publicNonVoidRepositoryMethod()")
    public void beforeQuery(JoinPoint joinPoint) {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter(DELETION_FILTER);
        filter.setParameter("deleted", false);
        filter.validate();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Annotation methodAnnotation = method.getAnnotation(TenantFilter.class);
        if (methodAnnotation != null)
            this.processTenantFilter(methodAnnotation, session);
        else {
            for (Class<?> i : joinPoint.getTarget().getClass().getInterfaces()) {
                Annotation annotation = i.getAnnotation(TenantFilter.class);
                if (annotation != null)
                    this.processTenantFilter(annotation, session);
            }
        }
    }

    private void processTenantFilter(Annotation annotation, Session session) {
        if (((TenantFilter) annotation).active() && TenantContext.getTenant() != null) {
            Filter filter = session.enableFilter(TENANT_FILTER);
            filter.setParameter("tenantId", TenantContext.getTenant());
            filter.validate();
        } else
            session.disableFilter(TENANT_FILTER);
    }
}
