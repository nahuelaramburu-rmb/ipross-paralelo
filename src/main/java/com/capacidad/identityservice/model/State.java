package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "application_state")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "state_seq_gen", sequenceName = "application_state_seq", allocationSize = 1)
public class State extends BaseEntity<Long> implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "state_seq_gen")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

}
