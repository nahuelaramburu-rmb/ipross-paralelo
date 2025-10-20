package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryCategoryService;
import com.capacidad.validationapi.module.company.service.CompanyService;
import com.capacidad.validationapi.module.insuranceplan.service.InsurancePlanService;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Province;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.util.Map;

import static com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryImportConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryImportPropertiesInitializerTest {

    @Mock
    private InsurancePlanService insurancePlanService;

    @Mock
    private BeneficiaryCategoryService beneficiaryCategoryService;

    @Mock
    private PropertiesService propertiesService;

    @Mock
    private CompanyService companyService;

    @Mock
    private EntityManager entityManager;

    @Spy
    @InjectMocks
    private BeneficiaryImportPropertiesInitializer importPropertiesInitializer;

    @Test
    public void testInitializeFillsProperties() {
        Province province = new Province();
        province.setName("province");
        City city = new City();
        city.setName("city");
        province.getCities().add(city);

        when(importPropertiesInitializer.getProcessingEntityManager()).thenReturn(entityManager);
        when(importPropertiesInitializer.getProcessingEntityManager()).thenReturn(entityManager);
        when(propertiesService.getPropertiesTypedQuery(entityManager)).thenReturn(new Properties());

        Map<String, Object> properties = importPropertiesInitializer.initializeProperties();

        assertThat(properties.get(CITIES_KEY)).isNotNull();
        assertThat(properties.get(ID_TYPES_KEY)).isNotNull();
        assertThat(properties.get(MARITAL_STATUSES_KEY)).isNotNull();
        assertThat(properties.get(RELATIONSHIP_TYPES_KEY)).isNotNull();

        verify(insurancePlanService, times(1)).findAllInsurancePlansTypedQuery(entityManager);
        verify(beneficiaryCategoryService, times(1)).findAllBeneficiaryCategoriesTypedQuery(entityManager);
        verify(propertiesService, times(1)).getPropertiesTypedQuery(entityManager);
        verify(companyService, times(1)).findAllCompaniesTypedQuery(entityManager);
    }

}
