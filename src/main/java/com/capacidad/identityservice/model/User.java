package com.capacidad.identityservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;

    @OneToOne(mappedBy = "user") //
    private LoginMockModel login;
}
