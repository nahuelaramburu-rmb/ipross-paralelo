package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Usuario base de la aplicación.

@Entity
@Table(name = "application_user")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "user_seq_gen", sequenceName = "application_user_seq", allocationSize = 1)
public class ApplicationUser extends BaseEntity<Long> implements Serializable {

    //versión de serialización de la clase
    //Sirve para garantizar que los objetos serializados sean compatibles entre distintas versiones de la clase
    private static final long serialVersionUID = -2156950437969065955L;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    private Long id;


    @Column(nullable = false, unique = true, updatable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    //identificadores en formato UUID (probablemente para trazabilidad y compatibilidad con OAuth2 / OpenID Connect)
    @Column(columnDefinition = "uuid", name = "resource_id", updatable = false)
    private UUID resourceId;

    @Column(columnDefinition = "uuid", unique = true, updatable = false)
    private UUID sub = UUID.randomUUID();

    @Column(nullable = false)
    private String password;

    @Transient
    private String temporaryPassword;

    //Estado y seguridad
    @Column(nullable = false, name = "email_verified")
    private Boolean emailVerified = false;

    //tipo de challenge de seguridad (ej: OTP, preguntas secretas, etc.).
    @Enumerated(EnumType.STRING)
    @Column
    private ChallengeType challengeType;

    //enum que clasifica al usuario (DEV, PROD, TEST, UAT)
    @Enumerated(EnumType.STRING)
    @Column(name = "user_group", nullable = false)
    private Group group;

    //datos personales del usuario (nombre, apellido, etc.).
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    // referencia a la entidad State (estado funcional del usuario)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    //códigos OTP de validación/restauración.
    @Column(name = "verification_otp", updatable = false)
    private Integer verificationOtp;

    @Column(name = "restore_otp")
    private Integer restoreOtp;

    //conjunto de ApplicationUserContext, que vincula al usuario con tenants / roles / permisos.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApplicationUserContext> contextSet = new HashSet<>();


    //Garantiza la consistencia en la relación bidireccional User ↔ Context
    //Cuando se agregan contextos, este metodo se asegura de setear el user en cada ApplicationUserContext
    @Override
    public void associateChildObjects() {
        this.contextSet.forEach(context -> context.setUser(this));
    }

}
