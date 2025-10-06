package com.capacidad.identityservice.model.dto.authdto;

import com.capacidad.identityservice.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
    private Profile profile;

}
