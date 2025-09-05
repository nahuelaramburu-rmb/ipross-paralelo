package com.capacidad.validationapi.module.ruleprocessor.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.ruleprocessor.model.Rule;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuleRepository extends ExtendedJpaRepository<Rule, Long> {
    Optional<Rule> findById(Long id);
}
