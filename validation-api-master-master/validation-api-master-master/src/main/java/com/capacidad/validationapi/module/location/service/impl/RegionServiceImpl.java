package com.capacidad.validationapi.module.location.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.dto.RegionDTO;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.projection.CountryProjection;
import com.capacidad.validationapi.module.location.projection.RegionProjection;
import com.capacidad.validationapi.module.location.repository.CityRepository;
import com.capacidad.validationapi.module.location.repository.CountryRepository;
import com.capacidad.validationapi.module.location.repository.ProvinceRepository;
import com.capacidad.validationapi.module.location.repository.RegionRepository;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Service
public class RegionServiceImpl extends BaseServiceImpl<Region, RegionDTO, Long> implements RegionService {

    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ProvinceRepository provinceRepository;

    @Autowired
    public RegionServiceImpl(RegionRepository repository,
                             CountryRepository countryRepository,
                             CityRepository cityRepository,
                             ProvinceRepository provinceRepository) {
        super(repository);
        this.regionRepository = repository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.provinceRepository = provinceRepository;
    }

    @Override
    public Set<RegionProjection> getRegions(String name) {
        return regionRepository
                .findAllProjectedByNameContainingIgnoreCase(name);
    }

    @Override
    public List<CountryProjection> getAllCountries() {
        return countryRepository.findAllProjectedBy(CountryProjection.class);
    }

    @Override
    public List<IdAndNameOnlyProjection> getProvinces(long countryId) throws ObjectNotFoundException {
        if (!countryRepository.existsById(countryId))
            throw new ObjectNotFoundException(
                    "location.countryNotFound",
                    String.valueOf(countryId));
        return provinceRepository.findProjectedByCountryId(countryId);
    }

    @Override
    public List<IdAndNameOnlyProjection> getCities(long provinceId) throws ObjectNotFoundException {
        if (!provinceRepository.existsById(provinceId))
            throw new ObjectNotFoundException(
                    "location.provinceNotFound",
                    String.valueOf(provinceId));
        return cityRepository.findProjectedByProvinceId(provinceId);
    }

    @Override
    public boolean cityBelongToRegion(Region region, City city) {
        return regionRepository.existsByIdAndCities(region.getId(), city);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        Region region = this.findById(objectId);
        if (!region.getAuditTrays().isEmpty() && !allAuditTraysDeleted(region.getAuditTrays()))
            throw new ObjectNotValidException("region.cannotDeleteAuditTraysAttached");
        if (!region.getContractAdjustments().isEmpty() && !allContractAdjustmentsDeleted(region.getContractAdjustments()))
            throw new ObjectNotValidException("region.cannotDeleteContractAdjustmentsAttached");
        if (!region.getMedicalCoverages().isEmpty() && !allMedicalCoveragesDeleted(region.getMedicalCoverages()))
            throw new ObjectNotValidException("region.cannotDeleteMedicalCoveragesAttached");
        if (!region.getOrganizations().isEmpty() && !allOrganizationsDeleted(region.getOrganizations()))
            throw new ObjectNotValidException("region.cannotDeleteOrganizationsAttached");
        region.setDeleted(true);
        region.setDeletionToken(UUID.randomUUID());
        this.getRepository().save(region);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(region, this.getRepository()));
        return this.buildIdResponse(objectId);
    }

    private boolean allAuditTraysDeleted(Set<AuditTray> auditTrays) {
        return auditTrays.size() == auditTrays.stream()
                .filter(BaseEntity::getDeleted)
                .count();
    }

    private boolean allContractAdjustmentsDeleted(Set<ContractAdjustment> contractAdjustments) {
        return contractAdjustments.size() == contractAdjustments.stream()
                .filter(BaseEntity::getDeleted)
                .count();
    }

    private boolean allMedicalCoveragesDeleted(Set<MedicalCoverage> medicalCoverages) {
        return medicalCoverages.size() == medicalCoverages.stream()
                .filter(BaseEntity::getDeleted)
                .count();
    }

    private boolean allOrganizationsDeleted(Set<Organization> organizations) {
        return organizations.size() == organizations.stream()
                .filter(BaseEntity::getDeleted)
                .count();
    }

}
