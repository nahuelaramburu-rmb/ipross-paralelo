package com.capacidad.validationapi.module.ruleprocessor.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "rule_property_metadata")
@NoArgsConstructor
@Getter
@Setter
public class RulePropertyMetadata extends BaseEntity<Long> {

    @Column(name = "rule_property_type", nullable = false)
    private String type;

    @Column(nullable = false)
    private String description;

    @Column(name = "data_identifier", nullable = false)
    private String dataIdentifier;

    @Column(name = "label_key")
    private String labelKey;

    @Column(name = "data_key", nullable = false)
    private String dataKey;

    @Column(name = "data_key_wrapper")
    private String dataKeyWrapper;

    @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean required = false;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule ruleRef;

}
