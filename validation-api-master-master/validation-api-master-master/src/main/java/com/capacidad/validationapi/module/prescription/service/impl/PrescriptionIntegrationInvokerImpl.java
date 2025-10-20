package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.service.PrescriptionIntegrationInvoker;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.prescription.integration.PrescriptionIntegration;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

@Log4j2
@Service
public class PrescriptionIntegrationInvokerImpl implements PrescriptionIntegrationInvoker {

    private static final String PRESCRIPTION_INTEGRATION_ERROR_CODE = "prescription.integrationError";
    private static final String PRESCRIPTION_INTEGRATION_ERROR_LOG_MESSAGE = "Error trying to execute PrescriptionIntegration Method {}. Error: {}";
    private final ApplicationContext applicationContext;
    private final PropertiesService propertiesService;

    @Autowired
    public PrescriptionIntegrationInvokerImpl(ApplicationContext applicationContext,
                                              PropertiesService propertiesService) {
        this.applicationContext = applicationContext;
        this.propertiesService = propertiesService;
    }

    @Override
    public void invokeCreation(Collection<Prescription> prescriptions) throws ObjectNotValidException, ObjectNotFoundException {
        if (isSyncEnabled()) {
            String serviceName = getPrescriptionPropertiesServiceName();
            Object prescriptionService = getPrescriptionServiceBean(serviceName);
            prescriptions.forEach(p -> p.setDType(serviceName));
            findAndInvokePrescriptionIntegrationServiceRuntime(prescriptionService, "savePrescriptions", new Class<?>[]{Collection.class}, new Object[]{prescriptions});
        }
    }

    private boolean isSyncEnabled() {
        return Boolean.TRUE.equals(propertiesService.getApplicationProperties().getPrescriptionSynchronizationEnabled());
    }

    private String getPrescriptionPropertiesServiceName() {
        return propertiesService.getProperties().getPrescriptionService();
    }

    private Object getPrescriptionServiceBean(String name) {
        return applicationContext.getBean(name);
    }

    private void findAndInvokePrescriptionIntegrationServiceRuntime(Object service, String methodName, Class<?>[] parameterTypes, Object[] args) throws ObjectNotValidException, ObjectNotFoundException {
        try {
            findAndInvoke(service, methodName, parameterTypes, args);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | BeansException | UnexpectedRollbackException e) {
            log.error(PRESCRIPTION_INTEGRATION_ERROR_LOG_MESSAGE, methodName, e.getCause().getMessage());
            throwIfExpected(e);
            throw new ObjectNotValidException(PRESCRIPTION_INTEGRATION_ERROR_CODE);
        }
    }

    private Object findAndInvoke(Object service, String methodName, Class<?>[] parameterTypes, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = service.getClass().getMethod(PrescriptionIntegration.class.getDeclaredMethod(methodName, parameterTypes).getName(), parameterTypes);
        return method.invoke(service, args);
    }

    private void throwIfExpected(Throwable expected) throws ObjectNotValidException, ObjectNotFoundException {
        Throwable cause = expected.getCause();
        if (cause instanceof ObjectNotValidException)
            throw (ObjectNotValidException) cause;
        if (cause instanceof ObjectNotFoundException)
            throw (ObjectNotFoundException) cause;
    }

    @Override
    public void invokeCreation(Prescription prescription) throws ObjectNotValidException, ObjectNotFoundException {
        if (isSyncEnabled()) {
            String serviceName = getPrescriptionPropertiesServiceName();
            Object prescriptionService = getPrescriptionServiceBean(serviceName);
            prescription.setDType(serviceName);
            findAndInvokePrescriptionIntegrationServiceRuntime(prescriptionService, "savePrescription", new Class<?>[]{Prescription.class}, new Object[]{prescription});
        }
    }

    @Override
    public void invokeCancellation(Prescription prescription) throws ObjectNotValidException {
        if (isSyncEnabled()) {
            String serviceName = getPrescriptionPropertiesServiceName();
            Object prescriptionService = getPrescriptionServiceBean(serviceName);
            findAndInvokePrescriptionIntegrationService(prescriptionService, "cancelPrescription", new Class<?>[]{Prescription.class}, new Object[]{prescription});
        }
    }

    @Override
    public void invokeStatusSynchronization(String serviceName, Collection<Prescription> prescriptions, Status newStatus) throws ObjectNotValidException {
        if (isSyncEnabled()) {
            Object prescriptionService = getPrescriptionServiceBean(serviceName);
            findAndInvokePrescriptionIntegrationService(prescriptionService, "syncPrescriptionsStatus", new Class<?>[]{Collection.class, Status.class}, new Object[]{prescriptions, newStatus});
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Prescription> invokeFindPrescription(Beneficiary beneficiary, String exchangeId) throws ObjectNotValidException {
        String serviceName = getPrescriptionPropertiesServiceName();
        Object prescriptionService = getPrescriptionServiceBean(serviceName);
        return (Optional<Prescription>) findAndInvokePrescriptionIntegrationService(prescriptionService,
                "findBeneficiaryValidatedPrescription",
                new Class<?>[]{Beneficiary.class, String.class},
                new Object[]{beneficiary, exchangeId});
    }

    private Object findAndInvokePrescriptionIntegrationService(Object service, String methodName, Class<?>[] parameterTypes, Object[] args) throws ObjectNotValidException {
        try {
            return findAndInvoke(service, methodName, parameterTypes, args);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | BeansException | UnexpectedRollbackException e) {
            log.error(PRESCRIPTION_INTEGRATION_ERROR_LOG_MESSAGE, methodName, e.getCause().getMessage());
            throw new ObjectNotValidException(PRESCRIPTION_INTEGRATION_ERROR_CODE);
        }
    }

}
