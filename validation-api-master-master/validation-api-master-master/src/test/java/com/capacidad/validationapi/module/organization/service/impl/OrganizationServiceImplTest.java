package com.capacidad.validationapi.module.organization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.organization.repository.OrganizationRepository;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationServiceImplTest {

    @Mock
    private IdentityClientService identityClientService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    @InjectMocks
    private OrganizationServiceImpl organizationService;

    @Test
    public void testDeleteThrowsExceptionWhenContractAttached() throws ObjectNotFoundException {
        Organization organization = new Organization();
        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setName("organizationContract");
        organization.setContract(organizationContract);

        doReturn(organization).when(organizationService).findById(1L);
        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> organizationService.delete(1L));

        assertThat(exception.getMessage()).isEqualTo("contractShouldBeDeletedFirst");
    }

    @Test
    public void testDeleteExecutesSuccessfullyWhenNotContractAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setContract(null);

        testDeleteExecutesSuccessfully(organization);
    }

    @Test
    public void testDeleteExecutesSuccessfullyWhenDeletedContractAttached() throws ObjectNotFoundException, ObjectNotValidException {
        Organization organization = new Organization();
        organization.setId(1L);

        OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setDeleted(true);
        organization.setContract(organizationContract);

        testDeleteExecutesSuccessfully(organization);
    }

    private void testDeleteExecutesSuccessfully(Organization organization) throws ObjectNotValidException, ObjectNotFoundException {
        organization.getMedicalRegistrations().add(new MedicalRegistration());

        Organization related = new Organization();
        related.setRelatedOrganization(organization);

        Set<Organization> relatedSet = new HashSet<>();
        relatedSet.add(related);

        Address address = new Address();
        organization.setAddress(address);

        doReturn(organization).when(organizationService).findById(organization.getId());
        doReturn(new ObjectMapper()).when(organizationService).getObjectMapper();
        doReturn(applicationEventPublisher).when(organizationService).getApplicationEventPublisher();
        when(organizationRepository.findAllByRelatedOrganizationId(organization.getId())).thenReturn(Collections.singleton(related));

        JsonNode result = organizationService.delete(organization.getId());

        assertThat(result.get("id").asLong()).isEqualTo(organization.getId());
        assertThat(organization.getDeleted()).isTrue();
        assertThat(organization.getDeletionToken()).isNotEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThat(organization.getAddress().getDeleted()).isTrue();
        assertThat(organization.getAddress().getDeletionToken()).isEqualTo(organization.getDeletionToken());
        assertThat(related.getRelatedOrganization()).isNull();
        assertThat(organization.getMedicalRegistrations()).isEmpty();

        verify(organizationRepository, times(1)).saveAll(relatedSet);
        verify(organizationRepository, times(1)).save(organization);
        verify(identityClientService, times(1)).deleteResourceIdAccounts(organization.getResourceId());
        verify(applicationEventPublisher, times(1)).publishEvent(any(AfterSoftDeleteEvent.class));
    }

}
