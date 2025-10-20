package com.capacidad.validationapi.module.ruleprocessor.service.impl;

import com.capacidad.validationapi.module.base.dto.IdAndNameDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.medicalcoverage.reference.RestrictionTypeReference;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.ruleprocessor.dto.DayOnlyDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.TimedNomenclatorDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.TimedNomenclatorSetDTO;
import com.capacidad.validationapi.module.ruleprocessor.model.Rule;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DASH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MedicalAuthorizationItemRulesImplTest {

    @Mock
    private MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Mock
    private RestrictionTypeValidator restrictionTypeValidator;

    @Mock
    private RuleConfigurationService ruleConfigurationService;

    @InjectMocks
    private MedicalAuthorizationItemRulesImpl medicalAuthorizationItemRules;

    @Test
    public void testBeneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriodAppliesRestriction() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);
        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorCode("420101");
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        DayOnlyDTO dayOnlyDTO = new DayOnlyDTO();
        dayOnlyDTO.setDays(1);

        MedicalAuthorizationItem medicalAuthorizationItem1 = new MedicalAuthorizationItem();
        medicalAuthorizationItem1.setId(2L);
        MedicalAuthorization medicalAuthorization1 = new MedicalAuthorization();
        medicalAuthorization1.setId(2L);
        medicalAuthorizationItem1.setMedicalAuthorization(medicalAuthorization1);

        List<MedicalAuthorizationItem> medicalAuthorizationItems = new ArrayList<>();
        medicalAuthorizationItems.add(medicalAuthorizationItem);
        medicalAuthorizationItems.add(medicalAuthorizationItem1);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(dayOnlyDTO));
        when(medicalAuthorizationItemService.getBeneficiaryPractitionerAuthorizationItemsInPeriod
                (any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(medicalAuthorizationItems);

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod",
                "1",
                String.valueOf(medicalAuthorizationItems.size() + 1),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.RULE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalAuthorizationItemRules.beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod(ruleConfiguration, medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(expectedRestriction, medicalAuthorizationItem);
    }

    @Test
    public void testBeneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriodDoNotAppliesRestriction() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        DayOnlyDTO dayOnlyDTO = new DayOnlyDTO();
        dayOnlyDTO.setDays(1);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(dayOnlyDTO));
        when(medicalAuthorizationItemService.getBeneficiaryPractitionerAuthorizationItemsInPeriod
                (any(MedicalAuthorizationItem.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        medicalAuthorizationItemRules.beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod(ruleConfiguration, medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testTimedNomenclatorsDoNotAppliesRestrictionWhenDifferentNomenclator() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        TimedNomenclatorSetDTO timedNomenclatorSetDTO = new TimedNomenclatorSetDTO();
        TimedNomenclatorDTO timedNomenclatorDTO = new TimedNomenclatorDTO();
        IdAndNameDTO<Long> nomDTO = new IdAndNameDTO<>();
        nomDTO.setId(2L);
        timedNomenclatorDTO.setNomenclator(nomDTO);
        timedNomenclatorSetDTO.getTimedNomenclators().add(timedNomenclatorDTO);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(timedNomenclatorSetDTO));

        medicalAuthorizationItemRules.timedNomenclators(ruleConfiguration, medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testTimedNomenclatorsDoNotAppliesRestrictionWhenValidTimeRange() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        TimedNomenclatorSetDTO timedNomenclatorSetDTO = new TimedNomenclatorSetDTO();
        TimedNomenclatorDTO timedNomenclatorDTO = new TimedNomenclatorDTO();
        IdAndNameDTO<Long> nomDTO = new IdAndNameDTO<>();
        nomDTO.setId(1L);
        timedNomenclatorDTO.setNomenclator(nomDTO);
        timedNomenclatorDTO.setTimeFrom(LocalTime.now().minusHours(1));
        timedNomenclatorDTO.setTimeTo(LocalTime.now().plusHours(1));
        timedNomenclatorSetDTO.getTimedNomenclators().add(timedNomenclatorDTO);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(timedNomenclatorSetDTO));

        medicalAuthorizationItemRules.timedNomenclators(ruleConfiguration, medicalAuthorizationItem);

        verify(restrictionTypeValidator, never()).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testTimedNomenclatorsAppliesRestrictionWhenBeforeExpectedTimeRange() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        TimedNomenclatorSetDTO timedNomenclatorSetDTO = new TimedNomenclatorSetDTO();
        TimedNomenclatorDTO timedNomenclatorDTO = new TimedNomenclatorDTO();
        IdAndNameDTO<Long> nomDTO = new IdAndNameDTO<>();
        nomDTO.setId(1L);
        timedNomenclatorDTO.setNomenclator(nomDTO);
        timedNomenclatorDTO.setTimeFrom(LocalTime.now().plusHours(1));
        timedNomenclatorDTO.setTimeTo(LocalTime.now().plusHours(2));
        timedNomenclatorSetDTO.getTimedNomenclators().add(timedNomenclatorDTO);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(timedNomenclatorSetDTO));

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("timedNomenclators",
                StringUtils.join(timedNomenclatorDTO.getTimeFrom().toString(), DASH, timedNomenclatorDTO.getTimeTo().toString()),
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.RULE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalAuthorizationItemRules.timedNomenclators(ruleConfiguration, medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

    @Test
    public void testTimedNomenclatorsAppliesRestrictionWhenAfterExpectedTimeRange() {
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        medicalAuthorizationItem.setNomenclator(nomenclator);

        Rule rule = new Rule();
        rule.setDescription("description");

        RestrictionType restrictionType = new RestrictionType();
        restrictionType.setId(RestrictionTypeReference.REJECTION.getId());

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setRuleRef(rule);
        ruleConfiguration.setRestrictionType(restrictionType);

        TimedNomenclatorSetDTO timedNomenclatorSetDTO = new TimedNomenclatorSetDTO();
        TimedNomenclatorDTO timedNomenclatorDTO = new TimedNomenclatorDTO();
        IdAndNameDTO<Long> nomDTO = new IdAndNameDTO<>();
        nomDTO.setId(1L);
        timedNomenclatorDTO.setNomenclator(nomDTO);
        timedNomenclatorDTO.setTimeFrom(LocalTime.now().minusHours(2));
        timedNomenclatorDTO.setTimeTo(LocalTime.now().minusHours(1));
        timedNomenclatorSetDTO.getTimedNomenclators().add(timedNomenclatorDTO);

        when(ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration)).thenReturn(Optional.of(timedNomenclatorSetDTO));

        Restriction expectedRestriction = new Restriction();
        RestrictionMessage expectedRestrictionMessage = new RestrictionMessage();

        when(restrictionTypeValidator.buildRestrictionMessage("timedNomenclators",
                StringUtils.join(timedNomenclatorDTO.getTimeFrom().toString(), DASH, timedNomenclatorDTO.getTimeTo().toString()),
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                null)).thenReturn(expectedRestrictionMessage);

        when(restrictionTypeValidator.buildRestriction(
                restrictionType,
                FailureType.RULE,
                expectedRestrictionMessage)).thenReturn(expectedRestriction);

        medicalAuthorizationItemRules.timedNomenclators(ruleConfiguration, medicalAuthorizationItem);

        verify(restrictionTypeValidator, times(1)).applyRestriction(any(Restriction.class), any(MedicalAuthorizationItem.class));
    }

}
