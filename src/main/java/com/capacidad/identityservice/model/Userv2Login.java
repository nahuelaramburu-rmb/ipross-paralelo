package com.capacidad.identityservice.model;

import com.capacidad.identityservice.loginv2.Loginv2Model;
import jakarta.persistence.*;

@Entity
@Table(name = "_user")
public class Userv2Login {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String nombre;
    private String email;

    @OneToOne(mappedBy = "user") //
    private Loginv2Model login;
}
