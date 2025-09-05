package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryImportDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryCategory;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.importprocessor.misc.ImportUtils;
import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.person.model.*;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.HOLDER;

@Log4j2
public abstract class BeneficiaryImportBuilderTemplate {

    public Beneficiary buildBeneficiary(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotValidException, ObjectNotFoundException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode(resolveBeneficiaryCode(importDTO));
        setLastNameAndName(beneficiary, importDTO);
        beneficiary.setBirthDate(resolveDate(importDTO));
        beneficiary.setIdType(findIdType(importDTO, persistedProperties));
        beneficiary.setIdNumber(resolveIdNumber(importDTO));
        beneficiary.setGender(resolveGender(importDTO));
        beneficiary.setWorkIdNumber(resolveWorkIdNumber(importDTO)
                .orElse(null));
        beneficiary.setMaritalStatus(findMaritalStatus(importDTO, persistedProperties)
                .orElse(null));
        beneficiary.setBeneficiaryInsurancePlans(resolveInsurancePlans(importDTO, persistedProperties));
        beneficiary.setAddress(resolveAddress(importDTO, persistedProperties));
        beneficiary.setPhone(resolvePhone(importDTO)
                .orElse(null));
        beneficiary.setEmail(ImportUtils.strip(importDTO.getEmail()).orElse(null));
        RelationshipType relationshipType = findRelationshipType(importDTO, persistedProperties);
        beneficiary.setRelationshipType(relationshipType);
        if (relationshipType.getId().equals(HOLDER.getId()))
            setHolderProperties(beneficiary, importDTO, persistedProperties);
        else
            setRelativeProperties(beneficiary, importDTO);
        beneficiary.associateChildObjects();
        return beneficiary;
    }

    private void setHolderProperties(Beneficiary beneficiary, BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotValidException, ObjectNotFoundException {
        beneficiary.setBeneficiaryCategory(findBeneficiaryCategory(importDTO, persistedProperties).orElse(null));
        beneficiary.setCompany(findCompany(importDTO.getCompany(), persistedProperties).orElse(null));
        resolveAndSetPaymentMethod(beneficiary, persistedProperties);
    }

    private void setRelativeProperties(Beneficiary beneficiary, BeneficiaryImportDTO importDTO) {
        determineRelatedBeneficiary(beneficiary, importDTO);
    }

    protected abstract String resolveBeneficiaryCode(BeneficiaryImportDTO importDTO) throws ObjectNotValidException;

    protected abstract Long resolveIdNumber(BeneficiaryImportDTO importDTO) throws ObjectNotValidException;

    protected abstract void setLastNameAndName(Beneficiary beneficiary, BeneficiaryImportDTO importDTO) throws ObjectNotValidException;

    protected abstract LocalDate resolveDate(BeneficiaryImportDTO importDTO) throws ObjectNotValidException;

    protected abstract Gender resolveGender(BeneficiaryImportDTO importDTO);

    protected abstract IdType findIdType(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties);

    protected abstract RelationshipType findRelationshipType(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties);

    protected abstract Address resolveAddress(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotFoundException;

    protected abstract Set<BeneficiaryInsurancePlan> resolveInsurancePlans(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotValidException;

    protected abstract Optional<Company> findCompany(String companyName, Map<String, Object> persistedProperties) throws ObjectNotValidException;

    protected abstract void resolveAndSetPaymentMethod(Beneficiary beneficiary, Map<String, Object> persistedProperties) throws ObjectNotValidException, ObjectNotFoundException;

    protected abstract Optional<BeneficiaryCategory> findBeneficiaryCategory(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties) throws ObjectNotValidException, ObjectNotFoundException;

    protected abstract Optional<Long> resolveWorkIdNumber(BeneficiaryImportDTO importDTO);

    protected abstract Optional<MaritalStatus> findMaritalStatus(BeneficiaryImportDTO importDTO, Map<String, Object> persistedProperties);

    protected abstract Optional<Phone> resolvePhone(BeneficiaryImportDTO importDTO);

    protected abstract void determineRelatedBeneficiary(Beneficiary beneficiary, BeneficiaryImportDTO importDTO);

    protected abstract void setAuditInfo(Beneficiary beneficiary, ImportProperties properties);

}
