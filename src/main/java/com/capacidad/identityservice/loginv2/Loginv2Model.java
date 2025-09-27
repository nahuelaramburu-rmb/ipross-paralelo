package com.capacidad.identityservice.loginv2;


import com.capacidad.identityservice.model.Userv2Login;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "login" )
public class Loginv2Model {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;
    private String password;


    @OneToOne
    @JoinColumn(name = "id_user") // esta es la clave foránea
    private Userv2Login user;

    private Date created_at;

}
