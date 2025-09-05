package com.capacidad.validationapi.module.base.model;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DELETION_FILTER;


@MappedSuperclass
@FilterDef(name = DELETION_FILTER, parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = DELETION_FILTER, condition = "deleted = :deleted")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity<I extends Serializable> implements Serializable {

    private static final String UUID_NIL = "00000000-0000-0000-0000-000000000000";

    @Audited
    @JsonIgnore
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt;

    @Audited
    @JsonIgnore
    @LastModifiedDate
    @Column(name = "modified_at")
    protected LocalDateTime modifiedAt;

    @JsonIgnore
    @Column(columnDefinition = "boolean DEFAULT false", nullable = false)
    protected Boolean deleted = false;

    @JsonIgnore
    @Column(name = "deletion_token", columnDefinition = "uuid DEFAULT uuid_nil()", nullable = false)
    protected UUID deletionToken = UUID.fromString(UUID_NIL);

    @Audited
    @JsonIgnore
    @Column(name = "created_by", updatable = false)
    protected String createdBy;

    @Audited
    @JsonIgnore
    @Column(name = "modified_by")
    protected String modifiedBy;
    @Audited

    @JsonIgnore
    @Column(name = "client_id")
    protected String clientId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "base_seq_gen")
    @Column(updatable = false, nullable = false)
    private I id;

    @PrePersist
    protected void onPrePersist() {
        this.setCreatedBy(SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("default_principal"));
        this.setClientId(SecurityUtils.getAuthenticatedAuthorityClientId().orElse("default_client_id"));
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.setModifiedBy(SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("default_principal"));
        this.setClientId(SecurityUtils.getAuthenticatedAuthorityClientId().orElse("default_client_id"));
    }

    public void associateChildObjects() {
        //Not default method implemented
    }

}
