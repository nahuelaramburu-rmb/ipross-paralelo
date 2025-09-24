package com.capacidad.identityservice.model;


import jakarta.persistence.*;
import org.springframework.data.annotation.Reference;

import java.util.Date;

@Entity
@Table(name = "login" )
public class LoginMockModel {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;
    private String password;


    @OneToOne
    @JoinColumn(name = "user_id") // esta es la clave foránea
    private User user;

    private Date created_at;

}
