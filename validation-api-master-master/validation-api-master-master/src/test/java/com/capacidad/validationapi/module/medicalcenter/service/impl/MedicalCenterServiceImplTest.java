package com.capacidad.validationapi.module.medicalcenter.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.contract.model.MedicalCenterContract;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.repository.MedicalCenterRepository;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalCenterServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MedicalCenterRepository medicalCenterRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private IdentityClientService identityClientService;

    @Spy
    @InjectMocks
    private MedicalCenterServiceImpl medicalCenterService;

    @Test
    public void testDeleteThrowsExceptionWhenNotDeletedAssociatedContract() throws ObjectNotFoundException {
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        medicalCenter.setContract(new MedicalCenterContract());

        doReturn(medicalCenter).when(medicalCenterService).findById(medicalCenter.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCenterService.delete(medicalCenter.getId()));

        assertThat(exception.getMessage()).isEqualTo("contractShouldBeDeletedFirst");
    }

    @Test
    public void testDeleteUpdateSuccessfullyWhenNotAssociatedContract() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);
        medicalCenter.setContract(null);

        testDeleteUpdateSuccessfully(medicalCenter);
    }

    @Test
    public void testDeleteUpdateSuccessfullyWhenDeletedAssociatedContract() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalCenter medicalCenter = new MedicalCenter();
        medicalCenter.setId(1L);

        MedicalCenterContract medicalCenterContract = new MedicalCenterContract();
        medicalCenterContract.setDeleted(true);
        medicalCenter.setContract(medicalCenterContract);

        testDeleteUpdateSuccessfully(medicalCenter);
    }

    private void testDeleteUpdateSuccessfully(MedicalCenter medicalCenter) throws ObjectNotValidException, ObjectNotFoundException {
        Address address = new Address();
        medicalCenter.setAddress(address);

        Practitioner practitioner = new Practitioner();
        practitioner.getMedicalCenters().add(medicalCenter);
        medicalCenter.getPractitioners().add(practitioner);

        BatchItem batchItem = new BatchItem();
        batchItem.getMedicalCenters().add(medicalCenter);
        medicalCenter.getBatchItems().add(batchItem);

        doReturn(medicalCenter).when(medicalCenterService).findById(medicalCenter.getId());
        doReturn(objectMapper).when(medicalCenterService).getObjectMapper();
        doReturn(applicationEventPublisher).when(medicalCenterService).getApplicationEventPublisher();

        JsonNode result = medicalCenterService.delete(medicalCenter.getId());

        assertThat(result.get("id").asLong()).isEqualTo(medicalCenter.getId());
        assertThat(medicalCenter.getDeleted()).isTrue();
        assertThat(medicalCenter.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(address.getDeleted()).isTrue();
        assertThat(address.getDeletionToken()).isEqualTo(medicalCenter.getDeletionToken());
        assertThat(practitioner.getMedicalCenters()).isEmpty();
        assertThat(batchItem.getMedicalCenters()).isEmpty();
        verify(medicalCenterRepository, times(1)).save(medicalCenter);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
        verify(identityClientService, times(1)).deleteResourceIdAccounts(medicalCenter.getResourceId());
    }
}
