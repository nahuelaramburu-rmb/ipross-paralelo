package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.BaseExportService;
import com.capacidad.validationapi.module.medicine.model.Medicine;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.service.PrescriptionItemExportService;
import com.capacidad.validationapi.module.prescription.service.PrescriptionItemRoleSpecificationBuilder;
import com.capacidad.validationapi.module.prescription.service.PrescriptionItemService;
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
import static com.capacidad.validationapi.module.exportprocessor.misc.ExportUtils.*;

@Service
public class PrescriptionItemExportServiceImpl extends BaseExportService<PrescriptionItem, Long> implements PrescriptionItemExportService {

    private static final String DOWNLOAD_ORIGIN = "prescriptions";
    private final LocaleHandler localeHandler;
    private final SpecificationBuilder<PrescriptionItem, Long> specificationBuilder;
    private final PrescriptionItemRoleSpecificationBuilder prescriptionItemRoleSpecificationBuilder;

    @Autowired
    public PrescriptionItemExportServiceImpl(LocaleHandler localeHandler,
                                             PrescriptionItemService prescriptionItemService,
                                             @Qualifier("specBuilder") SpecificationBuilder<PrescriptionItem, Long> specificationBuilder,
                                             PrescriptionItemRoleSpecificationBuilder prescriptionItemRoleSpecificationBuilder) {
        super(prescriptionItemService);
        this.localeHandler = localeHandler;
        this.specificationBuilder = specificationBuilder;
        this.prescriptionItemRoleSpecificationBuilder = prescriptionItemRoleSpecificationBuilder;
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
        String[] stringheaders = new String[17];
        stringheaders[0] = localeHandler.getLocaleMessage("fields.id", locale).orElse("");
        stringheaders[1] = localeHandler.getLocaleMessage("fields.status", locale).orElse("");
        stringheaders[2] = localeHandler.getLocaleMessage("fields.createdAt", locale).orElse("");
        stringheaders[3] = localeHandler.getLocaleMessage("fields.preAuthorized", locale).orElse("");
        stringheaders[4] = localeHandler.getLocaleMessage("fields.practitioner", locale).orElse("");
        stringheaders[5] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringheaders[6] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringheaders[7] = localeHandler.getLocaleMessage("fields.beneficiary", locale).orElse("");
        stringheaders[8] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringheaders[9] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringheaders[10] = localeHandler.getLocaleMessage("fields.diseaseCode", locale).orElse("");
        stringheaders[11] = localeHandler.getLocaleMessage("fields.product", locale).orElse("");
        stringheaders[12] = localeHandler.getLocaleMessage("fields.presentation", locale).orElse("");
        stringheaders[13] = localeHandler.getLocaleMessage("fields.treatmentDays", locale).orElse("");
        stringheaders[14] = localeHandler.getLocaleMessage("fields.dailyDosage", locale).orElse("");
        stringheaders[15] = localeHandler.getLocaleMessage("fields.quantity", locale).orElse("");
        stringheaders[16] = localeHandler.getLocaleMessage("fields.exchangeIds", locale).orElse("");

        return stringheaders;
    }

    @Override
    public String[] toStringArray(Tuple tuple) {

        String[] stringData = new String[17];
        stringData[0] = tuple.get(0).toString();
        stringData[1] = tuple.get(1).toString();
        stringData[2] = tupleIsoToLocalDateTimeOrDefault(tuple, 2, DASH);
        stringData[3] = (boolean) tuple.get(3) ? "Si" : "No";
        stringData[4] = StringUtils.join(tuple.get(4).toString(), COMA, WHITESPACE, tuple.get(5).toString());
        stringData[5] = tuple.get(6).toString();
        stringData[6] = tuple.get(7).toString();
        stringData[7] = StringUtils.join(tuple.get(8).toString(), COMA, WHITESPACE, tuple.get(9).toString());
        stringData[8] = tuple.get(10).toString();
        stringData[9] = tuple.get(11).toString();
        stringData[10] = tuple.get(12).toString();
        stringData[11] = tuple.get(13).toString();
        stringData[12] = tuple.get(14).toString();
        stringData[13] = tuple.get(15) == null ? DASH : tuple.get(15).toString();
        stringData[14] = tuple.get(16) == null ? DASH : tuple.get(16).toString();
        stringData[15] = tuple.get(17).toString();
        stringData[16] = tuple.get(18) == null ? DASH : tuple.get(18).toString();

        return stringData;

    }

    @Override
    public List<Selection<?>> buildSelections(Root<PrescriptionItem> root, CriteriaQuery<Tuple> query, CriteriaBuilder builder) {
        List<Selection<?>> selections = new ArrayList<>();

        Join<PrescriptionItem, Prescription> prescriptionJoin = root.join("prescription");
        selections.add(prescriptionJoin.get("id"));
        selections.add(prescriptionJoin.join("status").get("name"));

        selections.add(prescriptionJoin.get("createdAt"));
        selections.add(prescriptionJoin.get("preAuthorized"));

        Join<Prescription, Practitioner> practitionerJoin = prescriptionJoin.join("practitioner");
        selections.add(practitionerJoin.get("lastName"));
        selections.add(practitionerJoin.get("name"));
        selections.add(practitionerJoin.join("idType").get("alias"));
        selections.add(practitionerJoin.get("idNumber"));

        Join<Prescription, Beneficiary> beneficiaryJoin = prescriptionJoin.join("beneficiary");
        selections.add(beneficiaryJoin.get("lastName"));
        selections.add(beneficiaryJoin.get("name"));
        selections.add(beneficiaryJoin.join("idType").get("alias"));
        selections.add(beneficiaryJoin.get("idNumber"));

        Join<PrescriptionItem, ICD10Disease> diseaseJoin = root.join("disease");
        selections.add(diseaseJoin.get("code"));

        Join<PrescriptionItem, Medicine> medicineJoin = root.join("medicine");
        selections.add(medicineJoin.get("product"));
        selections.add(medicineJoin.get("presentation"));

        selections.add(root.get("treatmentDays"));
        selections.add(root.get("dailyDosage"));
        selections.add(root.get("quantity"));

        query.groupBy(selectionsToExpressions(selections));
        query.orderBy(builder.desc(prescriptionJoin.get("id")), builder.desc(prescriptionJoin.get("createdAt")));
        selections.add(builder.function("string_agg",
                String.class,
                builder.concat(prescriptionJoin.join("exchangeId", JoinType.LEFT), ""),
                builder.literal(COMA)));
        return selections;
    }

    @Override
    public Specification<PrescriptionItem> buildSpecification(String search) {
        String parentSearch = parseAndBuildSpecificationFromParentSearch(search, "prescriptionItems", "prescription");
        return (root, query, builder) -> {
            Predicate tenantPredicate = builder.and(builder.equal(root.get("tenantId"), TenantContext.getTenant()),
                    builder.equal(root.get("deleted"), false));
            Optional<Specification<PrescriptionItem>> joinedSpec = getJoinedSpecifications(parentSearch);
            if (joinedSpec.isPresent())
                return builder.and(tenantPredicate, joinedSpec.get().toPredicate(root, query, builder));
            return tenantPredicate;
        };
    }

    private Optional<Specification<PrescriptionItem>> getJoinedSpecifications(String search) {
        Optional<Specification<PrescriptionItem>> spec1 = prescriptionItemRoleSpecificationBuilder.buildSpecification();
        Optional<Specification<PrescriptionItem>> spec2 = specificationBuilder.parseAndBuild(search);
        if (spec1.isPresent() && spec2.isPresent())
            return Optional.ofNullable(spec1.get().and(spec2.get()));
        if (spec1.isPresent())
            return spec1;
        return spec2;
    }

}
