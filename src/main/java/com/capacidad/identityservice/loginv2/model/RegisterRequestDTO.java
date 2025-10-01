package com.capacidad.identityservice.loginv2.model;

import com.capacidad.identityservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {

    private String nombre;
    private String numero_afiliado;
    private String email;
    private String password;
    private String estado;
    private String plan;
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