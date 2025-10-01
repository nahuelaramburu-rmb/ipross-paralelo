package com.capacidad.identityservice.loginv2.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Data
@Entity
@NoArgsConstructor
public class AuthUserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_seq_gen")
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "numero_afiliado", nullable = false)
    private String numero_afiliado;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "plan", nullable = false)
    private String plan;


    public AuthUserInfo(String nombre, String numero_afiliado, String email, String password,String estado, String plan) {
        this.nombre = nombre;
        this.numero_afiliado = numero_afiliado;
        this.email = email;
        this.password = password;
        this.estado = estado;
        this.plan = plan;

    }
}


/*
*
* {
  "id": "12345",
  "nombre": "Juan Pérez",
  "numero_afiliado": "123456789",
  "email": "juan.perez@email.com",
  "estado": "activo",
  "plan": "Plan Básico"
}
* */