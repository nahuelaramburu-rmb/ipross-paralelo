package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "application_login")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_login_seq")
public class Login extends BaseEntity<Long> {

    @Column(nullable = false)
    private String principal;

    @Column(name = "principal_class", nullable = false)
    private String principalClass;

    @Column(name = "tenant_context")
    private String tenantContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_event", nullable = false)
    private LoginEvent loginEvent;

    @Column
    private String agent;

    @Column(name = "ip_address")
    private String ipAddress;

}
