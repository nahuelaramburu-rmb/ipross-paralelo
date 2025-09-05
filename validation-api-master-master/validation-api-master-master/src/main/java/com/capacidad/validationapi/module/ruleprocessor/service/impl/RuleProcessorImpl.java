package com.capacidad.validationapi.module.ruleprocessor.service.impl;

import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleType;
import com.capacidad.validationapi.module.ruleprocessor.service.MedicalAuthorizationItemRules;
import com.capacidad.validationapi.module.ruleprocessor.service.MedicalAuthorizationRules;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleConfigurationService;
import com.capacidad.validationapi.module.ruleprocessor.service.RuleProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class RuleProcessorImpl implements RuleProcessor {

    private final RuleConfigurationService ruleConfigurationService;
    private final MedicalAuthorizationRules medicalAuthorizationRules;
    private final MedicalAuthorizationItemRules medicalAuthorizationItemRules;

    @Autowired
    public RuleProcessorImpl(RuleConfigurationService ruleConfigurationService,
                             MedicalAuthorizationRules medicalAuthorizationRules,
                             MedicalAuthorizationItemRules medicalAuthorizationItemRules) {
        this.ruleConfigurationService = ruleConfigurationService;
        this.medicalAuthorizationRules = medicalAuthorizationRules;
        this.medicalAuthorizationItemRules = medicalAuthorizationItemRules;
    }

    @Override
    public void applyMedicalAuthorizationRules(MedicalAuthorization medicalAuthorization) {
        try {
            applyAuthorizationRules(medicalAuthorization);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.error("Error trying to execute Authorization Rule Method: {}", e.getMessage());
        }
    }

    @Override
    public void applyMedicalAuthorizationItemRules(MedicalAuthorizationItem medicalAuthorizationItem) {
        try {
            applyAuthorizationItemRules(medicalAuthorizationItem);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.error("Error trying to execute Authorization Rule Method: {}", e.getMessage());
        }
    }

    private void applyAuthorizationItemRules(MedicalAuthorizationItem medicalAuthorizationItem) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Contract contract = medicalAuthorizationItem.getMedicalAuthorization().getContract();
        List<RuleConfiguration> itemRuleConfigurations = ruleConfigurationService.getRuleConfigurationsByContract(RuleType.AUTHORIZATION_ITEM, contract);
        for (RuleConfiguration ruleConfiguration : itemRuleConfigurations) {
            if (shouldApply(ruleConfiguration, medicalAuthorizationItem))
                invokeAuthorizationItemRule(ruleConfiguration, medicalAuthorizationItem);
        }
    }

    private boolean shouldApply(RuleConfiguration ruleConfiguration, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (medicalAuthorizationItem.containsBatchItem())
            return ruleConfiguration.getApplyToBatch() && ruleConfiguration.getActive();
        return ruleConfiguration.getActive();
    }

    private void applyAuthorizationRules(MedicalAuthorization medicalAuthorization) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Contract contract = medicalAuthorization.getContract();
        List<RuleConfiguration> authorizationRules = ruleConfigurationService.getRuleConfigurationsByContract(RuleType.AUTHORIZATION, contract);
        for (RuleConfiguration ruleConfiguration : authorizationRules) {
            if (shouldApply(ruleConfiguration, medicalAuthorization))
                invokeAuthorizationRule(ruleConfiguration, medicalAuthorization);
        }
    }

    private boolean shouldApply(RuleConfiguration ruleConfiguration, MedicalAuthorization medicalAuthorization) {
        if (medicalAuthorization.isBatchAppliedToAllItems())
            return ruleConfiguration.getApplyToBatch() && ruleConfiguration.getActive();
        return ruleConfiguration.getActive();
    }

    private void invokeAuthorizationRule(RuleConfiguration ruleConfiguration, MedicalAuthorization medicalAuthorization) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MedicalAuthorizationRules.class.getMethod(ruleConfiguration.getRuleRef().getName(), RuleConfiguration.class, MedicalAuthorization.class);
        method.invoke(medicalAuthorizationRules, ruleConfiguration, medicalAuthorization);
    }

    private void invokeAuthorizationItemRule(RuleConfiguration ruleConfiguration, MedicalAuthorizationItem medicalAuthorizationItem) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MedicalAuthorizationItemRules.class.getMethod(ruleConfiguration.getRuleRef().getName(), RuleConfiguration.class, MedicalAuthorizationItem.class);
        method.invoke(medicalAuthorizationItemRules, ruleConfiguration, medicalAuthorizationItem);
    }


}
