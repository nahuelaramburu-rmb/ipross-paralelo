package com.capacidad.validationapi.module.base.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CollectionModelWrapper<T> extends CollectionModel<T> {

    private final Map<String, Iterable<T>> resources = new HashMap<>();
    private String relation;

    public CollectionModelWrapper(Iterable<T> content) {
        super(content, Collections.emptyList());
        addRelation("content");
    }

    public CollectionModelWrapper(String relation, Iterable<T> content, Link... links) {
        super(content, links);
        addRelation(relation);
    }

    public CollectionModelWrapper(String relation, Iterable<T> content, Iterable<Link> links) {
        super(content, links);
        addRelation(relation);
    }

    private void addRelation(String relation) {
        this.relation = relation;
        this.resources.put(relation, getContent());
    }

    @JsonIgnore
    @Override
    public Collection<T> getContent() {
        return super.getContent();
    }

    @JsonProperty("_embedded")
    public Map<String, Iterable<T>> getResources() {
        return this.resources;
    }

    @JsonIgnore
    public String getRelation() {
        return this.relation;
    }

}
