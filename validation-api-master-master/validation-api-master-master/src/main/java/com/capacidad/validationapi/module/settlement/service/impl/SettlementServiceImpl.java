package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.render.service.RenderService;
import com.capacidad.validationapi.module.settlement.dto.SettlementDTO;
import com.capacidad.validationapi.module.settlement.dto.SettlementUpdateDTO;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import com.capacidad.validationapi.module.settlement.projection.SettlementProjection;
import com.capacidad.validationapi.module.settlement.repository.SettlementRepository;
import com.capacidad.validationapi.module.settlement.service.SettlementItemService;
import com.capacidad.validationapi.module.settlement.service.SettlementRoleSpecificationBuilder;
import com.capacidad.validationapi.module.settlement.service.SettlementService;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Subgraph;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.functional.ThrowingFunction.throwingFunction;
import static com.capacidad.validationapi.functional.ThrowingSupplier.throwingSupplier;
import static com.capacidad.validationapi.module.general.reference.StatusReference.CLOSED_SETTLEMENT;
import static com.capacidad.validationapi.module.general.reference.StatusReference.OPEN_SETTLEMENT;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.hibernate.jpa.QueryHints.*;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class SettlementServiceImpl extends BaseServiceImpl<Settlement, SettlementDTO, Long> implements SettlementService {

    private final MedicalAuthorizationItemService medicalAuthorizationItemService;
    private final SettlementRepository settlementRepository;
    private final StorageService storageService;
    private final RenderService renderService;
    private final ContractMediator contractMediator;
    private final PractitionerService practitionerService;
    private final SettlementRoleSpecificationBuilder roleSpecificationBuilder;
    private final SettlementItemService settlementItemService;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SettlementServiceImpl(SettlementRepository repository,
                                 MedicalAuthorizationItemService medicalAuthorizationItemService,
                                 StorageService storageService,
                                 RenderService renderService,
                                 ContractMediator contractMediator,
                                 PractitionerService practitionerService,
                                 SettlementRoleSpecificationBuilder roleSpecificationBuilder,
                                 SettlementItemService settlementItemService) {
        super(repository);
        this.settlementRepository = repository;
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
        this.storageService = storageService;
        this.renderService = renderService;
        this.contractMediator = contractMediator;
        this.practitionerService = practitionerService;
        this.roleSpecificationBuilder = roleSpecificationBuilder;
        this.settlementItemService = settlementItemService;
    }

    @Override
    public SettlementProjection createSettlement(SettlementDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        Settlement saved = create(input);
        return this.getProjectionFactory().createProjection(SettlementProjection.class, saved);
    }

    @Override
    public Settlement create(SettlementDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", input.getClass(), input);
        var practitioner = practitionerService.findById(input.getPractitioner().getId());
        var contract = filterPractitionerContract(practitioner, input);
        Optional<Settlement> settlement = findSettlement(practitioner, contract, OPEN_SETTLEMENT);
        if (settlement.isPresent())
            throw new ObjectAlreadyExistsException("settlement.alreadyExistsOpenedSettlement");
        var newSettlement = buildSettlement(practitioner, contract, OPEN_SETTLEMENT);
        return settlementRepository.save(newSettlement);
    }

    private Contract filterPractitionerContract(Practitioner practitioner, SettlementDTO dto) throws ObjectNotFoundException {
        return practitioner.getContracts().stream()
                .filter(c -> c.getId().equals(dto.getContract().getId()))
                .findFirst()
                .orElseThrow(() -> new ObjectNotFoundException("settlement.invalidPractitionerContract"));
    }

    private Optional<Settlement> findSettlement(Practitioner practitioner, Contract contract, StatusReference statusReference) {
        return settlementRepository.findByPractitionerIdAndContractIdAndStatusId(practitioner.getId(),
                contract.getId(),
                statusReference.getId());
    }


    private Settlement buildSettlement(Practitioner practitioner, Contract contract, StatusReference statusReference) {
        var newSettlement = new Settlement();
        Status openSettlement = this.getUtils().getGenericsEntityReference(Status.class, statusReference.getId());
        newSettlement.setPractitioner(practitioner);
        newSettlement.setStatus(openSettlement);
        newSettlement.setTotal(new BigDecimal(0));
        newSettlement.setContract(contract);
        newSettlement.setOpenedAt(LocalDateTime.now());
        return newSettlement;
    }

    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException {
        var settlement = this.findById(objectId);
        Set<MedicalAuthorizationItem> medicalAuthorizationItems = settlement.getSettlementItems().stream()
                .map(SettlementItem::getMedicalAuthorizationItem)
                .collect(Collectors.toSet());
        medicalAuthorizationItems.forEach(i -> i.setSettled(false));
        medicalAuthorizationItemService.saveAll(medicalAuthorizationItems);
        settlementRepository.delete(settlement);
        var result = this.getObjectMapper().createObjectNode();
        result.put("id", settlement.getId());
        return result;
    }

    @Override
    public void createOrUpdateFromMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        buildSettlementItemsFromAuthorizationAndPersist(medicalAuthorization);
    }

    private void buildSettlementItemsFromAuthorizationAndPersist(MedicalAuthorization medicalAuthorization) {
        var medAuthSummaries = new HashMap<Long, List<SummaryDTO>>();
        var settlement = findOrCreateOpenSettlement(medicalAuthorization.getPractitioner(), medicalAuthorization.getContract());
        Set<SettlementItem> settlementItems = medicalAuthorization.getMedicalAuthorizationItems().stream()
                .map(mi -> {
                    Optional<SettlementItem> optSettlementItem = buildSettlementItemFromAuthorizationItem(mi, medAuthSummaries);
                    optSettlementItem.ifPresent(si -> {
                        mi.setSettled(true);
                        settlement.setTotal(settlement.getTotal().add(si.getSubtotal()));
                    });
                    return optSettlementItem;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableSet());
        settlement.getSettlementItems().addAll(settlementItems);
        settlement.associateChildObjects();
        settlementRepository.save(settlement);
    }

    private Settlement findOrCreateOpenSettlement(Practitioner practitioner, Contract contract) {
        return findSettlement(practitioner, contract, OPEN_SETTLEMENT)
                .orElseGet(throwingSupplier(() -> buildSettlement(practitioner, contract, OPEN_SETTLEMENT)));
    }

    private Optional<SettlementItem> buildSettlementItemFromAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem, Map<Long, List<SummaryDTO>> summaryMap) {
        try {
            validateItem(medicalAuthorizationItem, summaryMap);
            var settlementItem = buildSettlementItemFromAuthorizationItem(medicalAuthorizationItem);
            return Optional.of(settlementItem);
        } catch (ObjectNotValidException e) {
            return Optional.empty();
        }
    }

    private void validateItem(MedicalAuthorizationItem medicalAuthorizationItem, Map<Long, List<SummaryDTO>> summaryMap) throws ObjectNotValidException {
        validateSettled(medicalAuthorizationItem);
        validateSubtotal(medicalAuthorizationItem);
        validateStatus(medicalAuthorizationItem);
        validateExpiration(medicalAuthorizationItem);
        validateReportRequired(medicalAuthorizationItem, summaryMap);
    }

    private void validateSettled(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        if (Boolean.TRUE.equals(medicalAuthorizationItem.getSettled()))
            throw new ObjectNotValidException("settlement.alreadySettled", medicalAuthorizationItem.getId().toString());
    }

    private void validateSubtotal(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        if (medicalAuthorizationItem.getSubtotal() == null || medicalAuthorizationItem.getSubtotal().equals(new BigDecimal(0)))
            throw new ObjectNotValidException("settlement.invalidSubtotal", medicalAuthorizationItem.getId().toString());
    }

    private void validateStatus(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        if (!medicalAuthorizationItem.getStatus().getId().equals(StatusReference.VALIDATION_APPROVED.getId())) {
            throw new ObjectNotValidException("settlement.invalidItemStatus", medicalAuthorizationItem.getId().toString());
        }
    }

    private void validateExpiration(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        var nomenclatorConfig = medicalAuthorizationItem.getNomenclator().getNomenclatorConfig();
        var duration = Duration.between(medicalAuthorizationItem.getCreatedAt(), LocalDateTime.now());
        if (duration.toDays() > nomenclatorConfig.getExpirationDays())
            throw new ObjectNotValidException("settlement.expiredItem", medicalAuthorizationItem.getId().toString());
    }

    private void validateReportRequired(MedicalAuthorizationItem medicalAuthorizationItem, Map<Long, List<SummaryDTO>> medAuthSummaries) throws ObjectNotValidException {
        var nomenclatorConfig = medicalAuthorizationItem.getNomenclator().getNomenclatorConfig();
        if (Boolean.TRUE.equals(nomenclatorConfig.getReportRequired())) {
            var medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
            List<SummaryDTO> summaries = medAuthSummaries.get(medicalAuthorization.getId());
            if (summaries == null) {
                summaries = storageService.getFileList(FileType.REPORT, medicalAuthorization.getId());
                medAuthSummaries.put(medicalAuthorization.getId(), summaries);
            }
            if (summaries.isEmpty())
                throw new ObjectNotValidException("settlement.reportRequired", medicalAuthorization.getId().toString());
        }
    }

    private SettlementItem buildSettlementItemFromAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        var settlementItem = new SettlementItem();
        var medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        var paymentMethod = medicalAuthorization.getPaymentMethod();
        settlementItem.setMedicalAuthorization(medicalAuthorization);
        settlementItem.setMedicalAuthorizationItem(medicalAuthorizationItem);
        settlementItem.setUnitPrice(medicalAuthorizationItem.getUnitPrice());
        settlementItem.setQuantity(medicalAuthorizationItem.getQuantity());
        settlementItem.setSubtotal(medicalAuthorizationItem.getSubtotal());
        settlementItem.setChargeUnitPrice(new BigDecimal(0));
        settlementItem.setRefundable(medicalAuthorizationItem.getRefundable());
        if (paymentMethod.getId().equals(PaymentMethodReference.VOLUNTARY.getId())) {
            settlementItem.setChargeUnitPrice(medicalAuthorizationItem.getChargeUnitPrice());
            settlementItem.setSubtotal(medicalAuthorizationItem.getSubtotal()
                    .subtract(medicalAuthorizationItem.getChargeSubtotal())
                    .setScale(2, RoundingMode.HALF_UP));
        }
        settlementItem.setNomenclator(medicalAuthorizationItem.getNomenclator());
        settlementItem.setMedicalCenter(medicalAuthorization.getMedicalCenter());
        return settlementItem;
    }

    @Override
    public void createOrUpdateFromMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) {
        var medAuthSummaries = new HashMap<Long, List<SummaryDTO>>();
        Optional<SettlementItem> optSettlementItem = buildSettlementItemFromAuthorizationItem(medicalAuthorizationItem, medAuthSummaries);
        optSettlementItem.ifPresent(throwingConsumer(si -> {
            medicalAuthorizationItem.setSettled(true);
            var medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
            var settlement = findOrCreateOpenSettlement(medicalAuthorization.getPractitioner(), medicalAuthorization.getContract());
            settlement.setTotal(settlement.getTotal().add(si.getSubtotal()));
            settlement.getSettlementItems().add(si);
            settlement.associateChildObjects();
            settlementRepository.save(settlement);
        }));
    }

    @Override
    public SettlementProjection updateSettlement(long settlementId, SettlementUpdateDTO updateDTO) throws ObjectNotFoundException, ObjectNotValidException {
        var settlement = this.findById(settlementId);
        validateUpdate(settlement);
        Settlement result = executeStrategy(settlement, updateDTO);
        return this.getProjectionFactory().createProjection(SettlementProjection.class, result);
    }

    @Override
    public void validateUpdate(Settlement settlement) throws ObjectNotValidException {
        if (!SecurityUtils.isHighRankingAuthority() && settlement.getStatus().getId().equals(CLOSED_SETTLEMENT.getId()))
            throw new ObjectNotValidException("settlement.cannotOperateOverClosedSettlement");
    }

    private Settlement executeStrategy(Settlement settlement, SettlementUpdateDTO updateDTO) throws ObjectNotValidException {
        switch (updateDTO.getOperation()) {
            case REMOVE_ITEMS:
                return removeItems(settlement, updateDTO);
            case ADD_ITEMS:
                return addNewItems(settlement, updateDTO);
            case CLOSE:
                return closeSettlement(settlement);
            default:
                return updateSettlementData(settlement, updateDTO);
        }
    }

    private Settlement removeItems(Settlement settlement, SettlementUpdateDTO updateDTO) throws ObjectNotValidException {
        validateEmptyItems(updateDTO);
        Set<SettlementItem> filteredSettlementItems = settlement.getSettlementItems().stream()
                .filter(i -> updateDTO.getItemIds().contains(i.getId()))
                .collect(Collectors.toSet());
        filteredSettlementItems.forEach(i -> settlement.setTotal(settlement.getTotal().subtract(i.getSubtotal())));
        Set<MedicalAuthorizationItem> medAuthItemsToUpdate = filteredSettlementItems.stream()
                .map(SettlementItem::getMedicalAuthorizationItem)
                .collect(Collectors.toUnmodifiableSet());
        settlement.getSettlementItems().removeAll(filteredSettlementItems);
        medAuthItemsToUpdate.forEach(i -> i.setSettled(false));
        medicalAuthorizationItemService.saveAll(medAuthItemsToUpdate);
        return settlementRepository.save(settlement);
    }

    private void validateEmptyItems(SettlementUpdateDTO updateDTO) throws ObjectNotValidException {
        if (updateDTO.getItemIds() == null || updateDTO.getItemIds().isEmpty())
            throw new ObjectNotValidException("settlement.emptyItems");
    }

    private Settlement addNewItems(Settlement settlement, SettlementUpdateDTO updateDTO) throws ObjectNotValidException {
        validateEmptyItems(updateDTO);
        Set<MedicalAuthorizationItem> medicalAuthorizationItems = medicalAuthorizationItemService
                .findNotSettledItemsByPractitionerContractAndCollectionOfIds(settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds());
        validateItemsCorrespondence(medicalAuthorizationItems, updateDTO.getItemIds());
        buildAndAssociateSettlementItems(settlement, medicalAuthorizationItems);
        return settlementRepository.save(settlement);
    }

    private void validateItemsCorrespondence(Set<MedicalAuthorizationItem> medicalAuthorizationItems, Set<Long> medicalAuthorizationItemIds) throws ObjectNotValidException {
        if (medicalAuthorizationItems.size() != medicalAuthorizationItemIds.size())
            throw new ObjectNotValidException("settlement.invalidItems");
    }

    private void buildAndAssociateSettlementItems(Settlement settlement, Set<MedicalAuthorizationItem> medicalAuthorizationItems) {
        var medAuthSummaries = new HashMap<Long, List<SummaryDTO>>();
        Set<SettlementItem> settlementItems = medicalAuthorizationItems.stream().map(throwingFunction(mi -> {
            validateItem(mi, medAuthSummaries);
            mi.setSettled(true);
            var settlementItem = buildSettlementItemFromAuthorizationItem(mi);
            settlementItem.setSettlement(settlement);
            settlement.getSettlementItems().add(settlementItem);
            settlement.setTotal(settlement.getTotal().add(settlementItem.getSubtotal()));
            return settlementItem;
        }))
                .collect(Collectors.toUnmodifiableSet());
        medicalAuthorizationItemService.saveAll(medicalAuthorizationItems);
        settlement.getSettlementItems().addAll(settlementItems);
        settlement.associateChildObjects();
    }

    private Settlement closeSettlement(Settlement settlement) throws ObjectNotValidException {
        if (!settlement.getStatus().getId().equals(OPEN_SETTLEMENT.getId()))
            throw new ObjectNotValidException("settlement.invalidStatus", settlement.getId().toString());
        settlement.setClosedAt(LocalDateTime.now());
        validateClosingPeriod(settlement);
        if (settlement.getSettlementItems().isEmpty())
            throw new ObjectNotValidException("settlement.emptySettlement");
        Status closed = this.getUtils().getGenericsEntityReference(Status.class, CLOSED_SETTLEMENT.getId());
        settlement.setStatus(closed);
        return settlementRepository.save(settlement);
    }

    private void validateClosingPeriod(Settlement settlement) throws ObjectAlreadyExistsException {
        if (settlement.getClosedAt() != null) {
            var closedAt = settlement.getClosedAt().toLocalDate();
            boolean alreadyClosedSettlements = settlementRepository.existsByIdIsNotAndPractitionerIdAndStatusIdAndContractIdAndClosedAtBetween
                    (settlement.getId(),
                            settlement.getPractitioner().getId(),
                            CLOSED_SETTLEMENT.getId(),
                            settlement.getContract().getId(),
                            closedAt.with(firstDayOfMonth()).atTime(0, 0, 0, 0),
                            closedAt.with(lastDayOfMonth()).atTime(23, 59, 59, 59));
            if (alreadyClosedSettlements)
                throw new ObjectAlreadyExistsException("settlement.alreadyClosed");
        }
    }

    private Settlement updateSettlementData(Settlement settlement, SettlementUpdateDTO updateDTO) throws ObjectNotValidException {
        if (SecurityUtils.isHighRankingAuthority()) {
            if (updateDTO.getOpenedAt() != null)
                settlement.setOpenedAt(updateDTO.getOpenedAt());
            if (updateDTO.getClosedAt() != null && settlement.getClosedAt() != null)
                settlement.setClosedAt(updateDTO.getClosedAt());
            validateDuration(settlement);
            validateClosingPeriod(settlement);
            return settlementRepository.save(settlement);
        }
        return settlement;
    }

    private void validateDuration(Settlement settlement) throws ObjectNotValidException {
        if (settlement.getOpenedAt() != null && settlement.getClosedAt() != null) {
            var duration = Duration.between(settlement.getOpenedAt(), settlement.getClosedAt());
            if (duration.isNegative() || duration.isZero())
                throw new ObjectNotValidException("settlement.openedAtBiggerOrEqualClosedAt");
        }
    }

    @Override
    public ByteArrayOutputStream generateReceipt(long settlementId) throws ObjectNotValidException, ObjectNotFoundException {
        Map<String, Object> properties = buildQueryHints();
        var settlement = settlementRepository.findProjectedByIdWithHints(Settlement.class, settlementId, properties)
                .orElseThrow(() -> new ObjectNotFoundException("base.notFound", String.valueOf(settlementId)));
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("generatedBy", SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("-"));
        templateValues.put("generatedAt", LocalDateTime.now());
        templateValues.put("data", settlement);
        return renderService.renderPDF("settlement", templateValues);
    }

    @Override
    public ByteArrayOutputStream generateItemsReceipt(long settlementId, Set<Long> itemIds) throws ObjectNotValidException {
        List<SettlementItem> settlementItems = settlementItemService.findAllBySettlementIdAndItemIds(settlementId, itemIds);
        return generatePartialReceipt(settlementItems);
    }

    private ByteArrayOutputStream generatePartialReceipt(List<SettlementItem> settlementItems) throws ObjectNotValidException {
        if (settlementItems.isEmpty())
            throw new ObjectNotValidException("settlement.emptyItems");
        var settlement = settlementItems.iterator().next().getSettlement();
        var partialTotal = new BigDecimal(0);
        for (SettlementItem item : settlementItems)
            partialTotal = partialTotal.add(item.getSubtotal());
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("generatedBy", SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("-"));
        templateValues.put("generatedAt", LocalDateTime.now());
        templateValues.put("data", settlement);
        templateValues.put("items", settlementItems);
        templateValues.put("partialTotal", partialTotal);
        return renderService.renderPDF("partial_settlement", templateValues);
    }

    @Override
    public ByteArrayOutputStream generateBeneficiaryItemsReceipt(long settlementId, String beneficiaryCode) throws ObjectNotValidException {
        List<SettlementItem> settlementItems = settlementItemService.findAllBySettlementIdAndBeneficiaryCode(settlementId, beneficiaryCode);
        return generatePartialReceipt(settlementItems);
    }

    private Map<String, Object> buildQueryHints() {
        Map<String, Object> queryHints = new HashMap<>();
        queryHints.put(HINT_FETCH_SIZE, "50");
        queryHints.put(HINT_READONLY, true);
        queryHints.put(HINT_CACHEABLE, true);
        queryHints.put("javax.persistence.fetchgraph", buildEntityGraph());
        return queryHints;
    }

    private EntityGraph<Settlement> buildEntityGraph() {
        EntityGraph<Settlement> entityGraph = entityManager.createEntityGraph(Settlement.class);
        Subgraph<SettlementItem> itemSubgraph = entityGraph.addSubgraph("settlementItems");
        itemSubgraph.addSubgraph("nomenclator").addSubgraph("medicalPractice");
        Subgraph<MedicalAuthorization> medAuthSubgraph = itemSubgraph.addSubgraph("medicalAuthorization");
        medAuthSubgraph.addSubgraph("medicalCenter");
        medAuthSubgraph.addSubgraph("beneficiary");
        Subgraph<Beneficiary> beneficiarySubgraph = medAuthSubgraph.addSubgraph("beneficiary");
        beneficiarySubgraph.addSubgraph("idType");
        Subgraph<Practitioner> practitionerSubgraph = entityGraph.addSubgraph("practitioner");
        practitionerSubgraph.addSubgraph("idType");
        practitionerSubgraph.addSubgraph("medicalSpecialties");
        entityGraph.addSubgraph("contract");
        return entityGraph;
    }

    @Override
    public boolean belongsToPractitioner(long settlementId) {
        return settlementRepository.existsByIdAndPractitionerResourceId
                (settlementId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public boolean belongsToContract(long settlementId) {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return settlementRepository.existsByIdAndContractIn
                (settlementId, contracts);
    }

    @Override
    public void closeAllIfContractDayMatchesDate(LocalDate dateToCompare) {
        Set<Settlement> openedSettlements = settlementRepository.findAllByStatusIdAndContractAutoSettlementIsTrue(OPEN_SETTLEMENT.getId());
        Map<Contract, List<Settlement>> contractSettlementMap = openedSettlements.stream()
                .collect(Collectors.groupingBy(Settlement::getContract));
        for (Map.Entry<Contract, List<Settlement>> item : contractSettlementMap.entrySet()) {
            List<Settlement> settlements = item.getValue();
            var dateToClose = item.getKey().determineLocalDateFromDayOfSettlement();
            if (dateToClose.isEqual(dateToCompare)) {
                settlements.forEach(s -> {
                    s.setClosedAt(LocalDateTime.now());
                    s.setStatus(CLOSED_SETTLEMENT.getInstance());
                });
                settlementRepository.saveAll(settlements);
            }
        }
    }

    @Override
    public Optional<Specification<Settlement>> appendCustomSpecification() {
        return roleSpecificationBuilder.buildSpecification();
    }


}
