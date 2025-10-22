package com.capacidad.identityservice.model.dto.authdto;

import com.capacidad.identityservice.model.Profile;
import com.capacidad.identityservice.model.dto.ProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/*
*
* {
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "MySecurePass123",
  "profile": {
    "firstName": "John",
    "lastName": "Doe"
  },
*
* */


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private ProfileDTO profile;
   // private String beneficiary_code;

    // ej dni , cred , otra tipo de id,
    // private String userId;
    // private String userIdType;

    // insertar fecha de nacimiento
    // private Date birthdate;


}
