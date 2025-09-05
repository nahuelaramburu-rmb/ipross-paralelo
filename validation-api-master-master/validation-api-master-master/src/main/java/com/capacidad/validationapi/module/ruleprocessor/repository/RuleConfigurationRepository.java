package com.capacidad.validationapi.module.ruleprocessor.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleType;
import org.springframework.stereotype.Repository;

import java.util.List;

@TenantFilter
@Repository
public interface RuleConfigurationRepository extends ExtendedJpaRepository<RuleConfiguration, Long> {

    List<RuleConfiguration> findAllByRuleRefRuleType(RuleType ruleType);

}
