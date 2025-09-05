package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageDTO;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageProjection;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import com.capacidad.validationapi.module.medicalcoverage.repository.ChargeTypeRepository;
import com.capacidad.validationapi.module.medicalcoverage.repository.MedicalCoverageRepository;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageItemService;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.person.model.Gender;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class MedicalCoverageServiceImpl extends BaseServiceImpl<MedicalCoverage, MedicalCoverageDTO, Long> implements MedicalCoverageService {

    private final MedicalCoverageRepository medicalCoverageRepository;
    private final MedicalCoverageItemService medicalCoverageItemService;
    private final ChargeTypeRepository chargeTypeRepository;
    private final RegionService regionService;
    private final ChargeCalculationContext chargeCalculationContext;
    private final RestrictionTypeValidator restrictionTypeValidator;
    private final MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Autowired
    public MedicalCoverageServiceImpl(MedicalCoverageRepository repository,
                                      MedicalCoverageItemService medicalCoverageItemService,
                                      ChargeTypeRepository chargeTypeRepository,
                                      RegionService regionService,
                                      ChargeCalculationContext chargeCalculationContext,
                                      RestrictionTypeValidator restrictionTypeValidator,
                                      MedicalAuthorizationItemService medicalAuthorizationItemService) {
        super(repository);
        this.medicalCoverageRepository = repository;
        this.medicalCoverageItemService = medicalCoverageItemService;
        this.chargeTypeRepository = chargeTypeRepository;
        this.regionService = regionService;
        this.chargeCalculationContext = chargeCalculationContext;
        this.restrictionTypeValidator = restrictionTypeValidator;
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
    }

    @Override
    public void validate(MedicalCoverage medicalCoverage) throws ObjectNotFoundException, ObjectNotValidException {
        validateCityOrRegion(medicalCoverage);
        if (medicalCoverage.getRegion() != null)
            validateRegion(medicalCoverage);
        if (medicalCoverage.getCity() != null)
            validateCity(medicalCoverage);
    }

    private void validateCityOrRegion(MedicalCoverage medicalCoverage) throws ObjectNotValidException {
        if ((medicalCoverage.getRegion() != null) == (medicalCoverage.getCity() != null))
            throw new ObjectNotValidException("medicalCoverage.regionOrCity");
    }

    private void validateRegion(MedicalCoverage medicalCoverage) throws ObjectNotFoundException, ObjectNotValidException {
        Region region = regionService.findById(medicalCoverage.getRegion().getId());
        if (medicalCoverageRepository.existsByCityOrRegion(medicalCoverage.getInsurancePlan(), region.getCities()))
            throw new ObjectAlreadyExistsException("medicalCoverage.alreadyExistsInRegion");
    }

    private void validateCity(MedicalCoverage medicalCoverage) throws ObjectNotValidException {
        if (medicalCoverageRepository.existsByCityOrRegion(medicalCoverage.getInsurancePlan(), Collections.singleton(medicalCoverage.getCity())))
            throw new ObjectAlreadyExistsException("medicalCoverage.cityAlreadyExists");
    }

    @Override
    public void validateUpdate(MedicalCoverage medicalCoverage) throws ObjectNotFoundException, ObjectNotValidException {
        validateCityOrRegion(medicalCoverage);
        if (medicalCoverage.getRegion() != null)
            validateRegionUpdate(medicalCoverage);
        if (medicalCoverage.getCity() != null)
            validateCityUpdate(medicalCoverage);
    }

    private void validateRegionUpdate(MedicalCoverage medicalCoverage) throws ObjectNotFoundException, ObjectNotValidException {
        Region region = regionService.findById(medicalCoverage.getRegion().getId());
        if (medicalCoverageRepository
                .existsByCityOrRegionAndIdIsNot(medicalCoverage.getInsurancePlan(),
                        region.getCities(),
                        medicalCoverage.getId()))
            throw new ObjectAlreadyExistsException("medicalCoverage.alreadyExistsInRegion");
    }

    private void validateCityUpdate(MedicalCoverage medicalCoverage) throws ObjectNotValidException {
        if (medicalCoverageRepository
                .existsByCityOrRegionAndIdIsNot(medicalCoverage.getInsurancePlan(),
                        Collections.singleton(medicalCoverage.getCity()),
                        medicalCoverage.getId()))
            throw new ObjectAlreadyExistsException("medicalCoverage.cityAlreadyExists");
    }

    @Override
    public MedicalCoverage create(MedicalCoverageDTO dto, InsurancePlan insurancePlan) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        MedicalCoverage object = this.mapDtoToInput(dto);
        object.setInsurancePlan(insurancePlan);
        this.validate(object);
        MedicalCoverage objectResponse = medicalCoverageRepository.save(object);
        log.info("create - void: {}({})", object.getClass(), object);
        return objectResponse;
    }

    @Override
    public Set<MedicalCoverageProjection> getMedicalCoverages(long insurancePlanId) {
        return medicalCoverageRepository
                .findAllProjectedByInsurancePlanId(insurancePlanId);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllChargeTypes() {
        return chargeTypeRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public MedicalCoverage findApplicableCoverage(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();
        List<BeneficiaryInsurancePlan> insurancePlans = beneficiary.getBeneficiaryInsurancePlans().stream()
                .sorted((bip1, bip2) -> bip2.getPriority() - bip1.getPriority())
                .collect(Collectors.toUnmodifiableList());
        for (BeneficiaryInsurancePlan beneficiaryInsurancePlan : insurancePlans) {
            Optional<MedicalCoverageItem> result = applyInsurancePlanCoverage(medicalAuthorizationItem, beneficiaryInsurancePlan.getInsurancePlan());
            if (result.isPresent()) {
                medicalAuthorizationItem.setMedicalCoverageItem(result.get());
                break;
            }
        }
        if (medicalAuthorizationItem.getMedicalCoverageItem() == null)
            throw new ObjectNotFoundException("medicalCoverage.applicableCoverageNotFound", beneficiary.getId().toString());
        return medicalAuthorizationItem.getMedicalCoverageItem().getMedicalCoverage();
    }

    @Override
    public void applyMedicalCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();
        MedicalCoverageItem medicalCoverageItem = medicalAuthorizationItem.getMedicalCoverageItem();
        validateAuthorizationItem(medicalCoverageItem, beneficiary, medicalAuthorizationItem);
    }

    private Optional<MedicalCoverageItem> applyInsurancePlanCoverage(MedicalAuthorizationItem medicalAuthorizationItem, InsurancePlan insurancePlan) {
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        Optional<MedicalCoverage> coverage = findApplicableMedicalCoverage(medicalAuthorization, insurancePlan);
        if (coverage.isPresent())
            return findApplicableMedicalCoverageItem(coverage.get(), medicalAuthorizationItem.getNomenclator());
        return Optional.empty();
    }

    private Optional<MedicalCoverage> findApplicableMedicalCoverage(MedicalAuthorization medicalAuthorization, InsurancePlan insurancePlan) {
        City medicalCenterCity = medicalAuthorization.getCity();
        return medicalCoverageRepository
                .findByInsurancePlanIdAndCityOrRegion(insurancePlan, Collections.singleton(medicalCenterCity));
    }

    private Optional<MedicalCoverageItem> findApplicableMedicalCoverageItem(MedicalCoverage medicalCoverage, Nomenclator nomenclator) {
        return medicalCoverageItemService.findMedicalCoverageItem(medicalCoverage.getId(), nomenclator.getId());
    }

    @Override
    public void calculateAuthorizationItemCharges(MedicalAuthorizationItem medicalAuthorizationItem) {
        MedicalCoverageItem medicalCoverageItem = medicalAuthorizationItem.getMedicalCoverageItem();
        if (freeCoverageIsAvailable(medicalCoverageItem, medicalAuthorizationItem)) {
            chargeCalculationContext.setChargeCalculationStrategy(new FreeChargeCalculationStrategyImpl());
        } else {
            if (medicalCoverageItem.isChargeTypePercentage())
                chargeCalculationContext.setChargeCalculationStrategy(new PercentageChargeCalculationStrategyImpl());
            if (medicalCoverageItem.isChargeTypeFixedValue())
                chargeCalculationContext.setChargeCalculationStrategy(new FixedChargeCalculationStrategyImpl());
        }
        chargeCalculationContext.calculateAuthorizationItemCharge(medicalCoverageItem, medicalAuthorizationItem);
    }

    private boolean freeCoverageIsAvailable(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (!medicalCoverageItem.hasFreePracticesConfigured())
            return false;
        long freeMaxDays = medicalCoverageItem.getFreeMaxDays().longValue();
        int freeMaxQuantity = medicalCoverageItem.getFreeMaxQuantity();
        int usedFreePractices = medicalAuthorizationItemService.countBeneficiaryFreeAuthorizationItemAmountInPeriod(medicalAuthorizationItem, freeMaxDays);
        return usedFreePractices < freeMaxQuantity;
    }

    private void validateAuthorizationItem(MedicalCoverageItem medicalCoverageItem, Beneficiary beneficiary, MedicalAuthorizationItem medicalAuthorizationItem) {
        validateAuditRequired(medicalCoverageItem, medicalAuthorizationItem);
        validateGender(beneficiary, medicalCoverageItem, medicalAuthorizationItem);
        validateFixedQuantity(medicalCoverageItem, medicalAuthorizationItem);
        validateAge(beneficiary, medicalCoverageItem, medicalAuthorizationItem);
        validateAwaitDays(beneficiary, medicalCoverageItem, medicalAuthorizationItem);
    }

    private void validateAuditRequired(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (Boolean.TRUE.equals(medicalCoverageItem.getAuditRequired()) &&
                medicalAuthorizationItem.getStatus().getId().equals(VALIDATION_APPROVED.getId())) {
            RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("validateAuditRequired",
                    "-",
                    "-",
                    null);
            RestrictionType audit = RestrictionTypeReference.AUDIT.getInstance();
            applyRestriction(audit, restrictionMessage, medicalAuthorizationItem);
        }
    }

    private void validateGender(Beneficiary beneficiary, MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (medicalCoverageItem.getGender() != Gender.INDISTINTO && medicalCoverageItem.getGender() != beneficiary.getGender()) {
            RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("validateGender",
                    medicalCoverageItem.getGender().toString(),
                    beneficiary.getGender().toString(),
                    null);
            applyRestriction(medicalCoverageItem.getRestrictionType(), restrictionMessage, medicalAuthorizationItem);
        }
    }

    private void validateFixedQuantity(MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (medicalCoverageItem.getFixedMaxQuantity() != null && medicalCoverageItem.getFixedMaxDays() != null) {
            long fixedMaxDays = medicalCoverageItem.getFixedMaxDays().longValue();
            LocalDateTime diff = LocalDate.now().minusDays(fixedMaxDays - 1).atTime(0, 0, 0, 0);
            List<MedicalAuthorizationItem> currentAuthorizationItems = medicalAuthorizationItemService
                    .getBeneficiaryAuthorizationItemAmountInPeriod(medicalAuthorizationItem, diff);
            int amount = currentAuthorizationItems.stream()
                    .mapToInt(MedicalAuthorizationItem::getQuantity)
                    .sum() + medicalAuthorizationItem.getQuantity();
            if (amount > medicalCoverageItem.getFixedMaxQuantity()) {
                RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("validateFixedQuantity",
                        medicalCoverageItem.getFixedMaxQuantity().toString(),
                        Integer.toString(amount),
                        buildFixedQuantityExtra(diff, currentAuthorizationItems));
                applyRestriction(medicalCoverageItem.getRestrictionType(), restrictionMessage, medicalAuthorizationItem);
            }
        }
    }

    private RestrictionMessageExtra buildFixedQuantityExtra(LocalDateTime from, List<MedicalAuthorizationItem> authorizations) {
        List<Object> idList = authorizations.stream()
                .map(i -> i.getMedicalAuthorization().getId())
                .collect(Collectors.toUnmodifiableList());
        return restrictionTypeValidator.buildRestrictionMessageExtra(RestrictionMessageExtraType.AUTHORIZATION_ID,
                idList,
                from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private void validateAge(Beneficiary beneficiary, MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        int beneficiaryAge = Period.between(beneficiary.getBirthDate(), LocalDate.now()).getYears();
        if (medicalCoverageItem.getAgeTo() != null && medicalCoverageItem.getAgeFrom() != null &&
                (beneficiaryAge < medicalCoverageItem.getAgeFrom() || beneficiaryAge > medicalCoverageItem.getAgeTo())) {
            RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("validateAge",
                    String.format("%d - %d", medicalCoverageItem.getAgeFrom(), medicalCoverageItem.getAgeTo()),
                    Integer.toString(beneficiaryAge),
                    null);
            applyRestriction(medicalCoverageItem.getRestrictionType(), restrictionMessage, medicalAuthorizationItem);
        }
    }

    private void validateAwaitDays(Beneficiary beneficiary, MedicalCoverageItem medicalCoverageItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        LocalDate awaitDays = beneficiary.getCreatedAt().toLocalDate().plusDays(medicalCoverageItem.getAwaitDays());
        if (awaitDays.isAfter(LocalDate.now())) {
            RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("validateAwaitDays",
                    medicalCoverageItem.getAwaitDays().toString(),
                    awaitDays.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    null);
            applyRestriction(medicalCoverageItem.getRestrictionType(), restrictionMessage, medicalAuthorizationItem);
        }
    }

    private void applyRestriction(RestrictionType restrictionType, RestrictionMessage restrictionMessage, MedicalAuthorizationItem medicalAuthorizationItem) {
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, FailureType.MEDICAL_COVERAGE, restrictionMessage);
        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorizationItem);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException {
        MedicalCoverage medicalCoverage = this.findById(objectId);
        medicalCoverage.setDeleted(true);
        medicalCoverage.setDeletionToken(UUID.randomUUID());
        handleCoverageItemsDelete(medicalCoverage);
        medicalCoverageRepository.save(medicalCoverage);
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(medicalCoverage, this.getRepository()));
        return buildIdResponse(objectId);
    }

    private void handleCoverageItemsDelete(MedicalCoverage medicalCoverage) {
        medicalCoverage.getMedicalCoverageItems().forEach(i -> {
            if (Boolean.FALSE.equals(i.getDeleted())) {
                i.setDeleted(true);
                i.setDeletionToken(medicalCoverage.getDeletionToken());
            }
        });
        Set<MedicalCoverageItem> itemsToHardDelete = medicalCoverage.getMedicalCoverageItems().stream()
                .filter(i -> i.getMedicalAuthorizationItems().isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        medicalCoverage.getMedicalCoverageItems().removeAll(itemsToHardDelete);
    }

}
