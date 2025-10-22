package com.capacidad.identityservice.misc;



import com.capacidad.identityservice.model.dto.authdto.RegisterRequest;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.dto.authdto.RegisterResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DTOtoEntityMapper {


//    public ApplicationUser mapRegisterRequestToApplicationUser(RegisterRequest registerRequestDTO ){
//
////        return new ApplicationUser(
////                registerRequestDTO.getUsername(),
////                registerRequestDTO.getEmail(),
////                registerRequestDTO.getPassword()
////               // registerRequestDTO.getProfile()
////        );
//    }


    public RegisterResponseDTO ApplicationUserToRegisterResponse(ApplicationUser user ){

        return new RegisterResponseDTO(

                user.getUsername(),
                user.getEmail()
        );
    }





}
