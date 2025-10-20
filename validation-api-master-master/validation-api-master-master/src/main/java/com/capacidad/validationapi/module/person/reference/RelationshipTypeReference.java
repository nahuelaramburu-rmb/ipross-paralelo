package com.capacidad.validationapi.module.person.reference;

import com.capacidad.validationapi.module.person.model.RelationshipType;

public enum RelationshipTypeReference {
    HOLDER(1L),
    SON(4L),
    NEWBORN(8L),
    DEFAULT_RELATIONSHIP_TYPE(9L);

    private final long id;

    RelationshipTypeReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public RelationshipType getInstance() {
        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(id);
        return relationshipType;
    }
}
