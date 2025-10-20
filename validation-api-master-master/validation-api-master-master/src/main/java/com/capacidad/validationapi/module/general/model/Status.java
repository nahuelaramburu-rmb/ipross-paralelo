package com.capacidad.validationapi.module.general.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "status", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "status_scope_id"}),
})
@NoArgsConstructor
@Getter
@Setter
public class Status extends BaseEntity<Long> {

    @Column(nullable = false)
    private String name;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, name = "status_scope_id", updatable = false)
    private StatusScope statusScope;

}
