package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "application_tenant")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_tenant_seq")
public class Tenant extends BaseEntity<Long> implements Serializable {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "uuid", nullable = false, unique = true)
    private UUID tenantId;

}
