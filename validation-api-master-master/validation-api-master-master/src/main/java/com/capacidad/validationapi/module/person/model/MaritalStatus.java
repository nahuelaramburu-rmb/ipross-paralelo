package com.capacidad.validationapi.module.person.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "marital_status")
@NoArgsConstructor
@Getter
@Setter
public class MaritalStatus extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

}
