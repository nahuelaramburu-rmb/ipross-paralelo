package com.capacidad.validationapi.module.beneficiary.dto;

import com.capacidad.validationapi.module.importprocessor.model.ImportObject;
import com.univocity.parsers.annotations.LowerCase;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryImportDTO implements ImportObject {

    @NotEmpty
    @Trim
    @Parsed(field = "beneficiaryCode")
    private String beneficiaryCode;

    @Trim
    @LowerCase
    @Parsed(field = "lastNameAndName")
    private String lastNameAndName;

    @Trim
    @LowerCase
    @Parsed(field = "name")
    private String name;

    @Trim
    @LowerCase
    @Parsed(field = "lastName")
    private String lastName;

    @NotEmpty
    @Trim
    @Parsed(field = "idNumber")
    private String idNumber;

    @NotEmpty
    @Trim
    @LowerCase
    @Parsed(field = "idTypeAlias")
    private String idTypeAlias;

    @Parsed(field = "workIdNumber")
    private String workIdNumber;

    @NotEmpty
    @Trim
    @LowerCase
    @Parsed(field = "gender")
    private String gender;

    @NotEmpty
    @Trim
    @Parsed(field = "birthDate")
    private String birthDate;

    @Trim
    @LowerCase
    @Parsed(field = "maritalStatus")
    private String maritalStatus;

    @NotEmpty
    @Trim
    @LowerCase
    @Parsed(field = "insurancePlans")
    private String insurancePlans;

    @Trim
    @LowerCase
    @Parsed(field = "beneficiaryCategory")
    private String beneficiaryCategory;

    @NotEmpty
    @LowerCase
    @Parsed(field = "city")
    private String city;

    @NotEmpty
    @Trim
    @LowerCase
    @Parsed(field = "province")
    private String province;

    @Trim
    @Parsed(field = "street")
    private String street;

    @Trim
    @Parsed(field = "streetNumber")
    private String streetNumber;

    @Trim
    @Parsed(field = "district")
    private String district;

    @Trim
    @Parsed(field = "apartment")
    private String apartment;

    @Trim
    @Parsed(field = "email")
    private String email;

    @Trim
    @Parsed(field = "phone")
    private String phone;

    @Trim
    @LowerCase
    @Parsed(field = "company")
    private String company;

    @NotEmpty
    @Trim
    @LowerCase
    @Parsed(field = "relationshipType")
    private String relationshipType;

    @Trim
    @Parsed(field = "relatedBeneficiaryCode")
    private String relatedBeneficiaryCode;

}
