package com.capacidad.validationapi.module.ruleprocessor.service.impl;

import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import com.capacidad.validationapi.module.ruleprocessor.dto.DayOnlyDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.TimedNomenclatorDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.TimedNomenclatorSetDTO;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.service.MedicalAuthorizationItemRules;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DASH;

@Service
public class MedicalAuthorizationItemRulesImpl implements MedicalAuthorizationItemRules {

    private final MedicalAuthorizationItemService medicalAuthorizationItemService;
    private final RestrictionTypeValidator restrictionTypeValidator;
    private final RuleConfigurationService ruleConfigurationService;

    @Autowired
    public MedicalAuthorizationItemRulesImpl(MedicalAuthorizationItemService medicalAuthorizationItemService,
                                             RestrictionTypeValidator restrictionTypeValidator,
                                             RuleConfigurationService ruleConfigurationService) {
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
        this.restrictionTypeValidator = restrictionTypeValidator;
        this.ruleConfigurationService = ruleConfigurationService;
    }


    @Override
    public void beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod(RuleConfiguration ruleConfiguration, MedicalAuthorizationItem medicalAuthorizationItem) {
        Optional<DayOnlyDTO> dto = ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration);
        dto.ifPresent(i -> {
            LocalDateTime dateDiff = LocalDate.now()
                    .minusDays(i.getDays() - 1L).atTime(0, 0, 0, 0);
            List<MedicalAuthorizationItem> currentValidationItems = medicalAuthorizationItemService
                    .getBeneficiaryPractitionerAuthorizationItemsInPeriod(medicalAuthorizationItem, dateDiff);
            int currentValidationItemAmount = currentValidationItems.size() + 1;
            if (currentValidationItemAmount > 1) {
                RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage(
                        "beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod",
                        i.getDays().toString(),
                        String.valueOf(currentValidationItemAmount),
                        buildBeneficiaryUniqueMedicalPracticeExtra(dateDiff, currentValidationItems));
                applyRestriction(ruleConfiguration.getRestrictionType(), restrictionMessage, medicalAuthorizationItem);
            }
        });
    }

    private RestrictionMessageExtra buildBeneficiaryUniqueMedicalPracticeExtra(LocalDateTime from, List<MedicalAuthorizationItem> items) {
        List<Object> idList = items.stream()
                .map(i -> i.getMedicalAuthorization().getId())
                .collect(Collectors.toUnmodifiableList());
        return restrictionTypeValidator.buildRestrictionMessageExtra(RestrictionMessageExtraType.AUTHORIZATION_ID,
                idList,
                from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    @Override
    public void timedNomenclators(RuleConfiguration ruleConfiguration, MedicalAuthorizationItem medicalAuthorizationItem) {
        Optional<TimedNomenclatorSetDTO> dto = ruleConfigurationService.buildPropertyFromRuleConfiguration(ruleConfiguration);
        dto.ifPresent(i -> {
            Optional<TimedNomenclatorDTO> timedNomenclatorDTO = i.getTimedNomenclators().stream()
                    .filter(t -> t.getNomenclator().getId().equals(medicalAuthorizationItem.getNomenclator().getId()))
                    .findFirst();
            timedNomenclatorDTO.ifPresent(t -> {
                Duration durationFrom = Duration.between(t.getTimeFrom(), LocalTime.now());
                Duration durationTo = Duration.between(LocalTime.now(), t.getTimeTo());
                if (isNegativeOrZeroDuration(durationFrom) || isNegativeOrZeroDuration(durationTo)) {
                    RestrictionMessage restrictionMessage = restrictionTypeValidator.buildRestrictionMessage("timedNomenclators",
                            StringUtils.join(t.getTimeFrom().toString(), DASH, t.getTimeTo().toString()),
                            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                            null);
                    applyRestriction(ruleConfiguration.getRestrictionType(), restrictionMessage, medicalAuthorizationItem);
                }
            });
        });
    }

    private boolean isNegativeOrZeroDuration(Duration duration) {
        return duration.isNegative() || duration.isZero();
    }

    private void applyRestriction(RestrictionType restrictionType, RestrictionMessage restrictionMessage, MedicalAuthorizationItem medicalAuthorizationItem) {
        Restriction restriction = restrictionTypeValidator.buildRestriction(restrictionType, FailureType.RULE, restrictionMessage);
        restrictionTypeValidator.applyRestriction(restriction, medicalAuthorizationItem);
    }

}
