package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.module.base.event.AfterSoftDeleteEvent;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryDTO;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryRelationshipDTO;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryRelativeDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.repository.BeneficiaryRepository;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryInsurancePlanService;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryService;
import com.capacidad.validationapi.module.general.dto.StatusUpdateDTO;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusScopeReference;
import com.capacidad.validationapi.module.person.model.RelationshipType;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static com.capacidad.validationapi.module.person.reference.IdTypeReference.TEMPORARY_ID;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.HOLDER;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class BeneficiaryServiceImpl extends BaseServiceImpl<Beneficiary, BeneficiaryDTO, Long> implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryInsurancePlanService beneficiaryInsurancePlanService;
    private final PropertiesService propertiesService;
    private final Random random;
    private final IdentityClientService identityClientService;
    private final TradeUnionService tradeUnionService;

    @Autowired
    public BeneficiaryServiceImpl(BeneficiaryRepository beneficiaryRepository,
                                  BeneficiaryInsurancePlanService beneficiaryInsurancePlanService,
                                  PropertiesService propertiesService,
                                  IdentityClientService identityClientService,
                                  TradeUnionService tradeUnionService) {
        super(beneficiaryRepository);
        this.beneficiaryRepository = beneficiaryRepository;
        this.beneficiaryInsurancePlanService = beneficiaryInsurancePlanService;
        this.propertiesService = propertiesService;
        this.random = new Random();
        this.identityClientService = identityClientService;
        this.tradeUnionService = tradeUnionService;
    }

    @Override
    public Beneficiary create(BeneficiaryDTO dto) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        Beneficiary object = this.mapDtoToInput(dto);
        validateHolderAge(object.getAge());
        this.validate(object);
        Status status = BENEFICIARY_WITH_COVERAGE.getInstance();
        object.setStatus(status);
        RelationshipType relationshipType = HOLDER.getInstance();
        object.setRelationshipType(relationshipType);
        object.associateChildObjects();
        return beneficiaryRepository.save(object);
    }

    private void validateHolderAge(int age) throws ObjectNotValidException {
        if (age < propertiesService.getProperties().getHolderBeneficiaryMinAge())
            throw new ObjectNotValidException("beneficiary.underAge", String.valueOf(age));
    }

    @Override
    public void validate(Beneficiary beneficiary) throws ObjectNotValidException, ObjectNotFoundException {
        validateIdTypeAndIdNumberAreNotUpdatedToNull(beneficiary);
        validatePaymentMethod(beneficiary);
        beneficiaryInsurancePlanService.validate(beneficiary.getBeneficiaryInsurancePlans());
    }

    private void validateIdTypeAndIdNumberAreNotUpdatedToNull(Beneficiary beneficiary) throws ObjectNotValidException {
        if (beneficiary.getIdType() == null || beneficiary.getIdNumber() == null)
            throw new ObjectNotValidException("beneficiary.invalidIdNumberOrType");
    }

    private void validatePaymentMethod(Beneficiary beneficiary) throws ObjectNotValidException {
        if (!beneficiary.isPaymentMethodInAccordanceWithCompany())
            throw new ObjectNotValidException("beneficiary.invalidPaymentMethod");
    }

    @Override
    public void validateUpdate(Beneficiary beneficiary) throws ObjectNotValidException, ObjectNotFoundException {
        this.validate(beneficiary);
        if (beneficiary.getRelatedBeneficiary() != null) {
            if (beneficiary.isHolder())
                throw new ObjectNotValidException("beneficiary.invalidRelationshipType");
            Beneficiary holder = beneficiary.getRelatedBeneficiary();
            updateRelativeWithHolderRequiredData(beneficiary, holder);
        } else {
            if (beneficiary.isHolder()) {
                List<Beneficiary> relatives = getRelatives(beneficiary);
                relatives.forEach(relative -> updateRelativeWithHolderRequiredData(relative, beneficiary));
                beneficiaryRepository.saveAll(relatives);
            }
        }
    }

    private void updateRelativeWithHolderRequiredData(Beneficiary relative, Beneficiary holder) {
        relative.setPaymentMethod(holder.getPaymentMethod());
        relative.setCompany(holder.getCompany());
        relative.setBeneficiaryCategory(holder.getBeneficiaryCategory());
    }

    @Override
    public Beneficiary createRelative(long holderBeneficiaryId, BeneficiaryRelativeDTO beneficiaryRelativeDto) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("createRelative - args: {}({})", beneficiaryRelativeDto.getClass(), beneficiaryRelativeDto);
        Beneficiary holderBeneficiary = this.findById(holderBeneficiaryId);
        Beneficiary relative = this.getObjectMapper().convertValue(beneficiaryRelativeDto, Beneficiary.class);
        if (relative.isNewborn())
            return createNewborn(holderBeneficiary, relative);
        validateRelative(relative);
        return createRelative(holderBeneficiary, relative);
    }

    private Beneficiary createNewborn(Beneficiary holder, Beneficiary newborn) throws ObjectNotValidException, ObjectNotFoundException {
        validateNewborn(newborn);
        newborn.setIdType(TEMPORARY_ID.getInstance());
        long tempIdNumber = buildNewbornTemporaryIdNumber(holder.getIdNumber());
        newborn.setIdNumber(tempIdNumber);
        return createRelative(holder, newborn);
    }

    private void validateNewborn(Beneficiary newborn) throws ObjectNotValidException {
        if (newborn.wasBornIn(LocalDate.now().getYear()))
            throw new ObjectNotValidException("beneficiary.newbornInvalidBirthDate");
    }

    private long buildNewbornTemporaryIdNumber(Long holderIdNumber) {
        String stringHolderIdNumber = holderIdNumber.toString();
        char[] charIdNumber = stringHolderIdNumber.toCharArray();
        String randomPick = "";
        for (int i = 0; i < 3; i++) {
            int randomElement = random.nextInt(charIdNumber.length);
            randomPick = StringUtils.join(randomPick, charIdNumber[randomElement]);
        }
        return Long.parseLong(StringUtils.join(randomPick, stringHolderIdNumber));
    }

    private void validateRelative(Beneficiary relative) throws ObjectNotValidException {
        if (relative.isHolder())
            throw new ObjectNotValidException("beneficiary.notAssignableRelationshipType");
    }

    private Beneficiary createRelative(Beneficiary holderBeneficiary, Beneficiary relative) throws ObjectNotValidException, ObjectNotFoundException {
        relative.setRelatedBeneficiary(holderBeneficiary);
        relateToHolderAndUpdateAddressData(holderBeneficiary, relative);
        this.validate(relative);
        Status status = BENEFICIARY_WITH_COVERAGE.getInstance();
        relative.setStatus(status);
        relative.associateChildObjects();
        Beneficiary objectResponse = beneficiaryRepository.save(relative);
        log.info("createRelative - Beneficiary: {}({})", relative.getClass(), relative);
        return objectResponse;
    }

    private void relateToHolderAndUpdateAddressData(Beneficiary holder, Beneficiary relative) throws ObjectNotValidException {
        relative.relateToHolder(holder);
        if (relative.getAddress() == null)
            relative.setAddress(holder.getAddress());
        if (relative.getPhone() == null)
            relative.setPhone(holder.getPhone());
    }

    @Override
    public BeneficiaryProjection updateStatus(long beneficiaryId, StatusUpdateDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = this.findById(beneficiaryId);
        Status newStatus = this.getUtils().getEntityReference(Status.class, input.getStatus().getId());
        beneficiary.setStatus(newStatus);
        beneficiary.setStatusUpdateDescription(input.getStatusUpdateDescription());
        validateStatusScope(beneficiary);
        Beneficiary updatedBeneficiary = beneficiaryRepository.save(beneficiary);
        return this.getProjectionFactory().createProjection(BeneficiaryProjection.class, updatedBeneficiary);
    }

    private void validateStatusScope(Beneficiary beneficiary) throws ObjectNotValidException {
        if (!beneficiary.getStatus().getStatusScope().getId().equals(StatusScopeReference.BENEFICIARY.getId()))
            throw new ObjectNotValidException("generic.invalidStatusScope");
    }

    @Override
    public BeneficiaryProjection updateRelationship(long beneficiaryId, BeneficiaryRelationshipDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary current = this.findById(beneficiaryId);
        if (!current.isHolder() && input.isHolder())
            current.makeHolder();
        else {
            if (current.isHolder() && !input.isHolder())
                removeHolderConditionAndRelateToANewHolder(current, input);
            else {
                if (!current.isHolder())
                    relateToHolder(current, input);
            }
        }
        Beneficiary updatedBeneficiary = beneficiaryRepository.saveAndFlush(current);
        beneficiaryRepository.refresh(updatedBeneficiary);
        return this.getProjectionFactory().createProjection(BeneficiaryProjection.class, updatedBeneficiary);
    }

    private void removeHolderConditionAndRelateToANewHolder(Beneficiary beneficiary, BeneficiaryRelationshipDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        validateHolderDoesNotContainRelatives(beneficiary);
        relateToHolder(beneficiary, input);
    }

    private void validateRelatedBeneficiaryIsNotNull(BeneficiaryRelationshipDTO input) throws ObjectNotValidException {
        if (input.getRelatedBeneficiary() == null)
            throw new ObjectNotValidException("beneficiary.relatedBeneficiaryRequired");
    }

    private void validateHolderDoesNotContainRelatives(Beneficiary holder) throws ObjectNotValidException {
        List<Beneficiary> beneficiaries = getRelatives(holder);
        if (!beneficiaries.isEmpty())
            throw new ObjectNotValidException("beneficiary.cannotRemoveHolderConditionWithRelatives");
    }

    private void validateNotRelatedToItself(Beneficiary beneficiary, BeneficiaryRelationshipDTO input) throws ObjectNotValidException {
        if (beneficiary.getId().equals(input.getRelatedBeneficiary().getId()))
            throw new ObjectNotValidException("beneficiary.cannotRelateToItself");
    }

    private void relateToHolder(Beneficiary beneficiary, BeneficiaryRelationshipDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        validateRelatedBeneficiaryIsNotNull(input);
        validateNotRelatedToItself(beneficiary, input);
        Beneficiary relateTo = this.findById(input.getRelatedBeneficiary().getId());
        RelationshipType relationshipType = this.getUtils().getGenericsEntityReference(RelationshipType.class, input.getRelationshipType().getId());
        beneficiary.setRelationshipType(relationshipType);
        beneficiary.relateToHolder(relateTo);
    }

    private List<Beneficiary> getRelatives(Beneficiary beneficiary) {
        return beneficiaryRepository.findAllByFamilyIdAndIdIsNot(beneficiary.getFamilyId(), beneficiary.getId());
    }

    @Override
    public Beneficiary save(Beneficiary beneficiary) {
        return beneficiaryRepository.save(beneficiary);
    }

    @Override
    public BeneficiaryProjection associateTradeUnion(long beneficiaryId, long tradeUnionId) throws ObjectNotValidException, ObjectNotFoundException {
        var beneficiary = this.findById(beneficiaryId);
        if (!beneficiary.isHolder())
            throw new ObjectNotValidException("beneficiary.cannotAssociateToAnonHolder");
        var tradeUnion = tradeUnionService.findById(tradeUnionId);
        if (beneficiary.getTradeUnions().contains(tradeUnion))
            throw new ObjectAlreadyExistsException("beneficiary.alreadyContainsTradeUnion", tradeUnion.getName());
        beneficiary.getTradeUnions().add(tradeUnion);
        var updatedBeneficiary = beneficiaryRepository.save(beneficiary);
        return this.getProjectionFactory().createProjection(BeneficiaryProjection.class, updatedBeneficiary);
    }

    @Override
    public BeneficiaryProjection dissociateTradeUnion(long beneficiaryId, long tradeUnionId) throws ObjectNotFoundException, ObjectNotValidException {
        var beneficiary = this.findById(beneficiaryId);
        var tradeUnion = tradeUnionService.findById(tradeUnionId);
        if (!beneficiary.getTradeUnions().contains(tradeUnion))
            throw new ObjectNotValidException("beneficiary.doesNotContainsTradeUnion", tradeUnion.getName());
        beneficiary.getTradeUnions().remove(tradeUnion);
        var updatedBeneficiary = beneficiaryRepository.save(beneficiary);
        return this.getProjectionFactory().createProjection(BeneficiaryProjection.class, updatedBeneficiary);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonNode delete(Long beneficiaryId) throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = this.findById(beneficiaryId);
        Set<Beneficiary> relatives = beneficiaryRepository.findAllByRelatedBeneficiaryId(beneficiary.getId());
        if (!relatives.isEmpty() && !allRelativesDeleted(relatives))
            throw new ObjectNotValidException("beneficiary.relativesAttached");
        beneficiary.getBatches().forEach(b -> {
            if (b.isActive())
                b.setStatus(BATCH_CANCELLED.getInstance());
        });
        beneficiary.getBudgets().forEach(b -> {
            if (!b.isPayed()) {
                b.setStatus(PAYED.getInstance());
                b.setClosedAt(LocalDateTime.now());
            }
        });
        beneficiary.setStatus(BENEFICIARY_WITHOUT_COVERAGE.getInstance());
        beneficiary.getBeneficiaryInsurancePlans().clear();
        beneficiary.getDiseases().clear();
        beneficiary.getExpirations().clear();
        beneficiary.setDeleted(true);
        beneficiary.setDeletionToken(UUID.randomUUID());
        beneficiary.setActiveBatch(false);
        beneficiaryRepository.save(beneficiary);
        identityClientService.deleteResourceIdAccounts(beneficiary.getResourceId());
        this.getApplicationEventPublisher().publishEvent(new AfterSoftDeleteEvent<>(beneficiary, beneficiaryRepository));
        return this.buildIdResponse(beneficiary.getId());
    }

    private boolean allRelativesDeleted(Set<Beneficiary> relatives) {
        return relatives.size() == relatives.stream()
                .filter(BaseEntity::getDeleted)
                .count();
    }

}
