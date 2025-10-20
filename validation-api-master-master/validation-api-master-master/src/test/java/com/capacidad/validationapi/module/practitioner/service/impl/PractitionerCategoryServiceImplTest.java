package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.model.PractitionerCategory;
import com.capacidad.validationapi.module.practitioner.repository.PractitionerCategoryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerCategoryServiceImplTest {

    @Mock
    private PractitionerCategoryRepository practitionerCategoryRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private PractitionerCategoryServiceImpl practitionerCategoryService;


    @Test
    public void testDeleteThrowsExceptionWhenContractItemsAttached() throws ObjectNotFoundException {
        PractitionerCategory practitionerCategory = new PractitionerCategory();
        practitionerCategory.setId(1L);
        practitionerCategory.getContractItems().add(new ContractItem());

        doReturn(practitionerCategory).when(practitionerCategoryService).findById(practitionerCategory.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> practitionerCategoryService.delete(practitionerCategory.getId()));

        assertThat(exception.getMessage()).isEqualTo("practitionerCategory.cannotDeleteContractItemsAttached");
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenNoContractsAttached() throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerCategory practitionerCategory = new PractitionerCategory();
        practitionerCategory.setId(1L);

        Practitioner practitioner = new Practitioner();
        practitioner.setPractitionerCategory(practitionerCategory);

        practitionerCategory.getPractitioners().add(practitioner);

        testDeleteExecuteSuccessfully(practitionerCategory);
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenDeletedContractsAttached() throws ObjectNotFoundException, ObjectNotValidException {
        PractitionerCategory practitionerCategory = new PractitionerCategory();
        practitionerCategory.setId(1L);

        ContractItem contractItem = new ContractItem();
        contractItem.setDeleted(true);

        practitionerCategory.getContractItems().add(contractItem);

        Practitioner practitioner = new Practitioner();
        practitioner.setPractitionerCategory(practitionerCategory);

        practitionerCategory.getPractitioners().add(practitioner);

        testDeleteExecuteSuccessfully(practitionerCategory);
    }

    private void testDeleteExecuteSuccessfully(PractitionerCategory practitionerCategory) throws ObjectNotValidException, ObjectNotFoundException {
        doReturn(practitionerCategory).when(practitionerCategoryService).findById(practitionerCategory.getId());
        doReturn(applicationEventPublisher).when(practitionerCategoryService).getApplicationEventPublisher();
        doReturn(new ObjectMapper()).when(practitionerCategoryService).getObjectMapper();

        JsonNode result = practitionerCategoryService.delete(practitionerCategory.getId());

        assertThat(result.get("id").asLong()).isEqualTo(practitionerCategory.getId());
        assertThat(practitionerCategory.getDeleted()).isTrue();
        assertThat(practitionerCategory.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(practitionerCategory.getPractitioners().iterator().next().getPractitionerCategory()).isNull();
        verify(practitionerCategoryRepository, times(1)).save(practitionerCategory);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

}
