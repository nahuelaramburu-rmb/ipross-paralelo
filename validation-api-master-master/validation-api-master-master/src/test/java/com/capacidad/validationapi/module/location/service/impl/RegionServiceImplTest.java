package com.capacidad.validationapi.module.location.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.repository.RegionRepository;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.organization.model.Organization;
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
public class RegionServiceImplTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private RegionServiceImpl regionService;

    @Test
    public void testDeleteThrowsExceptionWhenNonDeletedAuditTraysAttached() throws ObjectNotFoundException {
        Region region = new Region();
        region.setId(1L);

        AuditTray auditTray = new AuditTray();
        region.getAuditTrays().add(auditTray);

        doReturn(region).when(regionService).findById(region.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> regionService.delete(region.getId()));

        assertThat(exception.getMessage()).isEqualTo("region.cannotDeleteAuditTraysAttached");
    }

    @Test
    public void testDeleteThrowsExceptionWhenNonDeletedContractAdjustmentsAttached() throws ObjectNotFoundException {
        Region region = new Region();
        region.setId(1L);

        AuditTray auditTray = new AuditTray();
        auditTray.setDeleted(true);
        region.getAuditTrays().add(auditTray);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        region.getContractAdjustments().add(contractAdjustment);

        doReturn(region).when(regionService).findById(region.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> regionService.delete(region.getId()));

        assertThat(exception.getMessage()).isEqualTo("region.cannotDeleteContractAdjustmentsAttached");
    }

    @Test
    public void testDeleteThrowsExceptionWhenNonDeletedMedicalCoveragesAttached() throws ObjectNotFoundException {
        Region region = new Region();
        region.setId(1L);

        AuditTray auditTray = new AuditTray();
        auditTray.setDeleted(true);
        region.getAuditTrays().add(auditTray);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setDeleted(true);
        region.getContractAdjustments().add(contractAdjustment);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        region.getMedicalCoverages().add(medicalCoverage);

        doReturn(region).when(regionService).findById(region.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> regionService.delete(region.getId()));

        assertThat(exception.getMessage()).isEqualTo("region.cannotDeleteMedicalCoveragesAttached");
    }

    @Test
    public void testDeleteThrowsExceptionWhenNonDeletedOrganizationsAttached() throws ObjectNotFoundException {
        Region region = new Region();
        region.setId(1L);

        AuditTray auditTray = new AuditTray();
        auditTray.setDeleted(true);
        region.getAuditTrays().add(auditTray);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setDeleted(true);
        region.getContractAdjustments().add(contractAdjustment);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setDeleted(true);
        region.getMedicalCoverages().add(medicalCoverage);

        Organization organization = new Organization();
        region.getOrganizations().add(organization);

        doReturn(region).when(regionService).findById(region.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> regionService.delete(region.getId()));

        assertThat(exception.getMessage()).isEqualTo("region.cannotDeleteOrganizationsAttached");
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenDeletedEntitiesAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Region region = new Region();
        region.setId(1L);

        AuditTray auditTray = new AuditTray();
        auditTray.setDeleted(true);
        region.getAuditTrays().add(auditTray);

        ContractAdjustment contractAdjustment = new ContractAdjustment();
        contractAdjustment.setDeleted(true);
        region.getContractAdjustments().add(contractAdjustment);

        MedicalCoverage medicalCoverage = new MedicalCoverage();
        medicalCoverage.setDeleted(true);
        region.getMedicalCoverages().add(medicalCoverage);

        Organization organization = new Organization();
        organization.setDeleted(true);
        region.getOrganizations().add(organization);

        doReturn(region).when(regionService).findById(region.getId());
        doReturn(applicationEventPublisher).when(regionService).getApplicationEventPublisher();
        doReturn(new ObjectMapper()).when(regionService).getObjectMapper();

        JsonNode result = regionService.delete(region.getId());

        assertThat(result.get("id").asLong()).isEqualTo(region.getId());
        assertThat(region.getDeleted()).isTrue();
        assertThat(region.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        verify(regionRepository, times(1)).save(region);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

    @Test
    public void testDeleteExecuteSuccessfullyWhenNonEntitiesAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Region region = new Region();
        region.setId(1L);

        doReturn(region).when(regionService).findById(region.getId());
        doReturn(applicationEventPublisher).when(regionService).getApplicationEventPublisher();
        doReturn(new ObjectMapper()).when(regionService).getObjectMapper();

        JsonNode result = regionService.delete(region.getId());

        assertThat(result.get("id").asLong()).isEqualTo(region.getId());
        assertThat(region.getDeleted()).isTrue();
        assertThat(region.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        verify(regionRepository, times(1)).save(region);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

}
