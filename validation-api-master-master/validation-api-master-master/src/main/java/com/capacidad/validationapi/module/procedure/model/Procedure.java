package com.capacidad.validationapi.module.procedure.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "procedure")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "procedure_seq", allocationSize = 1)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Relation(collectionRelation = "procedures")
public class Procedure extends BaseTenantEntity<Long> {

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private Set<Message> messages = new HashSet<>();

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private Set<FileTag> fileTags = new HashSet<>();

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Column(length = 1000)
    private String description;

    @Audited
    @Column(name = "file_count")
    private Integer fileCount = 0;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column
    private LocalDate expiration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Column(name = "dtype", insertable = false, updatable = false)
    private String type;

}
