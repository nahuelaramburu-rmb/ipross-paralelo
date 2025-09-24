package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "application_profile")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "profile_seq_gen", sequenceName = "application_profile_seq", allocationSize = 1)
public class Profile extends BaseEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "profile_seq_gen")
    private Long id;


    @Column(nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "id_number", nullable = false)
    private Long idNumber;

    @Column(name = "id_type", nullable = false)
    private String idType;

}
