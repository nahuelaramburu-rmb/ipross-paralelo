package com.capacidad.validationapi.module.ruleprocessor.service.impl;

import com.capacidad.validationapi.module.base.dto.IdAndNameDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationService;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountOnlyDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountRegionAndNomenclatorSetDTO;
import com.capacidad.validationapi.module.ruleprocessor.model.Rule;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleConfigurationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationRulesImplTest {

    @Mock
    private RestrictionTypeValidator restrictionTypeValidator;

    @Mock
    private RuleConfigurationService ruleConfigurationService;

    @Mock
    private RegionService regionService;

    @Mock
    private MedicalAuthorizationService medicalAuthorizationService;

    @InjectMocks
    private MedicalAuthorizationRulesImpl medicalAuthorizationRules;

    @Test
    public void testMaxAmountOfValidationItemsAppliesRestriction() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());
        medicalAuthorization.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountOnlyDTO maxAmountOnlyDTO = new MaxAmountOnlyDTO();
        maxAmountOnlyDTO.setMaxAmount(1L);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(maxAmountOnlyDTO));

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("maxAmountOfValidationItems",
                maxAmountOnlyDTO.getMaxAmount().toString(),
                String.valueOf(medicalAuthorization.getMedicalAuthorizationItems().size()),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.RULE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalAuthorizationRules.maxAmountOfValidationItems(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorization);
    }

    @Test
    public void testMaxAmountOfValidationItemsDoNotAppliesRestriction() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.getMedicalAuthorizationItems().add(new MedicalAuthorizationItem());

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountOnlyDTO maxAmountOnlyDTO = new MaxAmountOnlyDTO();
        maxAmountOnlyDTO.setMaxAmount(1L);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(maxAmountOnlyDTO));
        medicalAuthorizationRules.maxAmountOfValidationItems(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorization.class));
    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionDoNotAppliesRestrictionWhenAllNomenclatorsExempt() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        IdAndNameDTO<Long> exemptNomenclator2 = new IdAndNameDTO<>();
        exemptNomenclator2.setId(nomenclator2.getId());
        secureDTO.getNomenclators().add(exemptNomenclator);
        secureDTO.getNomenclators().add(exemptNomenclator2);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorization.class));

    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionAppliesRestrictionWhenInvalidAgeNullRegionAndRegularAuthorization() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(30));
        medicalAuthorization.setBeneficiary(beneficiary);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        secureDTO.setMaxAmount(60L);
        secureDTO.setMaxUnsecured(0L);
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        secureDTO.getNomenclators().add(exemptNomenclator);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("secureAuthorizationBeneficiaryAgeInARegion",
                secureDTO.getMaxAmount().toString(),
                String.valueOf(beneficiary.getAge()),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.RULE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorization);

    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionDoNotAppliesRestrictionWhenValidAgeNullRegionAndRegularAuthorization() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(60));
        medicalAuthorization.setBeneficiary(beneficiary);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        secureDTO.setMaxAmount(60L);
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        secureDTO.getNomenclators().add(exemptNomenclator);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorization.class));

    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionDoNotAppliesRestrictionWhenInvalidAgeNullRegionAndSecureAuthorization() {
        QRMedicalAuthorization medicalAuthorization = new QRMedicalAuthorization();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(30));
        medicalAuthorization.setBeneficiary(beneficiary);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        secureDTO.setMaxAmount(60L);
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        secureDTO.getNomenclators().add(exemptNomenclator);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorization.class));

    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionAppliesRestrictionWhenInvalidAgeInsideRegionAndRegularAuthorization() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(new City());
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(30));
        medicalAuthorization.setBeneficiary(beneficiary);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        secureDTO.setMaxAmount(60L);
        secureDTO.setMaxUnsecured(0L);
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        IdAndNameDTO<Long> region = new IdAndNameDTO<>();
        region.setId(1L);
        secureDTO.getNomenclators().add(exemptNomenclator);
        secureDTO.setRegion(region);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));
        when(regionService.cityBelongToRegion(any(Region.class), any(City.class))).thenReturn(true);

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("secureAuthorizationBeneficiaryAgeInARegion",
                secureDTO.getMaxAmount().toString(),
                String.valueOf(beneficiary.getAge()),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.RULE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorization);

    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionDoNotAppliesRestrictionWhenInvalidAgeOutsideRegionAndRegularAuthorization() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(new City());
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(30));
        medicalAuthorization.setBeneficiary(beneficiary);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        secureDTO.setMaxAmount(60L);
        secureDTO.setMaxUnsecured(0L);
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        IdAndNameDTO<Long> region = new IdAndNameDTO<>();
        region.setId(1L);
        secureDTO.getNomenclators().add(exemptNomenclator);
        secureDTO.setRegion(region);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));
        when(regionService.cityBelongToRegion(any(Region.class), any(City.class))).thenReturn(false);

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorization.class));
    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionDoNotAppliesRestrictionWhenInvalidAgeButNotExceededQuota() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(new City());
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(30));
        medicalAuthorization.setBeneficiary(beneficiary);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);
        medicalAuthorization.setMedicalCenter(new MedicalCenter());

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        secureDTO.setMaxAmount(60L);
        secureDTO.setMaxUnsecured(2L);
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        secureDTO.getNomenclators().add(exemptNomenclator);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));
        when(medicalAuthorizationService.getMedicalCenterAuthorizationTypeAmountInPeriod(any(MedicalAuthorization.class),
                any(AuthorizationType.class), any(LocalDateTime.class))).thenReturn(1);

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorization.class));
    }

    @Test
    public void testSecureAuthorizationBeneficiaryAgeInARegionAppliesRestrictionWhenInvalidAgeAndQuotaExceeded() {
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setCity(new City());
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBirthDate(LocalDate.now().minusYears(30));
        medicalAuthorization.setBeneficiary(beneficiary);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setNomenclator(nomenclator2);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);
        medicalAuthorization.setMedicalCenter(new MedicalCenter());

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        MaxAmountRegionAndNomenclatorSetDTO secureDTO = new MaxAmountRegionAndNomenclatorSetDTO();
        secureDTO.setMaxAmount(60L);
        secureDTO.setMaxUnsecured(2L);
        IdAndNameDTO<Long> exemptNomenclator = new IdAndNameDTO<>();
        exemptNomenclator.setId(nomenclator.getId());
        secureDTO.getNomenclators().add(exemptNomenclator);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(secureDTO));
        when(medicalAuthorizationService.getMedicalCenterAuthorizationTypeAmountInPeriod(any(MedicalAuthorization.class),
                any(AuthorizationType.class), any(LocalDateTime.class))).thenReturn(3);

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("secureAuthorizationBeneficiaryAgeInARegion",
                secureDTO.getMaxAmount().toString(),
                String.valueOf(beneficiary.getAge()),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.RULE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalAuthorizationRules.secureAuthorizationBeneficiaryAgeInARegion(ruleConfiguration, medicalAuthorization);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorization);
    }

}
