package com.capacidad.validationapi.module.organization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.organization.dto.OrganizationDTO;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.organization.projection.OrganizationProjection;
import com.capacidad.validationapi.module.organization.repository.OrganizationRepository;
import com.capacidad.validationapi.module.organization.service.OrganizationService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
public class OrganizationServiceImpl extends BaseServiceImpl<Organization, OrganizationDTO, Long> implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final RegionService regionService;
    private final IdentityClientService identityClientService;

    @Autowired
    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   RegionService regionService,
                                   IdentityClientService identityClientService) {
        super(organizationRepository);
        this.organizationRepository = organizationRepository;
        this.regionService = regionService;
        this.identityClientService = identityClientService;
    }

    @Override
    public void validate(Organization organization) throws ObjectNotValidException {
        if (organization.getRegion() != null && !regionService.cityBelongToRegion(organization.getRegion(), organization.getAddress().getCity()))
            throw new ObjectNotValidException("organization.invalidCityRegion");
    }

    @Override
    public Set<OrganizationProjection> findOrganizationsContaining(String name) {
        return organizationRepository
                .findProjectedByNameContainingIgnoreCase(name);
    }

    @Override
    public OrganizationProjection getProjectedAuthOrganization() throws ObjectNotFoundException {
        return organizationRepository.findProjectedByResourceId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException("organization.resourceIdNotFound"));
    }

    @Override
    public Organization getAuthOrganization() throws ObjectNotFoundException {
        return organizationRepository.findByResourceId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException("organization.resourceIdNotFound"));
    }

    @Override
    public List<Long> getAuthOrganizationCityIds() {
        try {
            Organization organization = getAuthOrganization();
            Region region = organization.getRegion();
            return region != null ?
                    region.getCities().stream().map(City::getId).collect(Collectors.toUnmodifiableList()) :
                    Collections.singletonList(organization.getAddress().getCity().getId());
        } catch (ObjectNotFoundException e) {
            log.debug(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long organizationId) throws ObjectNotValidException, ObjectNotFoundException {
        Organization organization = this.findById(organizationId);
        if (organization.getContract() != null && !organization.getContract().getDeleted())
            throw new ObjectNotValidException("contractShouldBeDeletedFirst", organization.getContract().getName());
        UUID deletionToken = UUID.randomUUID();
        organization.getMedicalRegistrations().clear();
        organization.getAddress().setDeleted(true);
        organization.getAddress().setDeletionToken(deletionToken);
        organization.setDeleted(true);
        organization.setDeletionToken(deletionToken);
        Set<Organization> relatedOrganizations = organizationRepository.findAllByRelatedOrganizationId(organization.getId());
        relatedOrganizations.forEach(ro -> ro.setRelatedOrganization(null));
        organizationRepository.saveAll(relatedOrganizations);
        organizationRepository.save(organization);
        identityClientService.deleteResourceIdAccounts(organization.getResourceId());
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(organization, organizationRepository));
        return this.buildIdResponse(organization.getId());
    }

}
