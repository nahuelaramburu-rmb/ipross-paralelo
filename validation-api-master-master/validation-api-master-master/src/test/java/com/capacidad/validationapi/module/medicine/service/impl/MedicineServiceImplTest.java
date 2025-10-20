package com.capacidad.validationapi.module.medicine.service.impl;

import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.prescription.integration.DefaultPrescriptionServiceImpl;
import com.capacidad.validationapi.prescription.integration.ProductIntegrationProjection;
import com.capacidad.validationapi.prescription.integration.ProductWrapperDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MedicineServiceImplTest {

    @Mock
    private PropertiesService propertiesService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private DefaultPrescriptionServiceImpl defaultPrescriptionService;

    @InjectMocks
    private MedicineServiceImpl medicineService;

    @Test
    public void testGetProductsReturnValidProductWrapperDTOWhenValidPrescriptionService() {
        Properties properties = new Properties();
        properties.setPrescriptionService("defaultPrescriptionServiceImpl");
        when(propertiesService.getProperties()).thenReturn(properties);

        when(applicationContext.getBean(properties.getPrescriptionService())).thenReturn(defaultPrescriptionService);

        ProductWrapperDTO expected = new ProductWrapperDTO();
        expected.setPlanId(1);
        expected.setProducts(Collections.singleton(mock(ProductIntegrationProjection.class)));

        String productName = "name";
        String beneficiaryCode = "beneficiaryCode";

        when(defaultPrescriptionService.getProducts(productName, beneficiaryCode)).thenReturn(expected);

        ProductWrapperDTO result = medicineService.getProducts(productName, beneficiaryCode);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetProductsReturnEmptyProductWrapperDTOWhenInvalidPrescriptionService() {
        Properties properties = new Properties();
        properties.setPrescriptionService("defaultPrescriptionServiceImpl");
        when(propertiesService.getProperties()).thenReturn(properties);

        when(applicationContext.getBean(properties.getPrescriptionService())).thenReturn(defaultPrescriptionService);

        ProductWrapperDTO expected = new ProductWrapperDTO();
        expected.setPlanId(1);
        expected.setProducts(Collections.singleton(mock(ProductIntegrationProjection.class)));

        String productName = "name";
        String beneficiaryCode = "beneficiaryCode";

        when(defaultPrescriptionService.getProducts(productName, beneficiaryCode)).thenThrow(new NoSuchBeanDefinitionException(""));

        ProductWrapperDTO result = medicineService.getProducts(productName, beneficiaryCode);

        assertThat(result.getPlanId()).isEqualTo(0);
        assertThat(result.getProducts()).isEmpty();
    }

}
