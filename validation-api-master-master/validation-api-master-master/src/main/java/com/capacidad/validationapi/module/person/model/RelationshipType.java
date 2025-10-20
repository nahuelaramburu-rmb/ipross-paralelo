package com.capacidad.validationapi.module.person.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.person.reference.RelationshipTypeReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "relationship_type")
@NoArgsConstructor
@Getter
@Setter
public class RelationshipType extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

    public RelationshipType(RelationshipTypeReference reference) {
        this.setId(reference.getId());
    }

}
