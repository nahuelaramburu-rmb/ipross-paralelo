package com.capacidad.validationapi.module.medicine.service.impl;

import com.capacidad.validationapi.module.medicine.service.MedicineService;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.prescription.integration.MedicinesIntegration;
import com.capacidad.validationapi.prescription.integration.ProductWrapperDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

@Log4j2
@Service
public class MedicineServiceImpl implements MedicineService {

    private final ApplicationContext applicationContext;
    private final PropertiesService propertiesService;

    public MedicineServiceImpl(ApplicationContext applicationContext,
                               PropertiesService propertiesService) {
        this.applicationContext = applicationContext;
        this.propertiesService = propertiesService;
    }


    @SuppressWarnings("unchecked")
    @Override
    public ProductWrapperDTO getProducts(String name, String beneficiaryCode) {
        try {
            Object prescriptionService = applicationContext.getBean(propertiesService.getProperties().getPrescriptionService());
            Method method = prescriptionService.getClass().getMethod(MedicinesIntegration.class.getDeclaredMethod("getProducts", String.class, String.class).getName(),
                    String.class,
                    String.class);
            return (ProductWrapperDTO) method.invoke(prescriptionService, name, beneficiaryCode);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | BeansException e) {
            log.error("Error trying to execute getProducts MedicineIntegration Method: {}", e.getMessage());
        }
        return new ProductWrapperDTO(Collections.emptySet(), 0);
    }

}
