package com.capacidad.validationapi.module.nomenclator.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.repository.MedicalPracticeRepository;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MedicalPracticeServiceImplTest {

    @Mock
    private MedicalPracticeRepository medicalPracticeRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private MedicalPracticeServiceImpl medicalPracticeService;

    @Test
    public void testGetMedicalPracticeThrowsObjectNotFoundException() {
        when(medicalPracticeRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> medicalPracticeService.getMedicalPractice(1L));

        assertThat(exception.getMessage()).isEqualTo("medicalPractice.notFound");
    }

    @Test
    public void testDeleteThrowsExceptionWhenNonDeletedNomenclatorAttached() throws ObjectNotFoundException {
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setId(1L);

        Nomenclator nomenclator = new Nomenclator();

        medicalPractice.setNomenclators(Collections.singleton(nomenclator));

        doReturn(medicalPractice).when(medicalPracticeService).findById(medicalPractice.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalPracticeService.delete(medicalPractice.getId()));

        assertThat(exception.getMessage()).isEqualTo("medicalPractice.cannotDeleteNomenclatorAttached");
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenNoNomenclatorAttached() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setId(1L);

        medicalPractice.getMedicalSpecialties().add(new MedicalSpecialty());

        testDeleteExecuteSuccessfully(medicalPractice);
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenDeletedNomenclatorAttached() throws ObjectNotFoundException, ObjectNotValidException {
        MedicalPractice medicalPractice = new MedicalPractice();
        medicalPractice.setId(1L);

        medicalPractice.getMedicalSpecialties().add(new MedicalSpecialty());

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setDeleted(true);

        medicalPractice.setNomenclators(Collections.singleton(nomenclator));

        testDeleteExecuteSuccessfully(medicalPractice);
    }

    private void testDeleteExecuteSuccessfully(MedicalPractice medicalPractice) throws ObjectNotValidException, ObjectNotFoundException {
        doReturn(medicalPractice).when(medicalPracticeService).findById(medicalPractice.getId());
        doReturn(applicationEventPublisher).when(medicalPracticeService).getApplicationEventPublisher();
        doReturn(new ObjectMapper()).when(medicalPracticeService).getObjectMapper();

        JsonNode result = medicalPracticeService.delete(medicalPractice.getId());

        assertThat(result.get("id").asLong()).isEqualTo(medicalPractice.getId());
        assertThat(medicalPractice.getDeleted()).isTrue();
        assertThat(medicalPractice.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(medicalPractice.getMedicalSpecialties()).isEmpty();
        verify(medicalPracticeRepository, times(1)).save(medicalPractice);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

}
