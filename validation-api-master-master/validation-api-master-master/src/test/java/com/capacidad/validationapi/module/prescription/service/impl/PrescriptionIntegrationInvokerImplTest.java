package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.prescription.integration.DefaultPrescriptionServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrescriptionIntegrationInvokerImplTest {

    @Mock
    private PropertiesService propertiesService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private DefaultPrescriptionServiceImpl defaultPrescriptionService;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private PrescriptionIntegrationInvokerImpl prescriptionIntegrationInvoker;

    @Test
    public void testInvokeCreationCollectionDoNotExecutesWhenSyncDisabled() throws ObjectNotValidException, ObjectNotFoundException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(false);

        prescriptionIntegrationInvoker.invokeCreation(Collections.singleton(new Prescription()));

        verify(applicationContext, never()).getBean(anyString());
    }

    @Test
    public void testInvokeCreationCollectionExecutesWhenSyncEnabled() throws ObjectNotValidException, ObjectNotFoundException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(true);

        Properties properties = new Properties();
        properties.setPrescriptionService("defaultPrescriptionServiceImpl");

        when(propertiesService.getProperties()).thenReturn(properties);
        when(applicationContext.getBean(properties.getPrescriptionService())).thenReturn(defaultPrescriptionService);

        Prescription prescription = new Prescription();
        Collection<Prescription> prescriptions = Collections.singleton(prescription);

        prescriptionIntegrationInvoker.invokeCreation(prescriptions);

        verify(defaultPrescriptionService, times(1)).savePrescriptions(prescriptions);
        assertThat(prescription.getDType()).hasToString(properties.getPrescriptionService());
    }

    @Test
    public void testInvokeCreationDoNotExecutesWhenSyncDisabled() throws ObjectNotValidException, ObjectNotFoundException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(false);

        prescriptionIntegrationInvoker.invokeCreation(new Prescription());

        verify(applicationContext, never()).getBean(anyString());
    }

    @Test
    public void testInvokeCreationExecutesWhenSyncEnabled() throws ObjectNotValidException, ObjectNotFoundException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(true);

        Properties properties = new Properties();
        properties.setPrescriptionService("defaultPrescriptionServiceImpl");

        when(propertiesService.getProperties()).thenReturn(properties);
        when(applicationContext.getBean(properties.getPrescriptionService())).thenReturn(defaultPrescriptionService);

        Prescription prescription = new Prescription();

        prescriptionIntegrationInvoker.invokeCreation(prescription);

        verify(defaultPrescriptionService, times(1)).savePrescription(prescription);
        assertThat(prescription.getDType()).hasToString(properties.getPrescriptionService());
    }

    @Test
    public void testInvokeCancellationDoNotExecutesWhenSyncDisabled() throws ObjectNotValidException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(false);

        prescriptionIntegrationInvoker.invokeCancellation(new Prescription());

        verify(applicationContext, never()).getBean(anyString());
    }

    @Test
    public void testInvokeCancellationExecutesWhenSyncEnabled() throws ObjectNotValidException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(true);

        Properties properties = new Properties();
        properties.setPrescriptionService("defaultPrescriptionServiceImpl");

        when(propertiesService.getProperties()).thenReturn(properties);
        when(applicationContext.getBean(properties.getPrescriptionService())).thenReturn(defaultPrescriptionService);

        Prescription prescription = new Prescription();

        prescriptionIntegrationInvoker.invokeCancellation(prescription);

        verify(defaultPrescriptionService, times(1)).cancelPrescription(prescription);
    }

    @Test
    public void testInvokeStatusSynchronizationDoNotExecutesWhenSyncDisabled() throws ObjectNotValidException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(false);

        prescriptionIntegrationInvoker
                .invokeStatusSynchronization("defaultPrescriptionServiceImpl",
                        Collections.singleton(new Prescription()),
                        new Status());

        verify(applicationContext, never()).getBean(anyString());
    }

    @Test
    public void testInvokeStatusSynchronizationExecutesWhenSyncEnabled() throws ObjectNotValidException {
        when(propertiesService.getApplicationProperties()).thenReturn(applicationProperties);
        when(applicationProperties.getPrescriptionSynchronizationEnabled()).thenReturn(true);

        when(applicationContext.getBean("defaultPrescriptionServiceImpl")).thenReturn(defaultPrescriptionService);

        Collection<Prescription> prescriptions = Collections.singleton(new Prescription());
        Status status = new Status();

        prescriptionIntegrationInvoker
                .invokeStatusSynchronization("defaultPrescriptionServiceImpl",
                        prescriptions,
                        status);

        verify(defaultPrescriptionService, times(1)).syncPrescriptionsStatus(prescriptions, status);
    }

    @Test
    public void testInvokeFindPrescriptionExecutesWhenSyncEnabled() throws ObjectNotValidException {
        Properties properties = new Properties();
        properties.setPrescriptionService("defaultPrescriptionServiceImpl");

        when(propertiesService.getProperties()).thenReturn(properties);
        when(applicationContext.getBean(properties.getPrescriptionService())).thenReturn(defaultPrescriptionService);

        Beneficiary beneficiary = new Beneficiary();
        String exchangeId = "1231231";

        prescriptionIntegrationInvoker
                .invokeFindPrescription(beneficiary, exchangeId);

        verify(defaultPrescriptionService, times(1))
                .findBeneficiaryValidatedPrescription(beneficiary, exchangeId);
    }

}
