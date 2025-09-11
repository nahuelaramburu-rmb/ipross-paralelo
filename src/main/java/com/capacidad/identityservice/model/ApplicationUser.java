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

@Entity
@Table(name = "application_user")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_user_seq")
public class ApplicationUser extends BaseEntity<Long> implements Serializable {

    private static final long serialVersionUID = -2156950437969065955L;

    @Column(nullable = false, unique = true, updatable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "uuid", name = "resource_id", updatable = false)
    private UUID resourceId;

    @Column(columnDefinition = "uuid", unique = true, updatable = false)
    private UUID sub = UUID.randomUUID();

    @Column(nullable = false)
    private String password;

    @Transient
    private String temporaryPassword;

    @Column(nullable = false, name = "email_verified")
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column
    private ChallengeType challengeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_group", nullable = false)
    private Group group;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @Column(name = "verification_otp", updatable = false)
    private Integer verificationOtp;

    @Column(name = "restore_otp")
    private Integer restoreOtp;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApplicationUserContext> contextSet = new HashSet<>();

    @Override
    public void associateChildObjects() {
        this.contextSet.forEach(context -> context.setUser(this));
    }

}
