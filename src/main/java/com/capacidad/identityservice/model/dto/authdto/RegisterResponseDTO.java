package com.capacidad.identityservice.model.dto.authdto;

import com.capacidad.identityservice.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponseDTO {

    private String username;
    private String email;

}
