package com.capacidad.identityservice.loginv2;

import com.capacidad.identityservice.config.security.JwtUtils;
import com.capacidad.identityservice.exception.InvalidUserStateException;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.RegisterRequestDTO;
import com.capacidad.identityservice.model.dto.LoginRequestDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Log4j2
@Service
public class Loginv2Service {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtil;
    private final PasswordEncoder encoder;
    private final Loginv2Repository loginv2Repository;

    public Loginv2Service(AuthenticationManager authenticationManager, JwtUtils jwtUtil, PasswordEncoder encoder, Loginv2Repository loginv2Repository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
        this.loginv2Repository = loginv2Repository;
    }


    // todo , llenar con mas campos al user
    public String login(LoginRequestDTO request) {


        // se encarga de validar el user y password,
        // lanza una excepcion en casos incorrectos.
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );

        // aca ya se obtuvo el user
//        var user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new InvalidUserStateException("el usuario no existe"));
//
        // mock
        var usermock = loginv2Repository.findByUsuario(request.getUsername())
                .orElseThrow(() -> new InvalidUserStateException("el usuario no existe"));


        if (!Objects.equals(usermock.getPassword(), request.getPassword())){

            return "el usuario no existe";
        }

        return "usuario loggeado con exito";

        //  CustomUserDetails userDetails = new CustomUserDetails(user.getUsername(), user.getPassword(), emptyList());

        //var jwtToken = jwtUtil.generateToken(userDetails);

        // retorno el token de login
//        return AuthResponse.builder()
//                .token(jwtToken)
//                .build();

        //return null;
    }


    // todo , llenar con mas campos al user
//    public String register(RegisterRequestDTO request) {
//
//        // VALIDAR que no exista un user con el mismo email
//        var userDB = userRepository.findByEmail(request.getEmail());
//
//        if (userDB.isPresent()) {
//
//            throw new InvalidUserStateException("usuario con el mismo email ya registrado");
//        }
//
//        ApplicationUser user = new ApplicationUser();
//        user.setUsername(request.getFirstname());
//        user.setEmail(request.getEmail());
//        user.setPassword(encoder.encode(request.getPassword()));
////        user.setRole(request.getRole());
//
//        userRepository.save(user);
//
//        return "cliente registrado con exito";
//    }

}
