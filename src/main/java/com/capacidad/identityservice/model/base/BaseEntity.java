package com.capacidad.identityservice.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity<I extends Serializable> {

    private static final String UUID_NIL = "00000000-0000-0000-0000-000000000000";
    @JsonIgnore
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   // @SequenceGenerator(name = "sequenceGenerator")
    private I id;

    public void associateChildObjects() {
        //Not default implemented
    }

}
