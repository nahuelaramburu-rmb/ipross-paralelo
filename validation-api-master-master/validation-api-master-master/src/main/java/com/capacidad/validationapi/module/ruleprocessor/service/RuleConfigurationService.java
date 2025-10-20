package com.capacidad.validationapi.module.ruleprocessor.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.ruleprocessor.dto.RuleConfigurationDTO;
import com.capacidad.validationapi.module.ruleprocessor.dto.RuleProperty;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleType;
import com.capacidad.validationapi.module.ruleprocessor.projection.RuleProjection;

import java.util.List;
import java.util.Optional;

public interface RuleConfigurationService extends BaseService<RuleConfiguration, RuleConfigurationDTO, Long> {

    List<RuleConfiguration> getRuleConfigurationsByContract(RuleType ruleType, Contract contract);

    List<RuleProjection> getRules();

    <T extends RuleProperty> Optional<T> buildPropertyFromRuleConfiguration(RuleConfiguration ruleConfiguration);

}
