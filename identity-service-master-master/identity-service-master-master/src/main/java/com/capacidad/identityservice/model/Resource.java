package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "application_resource")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_resource_seq")
public class Resource extends BaseEntity<Long> implements Serializable {

    @Column(nullable = false, unique = true)
    private String name;

}
