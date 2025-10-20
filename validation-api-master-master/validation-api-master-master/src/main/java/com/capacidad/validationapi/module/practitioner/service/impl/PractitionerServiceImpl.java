package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import com.capacidad.validationapi.module.contract.service.ContractService;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusScopeReference;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.reference.MedicalSpecialtyReference;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import com.capacidad.validationapi.module.practitioner.dto.PractitionerDTO;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.practitioner.repository.PractitionerRepository;
import com.capacidad.validationapi.module.practitioner.service.MedicalRegistrationService;
import com.capacidad.validationapi.module.practitioner.service.MedicalSpecialtyService;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.rating.Rating;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.AVAILABLE;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PAYED;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class PractitionerServiceImpl extends BaseServiceImpl<Practitioner, PractitionerDTO, Long> implements PractitionerService {

    private final PractitionerRepository practitionerRepository;
    private final MedicalCenterService medicalCenterService;
    private final MedicalSpecialtyService medicalSpecialtyService;
    private final MedicalRegistrationService medicalRegistrationService;
    private final ContractService contractService;
    private final Hashids hashids;
    private final IdentityClientService identityClientService;

    @Autowired
    public PractitionerServiceImpl(PractitionerRepository repository,
                                   MedicalCenterService medicalCenterService,
                                   MedicalSpecialtyService medicalSpecialtyService,
                                   ContractService contractService,
                                   MedicalRegistrationService medicalRegistrationService,
                                   @Qualifier("reducedHashids") Hashids hashids,
                                   IdentityClientService identityClientService) {
        super(repository);
        this.practitionerRepository = repository;
        this.medicalCenterService = medicalCenterService;
        this.medicalSpecialtyService = medicalSpecialtyService;
        this.contractService = contractService;
        this.medicalRegistrationService = medicalRegistrationService;
        this.hashids = hashids;
        this.identityClientService = identityClientService;
    }

    @Override
    public Practitioner create(PractitionerDTO dto) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        Practitioner object = this.mapDtoToInput(dto);
        this.validate(object);
        object.setStatus(AVAILABLE.getInstance());
        object.associateChildObjects();
        Practitioner savedPractitioner = practitionerRepository.save(object);
        savedPractitioner.setTransactionKey(generateKey(savedPractitioner));
        practitionerRepository.flush();
        practitionerRepository.refresh(savedPractitioner);
        if (savedPractitioner.getContracts() != null && !savedPractitioner.getContracts().isEmpty())
            validatePractitionerContracts(savedPractitioner);
        log.info("create - void: {}({})", object.getClass(), object);
        return savedPractitioner;
    }

    private void validatePractitionerContracts(Practitioner practitioner) {
        Set<Contract> contracts = practitioner.getContracts();
        contracts.forEach(throwingConsumer(c -> {
            if (c instanceof OrganizationContract) {
                OrganizationContract contract = (OrganizationContract) c;
                if (!medicalRegistrationService.practitionerBelongsOrganization(practitioner.getId(), contract.getOrganization().getId()))
                    throw new ObjectNotValidException("practitioner.invalidOrganizationContract");
            }
            if (c instanceof PractitionerContract) {
                PractitionerContract contract = (PractitionerContract) c;
                if (!contract.getPractitioner().getId().equals(practitioner.getId()))
                    throw new ObjectNotValidException("practitioner.invalidPractitionerContract");
            }
        }));
    }

    @Override
    public PersonDetailProjection getPersonDetails(long practitionerId) throws ObjectNotFoundException {
        return practitionerRepository
                .findPractitionerProjectedById(practitionerId)
                .orElseThrow(() -> new ObjectNotFoundException("practitioner.notFound", String.valueOf(practitionerId)));
    }

    @Override
    public Optional<Specification<Practitioner>> appendCustomSpecification() {
        if (SecurityUtils.isMedicalCenter())
            return Optional.of((root, query, builder) -> {
                var join = root.join("medicalCenters");
                return builder.equal(join.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
            });
        if (SecurityUtils.isOrganization())
            return Optional.of((root, query, builder) -> {
                var join = root.join("medicalRegistrations").join("organization");
                return builder.equal(join.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
            });
        return Optional.empty();
    }

    @Override
    public Set<PractitionerProjection.Minor> findAvailablePractitioners(String lastName) {
        return practitionerRepository
                .findAllProjectedByLastNameContainingIgnoreCaseAndStatusId
                        (lastName, AVAILABLE.getId());
    }

    @Override
    public boolean canPerformMedicalPractice(Practitioner practitioner, MedicalPractice medicalPractice) {
        AtomicBoolean canPerform = new AtomicBoolean(false);
        Set<MedicalSpecialty> relatedSpecialties = medicalSpecialtyService.getMedicalPracticeSpecialties(medicalPractice.getId());
        MedicalSpecialty general = this.getUtils().getGenericsEntityReference(MedicalSpecialty.class, MedicalSpecialtyReference.ALL.getId());
        if (relatedSpecialties.contains(general))
            return true;
        relatedSpecialties.forEach(relatedSpecialty -> practitioner.getMedicalSpecialties().forEach(practitionerSpecialty -> {
            if (practitionerSpecialty.getId().equals(relatedSpecialty.getId())) {
                canPerform.set(true);
            }
        }));
        return canPerform.get();
    }

    @Override
    public PractitionerProjection associateMedicalCenter(long practitionerId, long medicalCenterId) throws
            ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = this.findById(practitionerId);
        MedicalCenter medicalCenter = medicalCenterService.findById(medicalCenterId);
        if (practitioner.getMedicalCenters().contains(medicalCenter))
            throw new ObjectAlreadyExistsException(
                    "practitioner.alreadyBelongsToMedicalCenter",
                    medicalCenter.getName());
        practitioner.getMedicalCenters().add(medicalCenter);
        Practitioner updatedPractitioner = practitionerRepository.save(practitioner);
        return this.getProjectionFactory().createProjection(PractitionerProjection.class, updatedPractitioner);
    }

    @Override
    public PractitionerProjection dissociateMedicalCenter(long practitionerId, long medicalCenterId) throws
            ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = this.findById(practitionerId);
        MedicalCenter medicalCenter = medicalCenterService.findById(medicalCenterId);
        Set<MedicalCenter> medicalCenters = practitioner.getMedicalCenters();
        if (medicalCenters.size() == 1)
            throw new ObjectNotValidException("practitioner.medicalCenterRequirement");
        if (!medicalCenters.contains(medicalCenter))
            throw new ObjectNotValidException(
                    "practitioner.invalidMedicalCenter",
                    medicalCenter.getName());
        practitioner.getMedicalCenters().remove(medicalCenter);
        Practitioner updatedPractitioner = practitionerRepository.save(practitioner);
        return this.getProjectionFactory().createProjection(PractitionerProjection.class, updatedPractitioner);
    }

    @Override
    public PractitionerProjection associateContract(long practitionerId, long contractId) throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = this.findById(practitionerId);
        Contract contract = contractService.findById(contractId);
        if (practitioner.getContracts().contains(contract))
            throw new ObjectAlreadyExistsException(
                    "practitioner.alreadyContainsContract",
                    contract.getName());
        practitioner.getContracts().add(contract);
        validatePractitionerContracts(practitioner);
        Practitioner updatedPractitioner = practitionerRepository.save(practitioner);
        return this.getProjectionFactory().createProjection(PractitionerProjection.class, updatedPractitioner);
    }

    @Override
    public PractitionerProjection dissociateContract(long practitionerId, long contractId) throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = this.findById(practitionerId);
        Contract contract = contractService.findById(contractId);
        if (!practitioner.getContracts().contains(contract))
            throw new ObjectNotValidException(
                    "practitioner.invalidContract",
                    contract.getName());
        practitioner.getContracts().remove(contract);
        Practitioner updatedPractitioner = practitionerRepository.save(practitioner);
        return this.getProjectionFactory().createProjection(PractitionerProjection.class, updatedPractitioner);
    }

    @Override
    public PractitionerProjection associateMedicalSpecialty(long practitionerId, long medicalSpecialtyId) throws
            ObjectNotFoundException, ObjectAlreadyExistsException {
        Practitioner practitioner = this.findById(practitionerId);
        MedicalSpecialty medicalSpecialty = medicalSpecialtyService.findById(medicalSpecialtyId);
        if (practitioner.getMedicalSpecialties().contains(medicalSpecialty))
            throw new ObjectAlreadyExistsException(
                    "practitioner.alreadyContainsSpecialty",
                    medicalSpecialty.getName());
        practitioner.getMedicalSpecialties().add(medicalSpecialty);
        Practitioner updatedPractitioner = practitionerRepository.save(practitioner);
        return this.getProjectionFactory().createProjection(PractitionerProjection.class, updatedPractitioner);
    }

    @Override
    public PractitionerProjection dissociateMedicalSpecialty(long practitionerId, long medicalSpecialtyId) throws
            ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = this.findById(practitionerId);
        MedicalSpecialty medicalSpecialty = medicalSpecialtyService.findById(medicalSpecialtyId);
        Set<MedicalSpecialty> medicalSpecialties = practitioner.getMedicalSpecialties();
        if (medicalSpecialties.size() == 1)
            throw new ObjectNotValidException("practitioner.specialtiesRequirement");
        if (!medicalSpecialties.contains(medicalSpecialty))
            throw new ObjectNotValidException(
                    "practitioner.invalidSpecialty",
                    medicalSpecialty.getName());
        practitioner.getMedicalSpecialties().remove(medicalSpecialty);
        Practitioner updatedPractitioner = practitionerRepository.save(practitioner);
        return this.getProjectionFactory().createProjection(PractitionerProjection.class, updatedPractitioner);
    }

    @Override
    public BaseProjection<Long> getMedicalRegistrationOwner(long medicalRegistrationId) throws ObjectNotFoundException {
        return practitionerRepository.findByMedicalRegistrationsId(medicalRegistrationId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "practitioner.medicalRegistrationNotFound",
                        String.valueOf(medicalRegistrationId)));
    }

    @Override
    public PractitionerProjection updateStatus(long practitionerId, StatusUpdateDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        Practitioner practitioner = this.findById(practitionerId);
        Status newStatus = this.getUtils().getGenericsEntityReference(Status.class, input.getStatus().getId());
        practitioner.setStatus(newStatus);
        practitioner.setStatusUpdateDescription(input.getStatusUpdateDescription());
        if (!practitioner.getStatus().getStatusScope().getId().equals(StatusScopeReference.GENERAL.getId()))
            throw new ObjectNotValidException("generic.invalidStatusScope");
        Practitioner updatedPractitioner = practitionerRepository.save(practitioner);
        return this.getProjectionFactory().createProjection(PractitionerProjection.class, updatedPractitioner);
    }

    @Override
    public boolean existsByAuthentication(long practitionerId) {
        return practitionerRepository.existsByIdAndResourceId(practitionerId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public boolean belongsToMedicalCenter(Practitioner practitioner, MedicalCenter medicalCenter) {
        return practitionerRepository.existsByIdAndMedicalCentersId(practitioner.getId(), medicalCenter.getId());
    }

    @Override
    public PractitionerProjection getProjectedAuthPractitioner() throws ObjectNotFoundException {
        return practitionerRepository
                .findProjectedByResourceId
                        (SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException("practitioner.resourceIdNotFound"));
    }

    @Override
    public Practitioner getAuthPractitioner() throws ObjectNotFoundException {
        return practitionerRepository
                .findByResourceId
                        (SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException("practitioner.resourceIdNotFound"));
    }

    @Override
    public void renewAllKeys() {
        Set<Practitioner> practitioners = practitionerRepository.findAllBy();
        practitioners.forEach(p -> p.setTransactionKey(generateKey(p)));
        practitionerRepository.saveAll(practitioners);
    }

    @Override
    public String getAuthPractitionerKey() throws ObjectNotFoundException {
        Practitioner practitioner = getAuthPractitioner();
        return practitioner.getTransactionKey();
    }

    @Override
    public Optional<Practitioner> findOptionallyByIdTypeAndIdNumber(IdType idType, Long idNumber) {
        return practitionerRepository.findByIdTypeIdAndIdNumber(idType.getId(), idNumber);
    }

    @Override
    public void addRating(Practitioner practitioner, Rating rating) {
        if (practitioner.getRating() == null)
            practitioner.setRating(rating);
        else {
            Rating currentRating = practitioner.getRating();
            Integer newQuantity = currentRating.getQuantity() + rating.getQuantity();
            currentRating.setQuantity(newQuantity);
            currentRating.setDuration(addToExistingAverage(currentRating.getDuration(), rating.getDuration(), newQuantity));
            currentRating.setWaitTime(addToExistingAverage(currentRating.getWaitTime(), rating.getWaitTime(), newQuantity));
            currentRating.setCharges(addToExistingAverage(currentRating.getCharges(), rating.getCharges(), newQuantity));
            currentRating.setQuality(addToExistingAverage(currentRating.getQuality(), rating.getQuality(), newQuantity));
            currentRating.setAverage(addToExistingAverage(currentRating.getAverage(), rating.getAverage(), newQuantity));
        }
        practitionerRepository.save(practitioner);
    }

    private BigDecimal addToExistingAverage(BigDecimal prevValue, BigDecimal newValue, Integer size) {
        return newValue.subtract(prevValue).divide(new BigDecimal(size), 1, RoundingMode.HALF_UP).add(prevValue).round(new MathContext(2));
    }

    @Override
    public void removeRating(Practitioner practitioner, Rating rating) {
        Rating currentRating = practitioner.getRating();
        if (rating != null) {
            if (currentRating.getQuantity() == 1)
                practitioner.setRating(null);
            else {
                Integer currentQuantity = currentRating.getQuantity();
                currentRating.setDuration(removeFromExistingAverage(currentRating.getDuration(), rating.getDuration(), currentQuantity));
                currentRating.setWaitTime(removeFromExistingAverage(currentRating.getWaitTime(), rating.getWaitTime(), currentQuantity));
                currentRating.setCharges(removeFromExistingAverage(currentRating.getCharges(), rating.getCharges(), currentQuantity));
                currentRating.setQuality(removeFromExistingAverage(currentRating.getQuality(), rating.getQuality(), currentQuantity));
                currentRating.setAverage(removeFromExistingAverage(currentRating.getAverage(), rating.getAverage(), currentQuantity));
                currentRating.setQuantity(currentQuantity - rating.getQuantity());
            }
            practitionerRepository.save(practitioner);
        }
    }

    @Override
    public Practitioner findByIdLocked(long practitionerId) throws ObjectNotFoundException {
        return practitionerRepository
                .findLockedById
                        (practitionerId)
                .orElseThrow(() -> new ObjectNotFoundException("base.notFound", String.valueOf(practitionerId)));
    }

    @Override
    public Practitioner getAuthPractitionerLocked() throws ObjectNotFoundException {
        return practitionerRepository
                .findLockedByResourceId
                        (SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException("practitioner.resourceIdNotFound"));
    }

    private BigDecimal removeFromExistingAverage(BigDecimal prevValue, BigDecimal newValue, Integer size) {
        BigDecimal sizeBigDecimal = new BigDecimal(size);
        BigDecimal multiplication = prevValue.multiply(sizeBigDecimal);
        BigDecimal subtraction = multiplication.subtract(newValue);
        BigDecimal divisorSubtraction = sizeBigDecimal.subtract(new BigDecimal(1));
        return subtraction.divide(divisorSubtraction, 1, RoundingMode.HALF_UP);
    }

    private String generateKey(Practitioner practitioner) {
        String keyString = StringUtils.join(practitioner.getId(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        long keyLong = Long.parseLong(keyString);
        return hashids.encode(keyLong);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long practitionerId) throws ObjectNotValidException, ObjectNotFoundException {
        Practitioner practitioner = this.findById(practitionerId);
        if (practitioner.getContract() != null && !practitioner.getContract().getDeleted())
            throw new ObjectNotValidException("contractShouldBeDeletedFirst", practitioner.getContract().getName());
        practitioner.getContracts().clear();
        practitioner.getMedicalCenters().clear();
        practitioner.getMedicalRegistrations().clear();
        practitioner.getBatchItems().forEach(i -> i.getPractitioners().remove(practitioner));
        practitioner.getMedicalSpecialties().clear();
        practitioner.getBudgets().forEach(b -> {
            if (!b.isPayed()) {
                b.setStatus(PAYED.getInstance());
                b.setClosedAt(LocalDateTime.now());
            }
        });
        practitioner.getPreMedicalAuthorizations().forEach(p -> {
            if (p.isActive())
                p.setPetitioner(null);
        });
        practitioner.setDeleted(true);
        practitioner.setDeletionToken(UUID.randomUUID());
        practitioner.getAddress().setDeleted(true);
        practitioner.getAddress().setDeletionToken(practitioner.getDeletionToken());
        practitionerRepository.save(practitioner);
        identityClientService.deleteResourceIdAccounts(practitioner.getResourceId());
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(practitioner, practitionerRepository));
        return this.buildIdResponse(practitioner.getId());
    }

}
