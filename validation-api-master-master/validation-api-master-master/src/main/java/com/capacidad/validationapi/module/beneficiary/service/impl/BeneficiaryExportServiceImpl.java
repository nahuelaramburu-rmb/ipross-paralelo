package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryExportService;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryService;
import com.capacidad.validationapi.module.exportprocessor.misc.ExportUtils;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.BaseExportService;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Province;
import com.capacidad.validationapi.module.person.model.Person;
import com.capacidad.validationapi.module.person.model.Phone;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static com.capacidad.validationapi.misc.constant.ModelConstants.CREATED_AT;
import static com.capacidad.validationapi.module.exportprocessor.misc.ExportUtils.*;

@Service
public class BeneficiaryExportServiceImpl extends BaseExportService<Beneficiary, Long> implements BeneficiaryExportService {

    private static final String DOWNLOAD_ORIGIN = "beneficiaries";
    private final LocaleHandler localeHandler;

    @Autowired
    public BeneficiaryExportServiceImpl(LocaleHandler localeHandler,
                                        BeneficiaryService beneficiaryService) {
        super(beneficiaryService);
        this.localeHandler = localeHandler;
    }

    @PostConstruct
    public void initialize() {
        super.initialize(DOWNLOAD_ORIGIN,
                getHeaderNames(),
                Sort.by(CREATED_AT).descending());
    }

    @Override
    public FileDownloadKey generateDownloadKey() {
        return this.getFileDownloadKeyService().generateDownloadKey(DOWNLOAD_ORIGIN);
    }

    private String[] getHeaderNames() {
        Locale locale = Locale.forLanguageTag("es");
        String[] superHeaders = Person.getHeaderNames(localeHandler, locale);
        String[] stringHeaders = new String[7];
        stringHeaders[0] = localeHandler.getLocaleMessage("fields.beneficiaryCode", locale).orElse("");
        stringHeaders[1] = localeHandler.getLocaleMessage("fields.category", locale).orElse("");
        stringHeaders[2] = localeHandler.getLocaleMessage("fields.status", locale).orElse("");
        stringHeaders[3] = localeHandler.getLocaleMessage("fields.paymentMethod", locale).orElse("");
        stringHeaders[4] = localeHandler.getLocaleMessage("fields.company", locale).orElse("");
        stringHeaders[5] = localeHandler.getLocaleMessage("fields.relationshipType", locale).orElse("");
        stringHeaders[6] = localeHandler.getLocaleMessage("fields.insurancePlans", locale).orElse("");
        return ArrayUtils.addAll(superHeaders, stringHeaders);
    }

    @Override
    public String[] toStringArray(Tuple tuple) {
        String[] stringData = new String[21];
        stringData[0] = StringUtils.join(tuple.get(0), COMA, WHITESPACE, tuple.get(1));
        stringData[1] = tuple.get(2).toString();
        stringData[2] = tuple.get(3).toString();
        stringData[3] = tupleValueOrDefault(tuple, 4, DASH);
        stringData[4] = tuple.get(5).toString();
        stringData[5] = tupleIsoToLocalDateOrDefault(tuple, 6, DASH);
        stringData[6] = StringUtils.join(tuple.get(7).toString(), COMA, WHITESPACE, tuple.get(8).toString());
        String street = tupleValueOrDefault(tuple, 9, "");
        String streetNumber = tupleValueOrDefault(tuple, 10, "");
        String apartment = tupleValueOrDefault(tuple, 11, "");
        String district = tupleValueOrDefault(tuple, 12, "");
        String joined = StringUtils.join(street, WHITESPACE, streetNumber, WHITESPACE, apartment, WHITESPACE, district);
        stringData[7] = !StringUtils.isBlank(joined) ? joined : DASH;
        stringData[8] = tupleValueOrDefault(tuple, 13, DASH);
        stringData[9] = tupleValueOrDefault(tuple, 14, DASH);
        stringData[10] = tupleValueOrDefault(tuple, 15, DASH);
        String areaCode = tupleValueOrDefault(tuple, 16, "");
        String phoneNumber = tupleValueOrDefault(tuple, 17, "");
        stringData[11] = StringUtils.join(areaCode, WHITESPACE, phoneNumber);
        stringData[12] = tupleValueOrDefault(tuple, 18, DASH);
        stringData[13] = tupleIsoToLocalDateTimeOrDefault(tuple, 19, DASH);
        stringData[14] = tuple.get(20).toString();
        stringData[15] = tupleValueOrDefault(tuple, 21, DASH);
        stringData[16] = tuple.get(22).toString();
        stringData[17] = tuple.get(23).toString();
        stringData[18] = tupleValueOrDefault(tuple, 24, DASH);
        stringData[19] = tuple.get(25).toString();
        stringData[20] = tupleValueOrDefault(tuple, 26, DASH);
        return stringData;
    }

    @Override
    public List<Selection<?>> buildSelections(Root<Beneficiary> root, CriteriaQuery<Tuple> query, CriteriaBuilder builder) {
        List<Selection<?>> selections = new ArrayList<>();
        selections.add(root.get("lastName"));
        selections.add(root.get("name"));
        selections.add(root.join("idType").get("alias"));
        selections.add(root.get("idNumber"));
        selections.add(root.get("workIdNumber"));
        selections.add(root.get("gender"));
        selections.add(root.get("birthDate"));
        Join<Beneficiary, Address> addressJoin = root.join("address");
        Join<Address, City> cityJoin = addressJoin.join("city");
        Join<City, Province> provinceJoin = cityJoin.join("province");
        selections.add(cityJoin.get("name"));
        selections.add(provinceJoin.get("name"));
        selections.add(addressJoin.get("street"));
        selections.add(addressJoin.get("streetNumber"));
        selections.add(addressJoin.get("apartment"));
        selections.add(addressJoin.get("district"));
        selections.add(root.join("maritalStatus", JoinType.LEFT).get("name"));
        selections.add(root.join("occupation", JoinType.LEFT).get("name"));
        selections.add(root.join("studies", JoinType.LEFT).get("name"));
        Join<Beneficiary, Phone> phoneJoin = root.join("phone", JoinType.LEFT);
        selections.add(phoneJoin.get("areaCode"));
        selections.add(phoneJoin.get("phoneNumber"));
        selections.add(root.get("email"));
        selections.add(root.get("createdAt"));
        selections.add(root.get("beneficiaryCode"));
        selections.add(root.join("beneficiaryCategory", JoinType.LEFT).get("name"));
        selections.add(root.join("status").get("name"));
        selections.add(root.join("paymentMethod").get("name"));
        selections.add(root.join("company", JoinType.LEFT).get("name"));
        selections.add(root.join("relationshipType").get("name"));
        query.groupBy(ExportUtils.selectionsToExpressions(selections));
        selections.add(builder.function("string_agg", String.class, root.join("beneficiaryInsurancePlans")
                .join("insurancePlan").get("name"), builder.literal(COMA)));
        return selections;
    }

}
