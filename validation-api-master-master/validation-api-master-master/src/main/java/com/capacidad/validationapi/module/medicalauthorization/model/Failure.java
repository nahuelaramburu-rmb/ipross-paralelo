package com.capacidad.validationapi.module.medicalauthorization.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table(name = "failure")
@NoArgsConstructor
@Getter
@Setter
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@SequenceGenerator(name = "base_seq_gen", sequenceName = "failure_seq")
public class Failure extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;

    @Column
    private String allowed;

    @Column
    private String current;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_type", nullable = false)
    private FailureType failureType;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JsonNode extra;

}
