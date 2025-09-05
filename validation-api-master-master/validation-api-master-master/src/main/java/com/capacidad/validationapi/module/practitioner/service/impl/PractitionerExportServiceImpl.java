package com.capacidad.validationapi.module.practitioner.service.impl;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.exportprocessor.misc.ExportUtils;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.BaseExportService;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Province;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.person.model.Person;
import com.capacidad.validationapi.module.person.model.Phone;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerExportService;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
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
public class PractitionerExportServiceImpl extends BaseExportService<Practitioner, Long> implements PractitionerExportService {

    private static final String DOWNLOAD_ORIGIN = "practitioners";
    private final LocaleHandler localeHandler;

    @Autowired
    public PractitionerExportServiceImpl(LocaleHandler localeHandler,
                                         PractitionerService practitionerService) {
        super(practitionerService);
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
        stringHeaders[0] = localeHandler.getLocaleMessage("fields.practitionerCode", locale).orElse("");
        stringHeaders[1] = localeHandler.getLocaleMessage("fields.category", locale).orElse("");
        stringHeaders[2] = localeHandler.getLocaleMessage("fields.status", locale).orElse("");
        stringHeaders[3] = localeHandler.getLocaleMessage("fields.medicalSpecialty", locale).orElse("");
        stringHeaders[4] = localeHandler.getLocaleMessage("fields.organization", locale).orElse("");
        stringHeaders[5] = localeHandler.getLocaleMessage("fields.medicalCenter", locale).orElse("");
        stringHeaders[6] = localeHandler.getLocaleMessage("fields.contract", locale).orElse("");
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
        stringData[17] = String.join(", ", tupleStringArrayToSet(tupleValueOrDefault(tuple, 23, DASH), COMA));
        stringData[18] = String.join(", ", tupleStringArrayToSet(tupleValueOrDefault(tuple, 24, DASH), COMA));
        stringData[19] = String.join(", ", tupleStringArrayToSet(tupleValueOrDefault(tuple, 25, DASH), COMA));
        stringData[20] = String.join(", ", tupleStringArrayToSet(tupleValueOrDefault(tuple, 26, DASH), COMA));
        return stringData;
    }

    @Override
    public List<Selection<?>> buildSelections(Root<Practitioner> root, CriteriaQuery<Tuple> query, CriteriaBuilder builder) {
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
        selections.add(root.get("practitionerCode"));
        selections.add(root.join("practitionerCategory", JoinType.LEFT).get("name"));
        selections.add(root.join("status").get("name"));
        query.groupBy(ExportUtils.selectionsToExpressions(selections));
        Join<Practitioner, MedicalSpecialty> medicalSpecialtyJoin = root.join("medicalSpecialties", JoinType.LEFT);
        selections.add(builder.function("string_agg", String.class, medicalSpecialtyJoin.get("name"), builder.literal(COMA)));
        Join<Practitioner, MedicalRegistration> medicalRegistrationJoin = root.join("medicalRegistrations", JoinType.LEFT);
        selections.add(builder.function("string_agg",
                String.class,
                builder.concat(builder.concat(medicalRegistrationJoin.get("organization").get("name"), WHITESPACE),
                        medicalRegistrationJoin.get("registrationCode")),
                builder.literal(COMA)));
        Join<Practitioner, MedicalCenter> medicalCenterJoin = root.join("medicalCenters", JoinType.LEFT);
        selections.add(builder.function("string_agg", String.class, medicalCenterJoin.get("name"), builder.literal(COMA)));
        Join<Practitioner, Contract> practitionerContractJoin = root.join("contracts", JoinType.LEFT);
        selections.add(builder.function("string_agg", String.class, practitionerContractJoin.get("name"), builder.literal(COMA)));
        return selections;
    }

}
