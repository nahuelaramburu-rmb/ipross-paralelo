package com.capacidad.identityservice.model;

import com.capacidad.identityservice.config.StringToOperationConverter;
import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "application_scope_role")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "scope_role_seq_gen", sequenceName = "application_scope_role_seq", allocationSize = 1)
public class ScopeRole extends BaseEntity<Long> implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "scope_role_seq_gen")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Convert(converter = StringToOperationConverter.class)
    @Column(nullable = false)
    private List<Operation> operations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

}
