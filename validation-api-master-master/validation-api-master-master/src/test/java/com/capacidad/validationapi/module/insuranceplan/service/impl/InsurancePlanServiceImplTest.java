package com.capacidad.validationapi.module.insuranceplan.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.repository.InsurancePlanRepository;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InsurancePlanServiceImplTest {

    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private InsurancePlanServiceImpl insurancePlanService;

    @Test
    public void testDeleteThrowsExceptionWhenNotEmptyNonDeletedMedicalCoverages() throws ObjectNotFoundException {
        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        MedicalCoverage medicalCoverage = new MedicalCoverage();

        insurancePlan.getMedicalCoverages().add(medicalCoverage);

        doReturn(insurancePlan).when(insurancePlanService).findById(insurancePlan.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> insurancePlanService.delete(insurancePlan.getId()));

        assertThat(exception.getMessage()).isEqualTo("insurancePlan.removeMedicalCoveragesFirst");
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenEmptyCoverages() throws ObjectNotFoundException, ObjectNotValidException {
        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        testDeleteExecuteSuccessfully(insurancePlan);
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenNonEmptyDeletedCoverages() throws ObjectNotFoundException, ObjectNotValidException {
        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setId(1L);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setDeleted(true);

        insurancePlan.getMedicalCoverages().add(medicalCoverage);

        testDeleteExecuteSuccessfully(insurancePlan);
    }

    private void testDeleteExecuteSuccessfully(InsurancePlan insurancePlan) throws ObjectNotFoundException, ObjectNotValidException {
        BeneficiaryInsurancePlan beneficiaryInsurancePlan = new BeneficiaryInsurancePlan();
        insurancePlan.getBeneficiaryInsurancePlans().add(beneficiaryInsurancePlan);

        doReturn(insurancePlan).when(insurancePlanService).findById(insurancePlan.getId());
        doReturn(new ObjectMapper()).when(insurancePlanService).getObjectMapper();
        doReturn(applicationEventPublisher).when(insurancePlanService).getApplicationEventPublisher();

        JsonNode result = insurancePlanService.delete(insurancePlan.getId());

        assertThat(result.get("id").asLong()).isEqualTo(insurancePlan.getId());
        assertThat(insurancePlan.getDeleted()).isTrue();
        assertThat(insurancePlan.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(insurancePlan.getBeneficiaryInsurancePlans()).isEmpty();

        verify(insurancePlanRepository, Mockito.times(1)).save(insurancePlan);
        verify(applicationEventPublisher, Mockito.times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }
}
