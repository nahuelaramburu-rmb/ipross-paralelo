package com.capacidad.validationapi.module.ruleprocessor.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "rule")
@NoArgsConstructor
@Getter
@Setter
public class Rule extends BaseEntity<Long> implements Serializable {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @OneToMany(mappedBy = "ruleRef")
    private List<RulePropertyMetadata> metadata;

    @Column(name = "dto_class_name")
    private String dtoClassName;

    @OneToMany(mappedBy = "ruleRef")
    private Set<RuleConfiguration> ruleConfigurations = new HashSet<>();

}
