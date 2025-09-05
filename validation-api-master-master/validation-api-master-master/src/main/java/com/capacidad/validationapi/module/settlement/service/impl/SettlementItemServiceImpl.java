package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import com.capacidad.validationapi.module.settlement.repository.SettlementItemRepository;
import com.capacidad.validationapi.module.settlement.service.SettlementItemService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.CLOSED_SETTLEMENT;
import static org.hibernate.jpa.QueryHints.*;

@Log4j2
@Service
public class SettlementItemServiceImpl extends BaseServiceImpl<SettlementItem, IdDTO<Long>, Long> implements SettlementItemService {

    private final SettlementItemRepository settlementItemRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SettlementItemServiceImpl(SettlementItemRepository repository) {
        super(repository);
        this.settlementItemRepository = repository;
    }

    @Override
    public void removeItems(MedicalAuthorization medicalAuthorization) {
        Set<SettlementItem> settlementItems = settlementItemRepository
                .findAllByMedicalAuthorizationId(medicalAuthorization.getId());
        settlementItems.forEach(throwingConsumer(i -> {
            var settlement = i.getSettlement();
            if (settlement.getStatus().getId().equals(CLOSED_SETTLEMENT.getId()))
                throw new ObjectNotValidException("settlement.cannotRemoveFromAlreadyClosedSettle");
            settlement.setTotal(settlement.getTotal().subtract(i.getSubtotal()));
            settlement.getSettlementItems().remove(i);
        }));
        settlementItemRepository.saveAll(settlementItems);
    }

    @Override
    public List<SettlementItem> findAllBySettlementIdAndItemIds(long settlementId, Set<Long> itemIds) {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SettlementItem> criteriaQuery = criteriaBuilder.createQuery(SettlementItem.class);
        Root<SettlementItem> root = criteriaQuery.from(SettlementItem.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("settlement").get("id"), settlementId),
                root.get("id").in(itemIds)));
        TypedQuery<SettlementItem> typedQuery = entityManager.createQuery(criteriaQuery);
        buildQueryHints().forEach(typedQuery::setHint);
        return typedQuery.getResultList();
    }

    private Map<String, Object> buildQueryHints() {
        Map<String, Object> queryHints = new HashMap<>();
        queryHints.put(HINT_FETCH_SIZE, "50");
        queryHints.put(HINT_READONLY, true);
        queryHints.put(HINT_CACHEABLE, true);
        queryHints.put("javax.persistence.fetchgraph", buildEntityGraph());
        return queryHints;
    }

    private EntityGraph<SettlementItem> buildEntityGraph() {
        EntityGraph<SettlementItem> entityGraph = entityManager.createEntityGraph(SettlementItem.class);
        entityGraph.addSubgraph("nomenclator").addSubgraph("medicalPractice");
        Subgraph<MedicalAuthorization> medAuthSubgraph = entityGraph.addSubgraph("medicalAuthorization");
        medAuthSubgraph.addSubgraph("medicalCenter");
        Subgraph<Beneficiary> beneficiarySubgraph = medAuthSubgraph.addSubgraph("beneficiary");
        beneficiarySubgraph.addSubgraph("idType");
        Subgraph<Settlement> settlementSubgraph = entityGraph.addSubgraph("settlement");
        settlementSubgraph.addSubgraph("contract");
        settlementSubgraph.addSubgraph("status");
        Subgraph<Practitioner> practitionerSubgraph = settlementSubgraph.addSubgraph("practitioner");
        practitionerSubgraph.addSubgraph("idType");
        practitionerSubgraph.addSubgraph("medicalSpecialties");
        return entityGraph;
    }

    @Override
    public List<SettlementItem> findAllBySettlementIdAndBeneficiaryCode(long settlementId, String beneficiaryCode) {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SettlementItem> criteriaQuery = criteriaBuilder.createQuery(SettlementItem.class);
        Root<SettlementItem> root = criteriaQuery.from(SettlementItem.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("settlement").get("id"), settlementId),
                criteriaBuilder.equal(root.get("medicalAuthorization").get("beneficiary")
                        .get("beneficiaryCode"), beneficiaryCode)));
        TypedQuery<SettlementItem> typedQuery = entityManager.createQuery(criteriaQuery);
        buildQueryHints().forEach(typedQuery::setHint);
        return typedQuery.getResultList();
    }

}