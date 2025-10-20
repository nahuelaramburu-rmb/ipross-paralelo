package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.BaseExportService;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemExportRoleSpecificationBuilder;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemExportService;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
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
public class MedicalAuthorizationItemExportServiceImpl extends BaseExportService<MedicalAuthorizationItem, Long> implements MedicalAuthorizationItemExportService {

    private static final String DOWNLOAD_ORIGIN = "medicalAuthorizations";
    private final LocaleHandler localeHandler;
    private final SpecificationBuilder<MedicalAuthorizationItem, Long> specificationBuilder;
    private final MedicalAuthorizationItemExportRoleSpecificationBuilder roleSpecificationBuilder;

    @Autowired
    public MedicalAuthorizationItemExportServiceImpl(LocaleHandler localeHandler,
                                                     MedicalAuthorizationItemService medicalAuthorizationItemService,
                                                     @Qualifier("specBuilder") SpecificationBuilder<MedicalAuthorizationItem, Long> specificationBuilder,
                                                     MedicalAuthorizationItemExportRoleSpecificationBuilder roleSpecificationBuilder) {
        super(medicalAuthorizationItemService);
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
        String[] stringHeaders = new String[28];
        stringHeaders[0] = localeHandler.getLocaleMessage("fields.id", locale).orElse("");
        stringHeaders[1] = localeHandler.getLocaleMessage("fields.authorizationType", locale).orElse("");
        stringHeaders[2] = localeHandler.getLocaleMessage("fields.status", locale).orElse("");
        stringHeaders[3] = localeHandler.getLocaleMessage("fields.authorizationCondition", locale).orElse("");
        stringHeaders[4] = localeHandler.getLocaleMessage("fields.createdAt", locale).orElse("");
        stringHeaders[5] = localeHandler.getLocaleMessage("fields.practitioner", locale).orElse("");
        stringHeaders[6] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringHeaders[7] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringHeaders[8] = localeHandler.getLocaleMessage("fields.practitionerCode", locale).orElse("");
        stringHeaders[9] = localeHandler.getLocaleMessage("fields.petitioner", locale).orElse("");
        stringHeaders[10] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringHeaders[11] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringHeaders[12] = localeHandler.getLocaleMessage("fields.practitionerCode", locale).orElse("");
        stringHeaders[13] = localeHandler.getLocaleMessage("fields.beneficiary", locale).orElse("");
        stringHeaders[14] = localeHandler.getLocaleMessage("fields.beneficiaryCode", locale).orElse("");
        stringHeaders[15] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringHeaders[16] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringHeaders[17] = localeHandler.getLocaleMessage("fields.birthDate", locale).orElse("");
        stringHeaders[18] = localeHandler.getLocaleMessage("fields.beneficiaryCity", locale).orElse("");
        stringHeaders[19] = localeHandler.getLocaleMessage("fields.paymentMethod", locale).orElse("");
        stringHeaders[20] = localeHandler.getLocaleMessage("fields.company", locale).orElse("");
        stringHeaders[21] = localeHandler.getLocaleMessage("fields.contract", locale).orElse("");
        stringHeaders[22] = localeHandler.getLocaleMessage("fields.medicalCenter", locale).orElse("");
        stringHeaders[23] = localeHandler.getLocaleMessage("fields.city", locale).orElse("");
        stringHeaders[24] = localeHandler.getLocaleMessage("fields.chargeTotal", locale).orElse("");
        stringHeaders[25] = localeHandler.getLocaleMessage("fields.medicalPractice", locale).orElse("");
        stringHeaders[26] = localeHandler.getLocaleMessage("fields.nomenclatorCode", locale).orElse("");
        stringHeaders[27] = localeHandler.getLocaleMessage("fields.quantity", locale).orElse("");
        return stringHeaders;
    }

    @Override
    public String[] toStringArray(Tuple tuple) {
        String[] stringData = new String[28];
        stringData[0] = tuple.get(0).toString();
        stringData[1] = tuple.get(1).toString();
        stringData[2] = tuple.get(2).toString();
        stringData[3] = tupleValueOrDefault(tuple, 3, DASH);
        stringData[4] = tupleIsoToLocalDateTimeOrDefault(tuple, 4, DASH);
        stringData[5] = StringUtils.join(tuple.get(5).toString(), COMA, WHITESPACE, tuple.get(6).toString());
        stringData[6] = tuple.get(7).toString();
        stringData[7] = tuple.get(8).toString();
        stringData[8] = tuple.get(9).toString();
        stringData[9] = StringUtils.join(tuple.get(10).toString(), COMA, WHITESPACE, tuple.get(11).toString());
        stringData[10] = tuple.get(12).toString();
        stringData[11] = tuple.get(13).toString();
        stringData[12] = tuple.get(14).toString();
        stringData[13] = StringUtils.join(tuple.get(15).toString(), COMA, WHITESPACE, tuple.get(16).toString());
        stringData[14] = tuple.get(17).toString();
        stringData[15] = tuple.get(18).toString();
        stringData[16] = tuple.get(19).toString();
        stringData[17] = tupleIsoToLocalDateOrDefault(tuple, 20, DASH);
        stringData[18] = StringUtils.join(tuple.get(21).toString(), COMA, WHITESPACE, tuple.get(22).toString());
        stringData[19] = tuple.get(23).toString();
        stringData[20] = tupleValueOrDefault(tuple, 24, DASH);
        stringData[21] = tuple.get(25).toString();
        stringData[22] = tuple.get(26).toString();
        stringData[23] = StringUtils.join(tuple.get(27).toString(), COMA, WHITESPACE, tuple.get(28).toString());
        stringData[24] = StringUtils.join("$", tuple.get(29).toString());
        stringData[25] = tuple.get(30).toString();
        stringData[26] = tuple.get(31).toString();
        stringData[27] = tuple.get(32).toString();
        return stringData;
    }

    @Override
    public List<Selection<?>> buildSelections(Root<MedicalAuthorizationItem> root, CriteriaQuery<Tuple> query, CriteriaBuilder builder) {
        List<Selection<?>> selections = new ArrayList<>();
        Join<MedicalAuthorizationItem, MedicalAuthorization> medicalAuthorizationJoin = root.join("medicalAuthorization");
        Join<MedicalAuthorizationItem, Nomenclator> nomenclatorJoin = root.join("nomenclator");
        selections.add(medicalAuthorizationJoin.get("id"));
        selections.add(medicalAuthorizationJoin.join("authorizationType").get("name"));
        selections.add(medicalAuthorizationJoin.join("status").get("name"));
        selections.add(medicalAuthorizationJoin.join("authorizationCondition", JoinType.LEFT).get("name"));
        selections.add(medicalAuthorizationJoin.get("createdAt"));
        Join<MedicalAuthorization, Practitioner> practitionerJoin = medicalAuthorizationJoin.join("practitioner");
        selections.add(practitionerJoin.get("lastName"));
        selections.add(practitionerJoin.get("name"));
        selections.add(practitionerJoin.join("idType").get("alias"));
        selections.add(practitionerJoin.get("idNumber"));
        selections.add(practitionerJoin.get("practitionerCode"));
        Join<MedicalAuthorization, Practitioner> petitionerJoin = medicalAuthorizationJoin.join("petitioner");
        selections.add(petitionerJoin.get("lastName"));
        selections.add(petitionerJoin.get("name"));
        selections.add(petitionerJoin.join("idType").get("alias"));
        selections.add(petitionerJoin.get("idNumber"));
        selections.add(petitionerJoin.get("practitionerCode"));
        Join<MedicalAuthorization, Beneficiary> beneficiaryJoin = medicalAuthorizationJoin.join("beneficiary");
        selections.add(beneficiaryJoin.get("lastName"));
        selections.add(beneficiaryJoin.get("name"));
        selections.add(beneficiaryJoin.get("beneficiaryCode"));
        selections.add(beneficiaryJoin.join("idType").get("alias"));
        selections.add(beneficiaryJoin.get("idNumber"));
        selections.add(beneficiaryJoin.get("birthDate"));
        Join<Beneficiary, City> beneficiaryAddressJoin = beneficiaryJoin.join("address");
        Join<Beneficiary, City> beneficiaryCityJoin = beneficiaryAddressJoin.join("city");
        Join<Beneficiary, City> beneficiaryProvinceJoin = beneficiaryCityJoin.join("province");
        selections.add(beneficiaryCityJoin.get("name"));
        selections.add(beneficiaryProvinceJoin.get("name"));
        selections.add(medicalAuthorizationJoin.join("paymentMethod").get("name"));
        selections.add(medicalAuthorizationJoin.join("company", JoinType.LEFT).get("name"));
        selections.add(medicalAuthorizationJoin.join("contract").get("name"));
        selections.add(medicalAuthorizationJoin.join("medicalCenter").get("name"));
        Join<MedicalAuthorization, City> cityJoin = medicalAuthorizationJoin.join("city");
        selections.add(cityJoin.get("name"));
        selections.add(cityJoin.join("province").get("name"));
        selections.add(medicalAuthorizationJoin.get("chargeTotal"));
        selections.add(nomenclatorJoin.join("medicalPractice").get("name"));
        selections.add(nomenclatorJoin.get("nomenclatorCode"));
        selections.add(root.get("quantity"));
        query.orderBy(builder.desc(medicalAuthorizationJoin.get("id")), builder.desc(medicalAuthorizationJoin.get("createdAt")));
        return selections;
    }

    @Override
    public Specification<MedicalAuthorizationItem> buildSpecification(String search) {
        String parentSearch = parseAndBuildSpecificationFromParentSearch(search, "medicalAuthorizationItems", "medicalAuthorization");
        return (root, query, builder) -> {
            Predicate tenantPredicate = builder.and(builder.equal(root.get("tenantId"), TenantContext.getTenant()),
                    builder.equal(root.get("deleted"), false));
            Optional<Specification<MedicalAuthorizationItem>> joinedSpec = getJoinedSpecifications(parentSearch);
            if (joinedSpec.isPresent())
                return builder.and(tenantPredicate, joinedSpec.get().toPredicate(root, query, builder));
            return tenantPredicate;
        };
    }

    private Optional<Specification<MedicalAuthorizationItem>> getJoinedSpecifications(String search) {
        Optional<Specification<MedicalAuthorizationItem>> spec1 = roleSpecificationBuilder.buildSpecification();
        Optional<Specification<MedicalAuthorizationItem>> spec2 = specificationBuilder.parseAndBuild(search);
        if (spec1.isPresent() && spec2.isPresent())
            return Optional.ofNullable(spec1.get().and(spec2.get()));
        if (spec1.isPresent())
            return spec1;
        return spec2;
    }

}
