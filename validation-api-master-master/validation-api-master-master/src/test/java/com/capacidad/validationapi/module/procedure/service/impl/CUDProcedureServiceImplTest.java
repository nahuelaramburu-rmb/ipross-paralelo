package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.procedure.dto.CUDProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.hateoas.CUDProcedureResource;
import com.capacidad.validationapi.module.procedure.model.CUDProcedure;
import com.capacidad.validationapi.module.procedure.model.ProcedureResolution;
import com.capacidad.validationapi.module.procedure.projection.CUDProcedureProjection;
import com.capacidad.validationapi.module.procedure.repository.CUDProcedureRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.HashSet;

import static com.capacidad.validationapi.module.general.reference.StatusReference.PROCEDURE_APPROVED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PROCEDURE_REVISION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CUDProcedureServiceImplTest {

    @Mock
    private Utils utils;

    @Mock
    private CUDProcedureRepository cudProcedureRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private CUDProcedureServiceImpl cudProcedureService;

    @Test(expected = ObjectNotValidException.class)
    public void testResolveThrowsExceptionWhenResolutionIsApproveAndNullDiagnosis() throws ObjectNotValidException, ObjectNotFoundException {
        CUDProcedure cudProcedure = new CUDProcedure();

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        cudProcedure.setStatus(revision);

        CUDProcedureResolutionDTO resolutionDTO = new CUDProcedureResolutionDTO();
        resolutionDTO.setExpiration(LocalDate.now());
        resolutionDTO.setResolution(ProcedureResolution.APPROVE);
        resolutionDTO.setDiagnosis(null);

        doReturn(cudProcedure).when(cudProcedureService).findById(1L);

        cudProcedureService.resolve(1L, resolutionDTO);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testResolveThrowsExceptionWhenResolutionIsApproveAndEmptyDiagnosis() throws ObjectNotValidException, ObjectNotFoundException {
        CUDProcedure cudProcedure = new CUDProcedure();

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        cudProcedure.setStatus(revision);

        CUDProcedureResolutionDTO resolutionDTO = new CUDProcedureResolutionDTO();
        resolutionDTO.setExpiration(LocalDate.now());
        resolutionDTO.setResolution(ProcedureResolution.APPROVE);
        resolutionDTO.setDiagnosis(new HashSet<>());

        doReturn(cudProcedure).when(cudProcedureService).findById(1L);

        cudProcedureService.resolve(1L, resolutionDTO);
    }

    @Test
    public void testResolveExecuteSuccessfullyWhenResolutionIsApproveAndNotEmptyDiagnosis() throws ObjectNotValidException, ObjectNotFoundException {
        CUDProcedure cudProcedure = new CUDProcedure();

        Status revision = new Status();
        revision.setId(PROCEDURE_REVISION.getId());

        cudProcedure.setStatus(revision);

        IdDTO<Long> diseaseDTO = new IdDTO<>();
        diseaseDTO.setId(1L);

        CUDProcedureResolutionDTO resolutionDTO = new CUDProcedureResolutionDTO();
        resolutionDTO.setExpiration(LocalDate.now());
        resolutionDTO.setResolution(ProcedureResolution.APPROVE);
        resolutionDTO.setDiagnosis(new HashSet<>());
        resolutionDTO.getDiagnosis().add(diseaseDTO);

        Status approved = new Status();
        approved.setId(PROCEDURE_APPROVED.getId());

        ICD10Disease disease = new ICD10Disease();
        disease.setId(diseaseDTO.getId());

        doReturn(cudProcedure).when(cudProcedureService).findById(1L);
        when(cudProcedureRepository.save(cudProcedure)).thenReturn(cudProcedure);
        when(cudProcedureService.getUtils()).thenReturn(utils);
        when(utils.getEntityReference(Status.class, approved.getId())).thenReturn(approved);
        when(utils.getEntityReference(ICD10Disease.class, diseaseDTO.getId())).thenReturn(disease);
        doReturn(applicationEventPublisher).when(cudProcedureService).getApplicationEventPublisher();
        doNothing().when(applicationEventPublisher).publishEvent(any());

        CUDProcedureResource result = (CUDProcedureResource) cudProcedureService.resolve(1L, resolutionDTO);
        CUDProcedureProjection procedureProjection = (CUDProcedureProjection) result.getContent();

        assertThat(procedureProjection.getStatus().getId()).isEqualTo(approved.getId());
        assertThat(procedureProjection.getClosedAt()).isNotNull();
        assertThat(procedureProjection.getDiagnosis().size()).isEqualTo(1);
    }

}
