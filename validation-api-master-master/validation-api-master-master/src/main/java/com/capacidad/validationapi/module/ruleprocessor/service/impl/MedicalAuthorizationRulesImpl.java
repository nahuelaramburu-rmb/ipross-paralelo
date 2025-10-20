package com.capacidad.validationapi.module.ruleprocessor.service.impl;

import com.capacidad.validationapi.module.base.dto.IdAndNameDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationService;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountOnlyDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountRegionAndNomenclatorSetDTO;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.service.MedicalAuthorizationRules;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleConfigurationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class MedicalAuthorizationRulesImpl implements MedicalAuthorizationRules {

    private final RestrictionTypeValidator restrictionTypeValidator;
    private final RuleConfigurationService ruleConfigurationService;
    private final RegionService regionService;
    private final MedicalAuthorizationService medicalAuthorizationService;

    @Autowired
    public MedicalAuthorizationRulesImpl(RestrictionTypeValidator restrictionTypeValidator,
                                         RuleConfigurationService ruleConfigurationService,
                                         RegionService regionService,
                                         MedicalAuthorizationService medicalAuthorizationService) {
        this.restrictionTypeValidator = restrictionTypeValidator;
        this.ruleConfigurationService = ruleConfigurationService;
        this.regionService = regionService;
        this.medicalAuthorizationService = medicalAuthorizationService;
    }

    @Override
    public void maxAmountOfValidationItems(RuleConfiguration ruleConfiguration, MedicalAuthorization medicalAuthorization) {
        Optional<MaxAmountOnlyDTO> dto = ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration);
        dto.ifPresent(i -> {
            int sizeValidationItems = medicalAuthorization.getMedicalAuthorizationItems().size();
            if (sizeValidationItems > i.getMaxAmount()) {
                RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("maxAmountOfValidationItems",
                        i.getMaxAmount().toString(),
                        String.valueOf(sizeValidationItems),
                        null);
                applyRestriction(ruleConfiguration.getRestrictionType(), restrictionMessage, medicalAuthorization);
            }
        });
    }

    @Override
    public void secureAuthorizationBeneficiaryAgeInARegion(RuleConfiguration ruleConfiguration, MedicalAuthorization medicalAuthorization) {
        Optional<MaxAmountRegionAndNomenclatorSetDTO> dto = ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration);
        dto.ifPresent(i -> {
            if (!allNomenclatorsExempt(medicalAuthorization, i))
                checkAgeRegionAndSecureAuthorization(medicalAuthorization, ruleConfiguration, i);
        });
    }

    private boolean allNomenclatorsExempt(MedicalAuthorization medicalAuthorization, MaxAmountRegionAndNomenclatorSetDTO dto) {
        Set<Long> exemptNomenclatorsIds = dto.getNomenclators().stream()
                .map(IdAndNameDTO::getId)
                .collect(Collectors.toUnmodifiableSet());
        return medicalAuthorization.getMedicalAuthorizationItems().size() == medicalAuthorization.getMedicalAuthorizationItems().stream()
                .filter(i -> exemptNomenclatorsIds.contains(i.getNomenclator().getId()))
                .count();
    }

    private void checkAgeRegionAndSecureAuthorization(MedicalAuthorization medicalAuthorization, RuleConfiguration ruleConfiguration, MaxAmountRegionAndNomenclatorSetDTO dto) {
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();
        if (isNotExcepted(beneficiary, dto) && isANonSecureMedicalAuthorizationInstance(medicalAuthorization) &&
                manualQuotaExceeded(medicalAuthorization, dto)) {
            if (dto.getRegion() != null) {
                if (belongsToRegion(medicalAuthorization, dto))
                    buildAndApplyTokenBeneficiaryAgeInARegionRestriction(medicalAuthorization, ruleConfiguration, dto, beneficiary.getAge());
            } else
                buildAndApplyTokenBeneficiaryAgeInARegionRestriction(medicalAuthorization, ruleConfiguration, dto, beneficiary.getAge());
        }
    }

    private boolean isNotExcepted(Beneficiary beneficiary, MaxAmountRegionAndNomenclatorSetDTO dto) {
        return beneficiary.getAge() < dto.getMaxAmount();
    }

    private boolean isANonSecureMedicalAuthorizationInstance(MedicalAuthorization medicalAuthorization) {
        return !isPreMedicalAuthorization(medicalAuthorization) && !isTokenOrQRAuthorization(medicalAuthorization);
    }

    private boolean isPreMedicalAuthorization(MedicalAuthorization medicalAuthorization) {
        return medicalAuthorization.getSelectedPreMedicalAuthorization() != null;
    }

    private boolean isTokenOrQRAuthorization(MedicalAuthorization medicalAuthorization) {
        return medicalAuthorization instanceof QRMedicalAuthorization || medicalAuthorization instanceof OTPMedicalAuthorization;
    }

    private boolean manualQuotaExceeded(MedicalAuthorization medicalAuthorization, MaxAmountRegionAndNomenclatorSetDTO dto) {
        if (dto.getMaxUnsecured() == 0)
            return true;
        AuthorizationType manualIdAuthorizationType = AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_ID_NUMBER.getInstance();
        LocalDateTime dateDiff = LocalDate.now().withDayOfMonth(1).atTime(0, 0, 0, 0);
        int currentValidationAmount = medicalAuthorizationService.getMedicalCenterAuthorizationTypeAmountInPeriod
                (medicalAuthorization, manualIdAuthorizationType, dateDiff);
        return currentValidationAmount >= dto.getMaxUnsecured();
    }

    private boolean belongsToRegion(MedicalAuthorization medicalAuthorization, MaxAmountRegionAndNomenclatorSetDTO dto) {
        Region controlRegion = new Region();
        controlRegion.setId(dto.getRegion().getId());
        return regionService.cityBelongToRegion(controlRegion, medicalAuthorization.getCity());
    }

    private void buildAndApplyTokenBeneficiaryAgeInARegionRestriction(MedicalAuthorization medicalAuthorization, RuleConfiguration ruleConfiguration, MaxAmountRegionAndNomenclatorSetDTO dto, int age) {
        RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("secureAuthorizationBeneficiaryAgeInARegion",
                dto.getMaxAmount().toString(),
                String.valueOf(age),
                null);
        applyRestriction(ruleConfiguration.getRestrictionType(), restrictionMessage, medicalAuthorization);
    }

    private void applyRestriction(RestrictionType restrictionType, RestrictionMessage restrictionMessage, MedicalAuthorization medicalAuthorization) {
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, FailureType.RULE, restrictionMessage);
        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorization);
    }

}
