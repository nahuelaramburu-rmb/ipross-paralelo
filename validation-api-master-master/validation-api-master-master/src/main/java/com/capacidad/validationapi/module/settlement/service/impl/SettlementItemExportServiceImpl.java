package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.BaseExportService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import com.capacidad.validationapi.module.settlement.service.SettlementItemExportService;
import com.capacidad.validationapi.module.settlement.service.SettlementItemRoleSpecificationBuilder;
import com.capacidad.validationapi.module.settlement.service.SettlementItemService;
import com.capacidad.validationapi.specification.SpecificationBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static com.capacidad.validationapi.module.exportprocessor.misc.ExportUtils.*;

@Service
public class SettlementItemExportServiceImpl extends BaseExportService<SettlementItem, Long> implements SettlementItemExportService {

    private static final String DOWNLOAD_ORIGIN = "settlements";
    private final LocaleHandler localeHandler;
    private final SpecificationBuilder<SettlementItem, Long> specificationBuilder;
    private final SettlementItemRoleSpecificationBuilder roleSpecificationBuilder;

    @Autowired
    public SettlementItemExportServiceImpl(LocaleHandler localeHandler,
                                           SettlementItemService settlementItemService,
                                           @Qualifier("specBuilder") SpecificationBuilder<SettlementItem, Long> specificationBuilder,
                                           SettlementItemRoleSpecificationBuilder roleSpecificationBuilder) {
        super(settlementItemService);
        this.localeHandler = localeHandler;
        this.specificationBuilder = specificationBuilder;
        this.roleSpecificationBuilder = roleSpecificationBuilder;
    }

    @PostConstruct
    public void initialize() {
        super.initialize(DOWNLOAD_ORIGIN,
                getHeaderNames(),
                null);
    }

    @Override
    public FileDownloadKey generateDownloadKey() {
        return this.getFileDownloadKeyService().generateDownloadKey(DOWNLOAD_ORIGIN);
    }

    private String[] getHeaderNames() {
        Locale locale = Locale.forLanguageTag("es");
        String[] stringHeaders = new String[10];
        stringHeaders[0] = localeHandler.getLocaleMessage("fields.id", locale).orElse("");
        stringHeaders[1] = localeHandler.getLocaleMessage("fields.status", locale).orElse("");
        stringHeaders[2] = localeHandler.getLocaleMessage("fields.createdAt", locale).orElse("");
        stringHeaders[3] = localeHandler.getLocaleMessage("fields.closedAt", locale).orElse("");
        stringHeaders[4] = localeHandler.getLocaleMessage("fields.practitioner", locale).orElse("");
        stringHeaders[5] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringHeaders[6] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringHeaders[7] = localeHandler.getLocaleMessage("fields.contract", locale).orElse("");
        stringHeaders[8] = localeHandler.getLocaleMessage("fields.total", locale).orElse("");
        stringHeaders[9] = localeHandler.getLocaleMessage("fields.nomenclatorCodesQuantity", locale).orElse("");
        return stringHeaders;
    }

    @Override
    public String[] toStringArray(Tuple tuple) {
        String[] stringData = new String[10];
        stringData[0] = tuple.get(0).toString();
        stringData[1] = tuple.get(1).toString();
        stringData[2] = tupleIsoToLocalDateTimeOrDefault(tuple, 2, DASH);
        stringData[3] = tupleIsoToLocalDateTimeOrDefault(tuple, 3, DASH);
        stringData[4] = StringUtils.join(tuple.get(4).toString(), COMA, WHITESPACE, tuple.get(5).toString());
        stringData[5] = tuple.get(6).toString();
        stringData[6] = tuple.get(7).toString();
        stringData[7] = tuple.get(8).toString();
        stringData[8] = StringUtils.join("$", tuple.get(9).toString());
        stringData[9] = tuple.get(10) != null ? buildSettlementItemQuantities(tuple.get(10).toString()) : DASH;
        return stringData;
    }

    @Override
    public List<Selection<?>> buildSelections(Root<SettlementItem> root, CriteriaQuery<Tuple> query, CriteriaBuilder builder) {
        List<Selection<?>> selections = new ArrayList<>();
        Join<SettlementItem, Settlement> settlementJoin = root.join("settlement");
        selections.add(settlementJoin.get("id"));
        selections.add(settlementJoin.join("status").get("name"));
        selections.add(settlementJoin.get("createdAt"));
        selections.add(settlementJoin.get("closedAt"));
        Join<Settlement, Practitioner> practitionerJoin = settlementJoin.join("practitioner");
        selections.add(practitionerJoin.get("lastName"));
        selections.add(practitionerJoin.get("name"));
        selections.add(practitionerJoin.join("idType").get("alias"));
        selections.add(practitionerJoin.get("idNumber"));
        selections.add(settlementJoin.join("contract").get("name"));
        selections.add(settlementJoin.get("total"));
        query.groupBy(selectionsToExpressions(selections));
        query.orderBy(builder.desc(settlementJoin.get("createdAt")));
        Join<SettlementItem, Nomenclator> nomenclatorJoin = root.join("nomenclator");
        selections.add(builder.function("string_agg",
                String.class,
                builder.concat(
                        builder.concat(
                                builder.concat(
                                        nomenclatorJoin.join("medicalPractice").get("name"), DASH),
                                builder.concat(nomenclatorJoin.get("nomenclatorCode"), DASH)),
                        root.get("quantity")).as(String.class),
                builder.literal(COMA)));
        return selections;
    }

    private String buildSettlementItemQuantities(String values) {
        String[] splittedValues = StringUtils.split(values, COMA);
        Map<String, BigInteger> itemQuantities = new HashMap<>();
        for (String val : splittedValues) {
            String[] nomQuantity = StringUtils.split(val, DASH);
            String name = StringUtils.join(nomQuantity[0], WHITESPACE, nomQuantity[1]);
            BigInteger value = new BigInteger(nomQuantity[2]);
            if (itemQuantities.containsKey(name)) {
                itemQuantities.put(name, itemQuantities.get(name).add(value));
            } else {
                itemQuantities.put(name, value);
            }
        }
        return itemQuantities.entrySet().stream()
                .map(v -> StringUtils.join(v.getKey(), " (x", v.getValue(), ")"))
                .collect(Collectors.joining(StringUtils.join(COMA, WHITESPACE)));
    }

    @Override
    public Specification<SettlementItem> buildSpecification(String search) {
        String parentSearch = parseAndBuildSpecificationFromParentSearch(search, "settlementItems", "settlement");
        return (root, query, builder) -> {
            Predicate tenantPredicate = builder.and(builder.equal(root.get("tenantId"), TenantContext.getTenant()),
                    builder.equal(root.get("deleted"), false));
            Optional<Specification<SettlementItem>> joinedSpec = getJoinedSpecifications(parentSearch);
            if (joinedSpec.isPresent())
                return builder.and(tenantPredicate, joinedSpec.get().toPredicate(root, query, builder));
            return tenantPredicate;
        };
    }

    private Optional<Specification<SettlementItem>> getJoinedSpecifications(String search) {
        Optional<Specification<SettlementItem>> spec1 = roleSpecificationBuilder.buildSpecification();
        Optional<Specification<SettlementItem>> spec2 = specificationBuilder.parseAndBuild(search);
        if (spec1.isPresent() && spec2.isPresent())
            return Optional.ofNullable(spec1.get().and(spec2.get()));
        if (spec1.isPresent())
            return spec1;
        return spec2;
    }


}
