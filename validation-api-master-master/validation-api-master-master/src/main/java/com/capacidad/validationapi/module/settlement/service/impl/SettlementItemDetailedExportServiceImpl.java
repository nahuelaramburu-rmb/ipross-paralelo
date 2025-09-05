package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.BaseExportService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import com.capacidad.validationapi.module.settlement.service.SettlementItemDetailedExportService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static com.capacidad.validationapi.module.exportprocessor.misc.ExportUtils.parseAndBuildSpecificationFromParentSearch;
import static com.capacidad.validationapi.module.exportprocessor.misc.ExportUtils.tupleIsoToLocalDateTimeOrDefault;

@Service
public class SettlementItemDetailedExportServiceImpl extends BaseExportService<SettlementItem, Long> implements SettlementItemDetailedExportService {

    private static final String DOWNLOAD_ORIGIN = "settlements";
    private final LocaleHandler localeHandler;
    private final SpecificationBuilder<SettlementItem, Long> specificationBuilder;
    private final SettlementItemRoleSpecificationBuilder roleSpecificationBuilder;

    @Autowired
    public SettlementItemDetailedExportServiceImpl(LocaleHandler localeHandler,
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
        var locale = Locale.forLanguageTag("es");
        var stringHeaders = new String[20];
        stringHeaders[0] = localeHandler.getLocaleMessage("fields.settlementId", locale).orElse("");
        stringHeaders[1] = localeHandler.getLocaleMessage("fields.status", locale).orElse("");
        stringHeaders[2] = localeHandler.getLocaleMessage("fields.createdAt", locale).orElse("");
        stringHeaders[3] = localeHandler.getLocaleMessage("fields.closedAt", locale).orElse("");
        stringHeaders[4] = localeHandler.getLocaleMessage("fields.contract", locale).orElse("");
        stringHeaders[5] = localeHandler.getLocaleMessage("fields.practitioner", locale).orElse("");
        stringHeaders[6] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringHeaders[7] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringHeaders[8] = localeHandler.getLocaleMessage("fields.beneficiary", locale).orElse("");
        stringHeaders[9] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringHeaders[10] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringHeaders[11] = localeHandler.getLocaleMessage("fields.beneficiaryCode", locale).orElse("");
        stringHeaders[12] = localeHandler.getLocaleMessage("fields.authorizationId", locale).orElse("");
        stringHeaders[13] = localeHandler.getLocaleMessage("fields.createdAt", locale).orElse("");
        stringHeaders[14] = localeHandler.getLocaleMessage("fields.nomenclatorCode", locale).orElse("");
        stringHeaders[15] = localeHandler.getLocaleMessage("fields.medicalPractice", locale).orElse("");
        stringHeaders[16] = localeHandler.getLocaleMessage("fields.quantity", locale).orElse("");
        stringHeaders[17] = localeHandler.getLocaleMessage("fields.unitPrice", locale).orElse("");
        stringHeaders[18] = localeHandler.getLocaleMessage("fields.chargeUnitPrice", locale).orElse("");
        stringHeaders[19] = localeHandler.getLocaleMessage("fields.subtotal", locale).orElse("");
        return stringHeaders;
    }

    @Override
    public String[] toStringArray(Tuple tuple) {
        var stringData = new String[20];
        stringData[0] = tuple.get(0).toString();
        stringData[1] = tuple.get(1).toString();
        stringData[2] = tupleIsoToLocalDateTimeOrDefault(tuple, 2, DASH);
        stringData[3] = tupleIsoToLocalDateTimeOrDefault(tuple, 3, DASH);
        stringData[4] = tuple.get(4).toString();
        stringData[5] = StringUtils.join(tuple.get(5).toString(), COMA, WHITESPACE, tuple.get(6).toString());
        stringData[6] = tuple.get(7).toString();
        stringData[7] = tuple.get(8).toString();
        stringData[8] = StringUtils.join(tuple.get(9).toString(), COMA, WHITESPACE, tuple.get(10).toString());
        stringData[9] = tuple.get(11).toString();
        stringData[10] = tuple.get(12).toString();
        stringData[11] = tuple.get(13).toString();
        stringData[12] = tuple.get(14).toString();
        stringData[13] = tupleIsoToLocalDateTimeOrDefault(tuple, 15, DASH);
        stringData[14] = tuple.get(16).toString();
        stringData[15] = tuple.get(17).toString();
        stringData[16] = tuple.get(18).toString();
        stringData[17] = StringUtils.join("$", tuple.get(19).toString());
        stringData[18] = StringUtils.join("$", tuple.get(20).toString());
        stringData[19] = StringUtils.join("$", tuple.get(21).toString());
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
        selections.add(settlementJoin.join("contract").get("name"));
        Join<Settlement, Practitioner> practitionerJoin = settlementJoin.join("practitioner");
        selections.add(practitionerJoin.get("lastName"));
        selections.add(practitionerJoin.get("name"));
        selections.add(practitionerJoin.join("idType").get("alias"));
        selections.add(practitionerJoin.get("idNumber"));
        Join<SettlementItem, MedicalAuthorization> medAuthJoin = root.join("medicalAuthorization");
        Join<MedicalAuthorization, Beneficiary> beneficiaryJoin = medAuthJoin.join("beneficiary");
        selections.add(beneficiaryJoin.get("lastName"));
        selections.add(beneficiaryJoin.get("name"));
        selections.add(beneficiaryJoin.join("idType").get("alias"));
        selections.add(beneficiaryJoin.get("idNumber"));
        selections.add(beneficiaryJoin.get("beneficiaryCode"));
        Join<SettlementItem, Nomenclator> nomenclatorJoin = root.join("nomenclator");
        Join<Nomenclator, MedicalPractice> medicalPracticeJoin = nomenclatorJoin.join("medicalPractice");
        selections.add(medAuthJoin.get("id"));
        selections.add(medAuthJoin.get("createdAt"));
        selections.add(nomenclatorJoin.get("nomenclatorCode"));
        selections.add(medicalPracticeJoin.get("name"));
        selections.add(root.get("quantity"));
        selections.add(root.get("unitPrice"));
        selections.add(root.get("chargeUnitPrice"));
        selections.add(root.get("subtotal"));
        query.orderBy(builder.desc(settlementJoin.get("id")));
        return selections;
    }

    @Override
    public Specification<SettlementItem> buildSpecification(String search) {
        String parentSearch = parseAndBuildSpecificationFromParentSearch(search, "settlementItems", "settlement");
        return (root, query, builder) -> {
            var tenantPredicate = builder.and(builder.equal(root.get("tenantId"), TenantContext.getTenant()),
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
