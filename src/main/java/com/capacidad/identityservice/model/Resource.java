package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "application_resource")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "resource_seq_gen", sequenceName = "application_resource_seq", allocationSize = 1)
public class Resource extends BaseEntity<Long> implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "resource_seq_gen")
//    @SequenceGenerator(name = "resource_seq_gen", sequenceName = "application_resource_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

}
