package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.security.JwtService;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.model.dto.authdto.AuthenticationResponse;
import com.capacidad.identityservice.model.dto.authdto.LoginRequestDTO;
import com.capacidad.identityservice.model.dto.authdto.RegisterRequest;
import com.capacidad.identityservice.repository.*;
import com.capacidad.identityservice.service.ApplicationUserService;
import com.capacidad.identityservice.service.ApplicationUserSupportService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ApplicationUserRepository applicationUserRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final ApplicationUserService applicationUserService;
    private final ApplicationUserSupportService applicationUserSupportService;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final ApplicationUserContextRepository applicationUserContextRepository;


//    public AuthenticationResponse register(RegisterRequest request) {
//
//
//        var user = User.builder()
//                .firstname(request.getFirstname())
//                .lastname(request.getLastname())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .role(request.getRole())
//                .build();
//
//        var savedUser = applicationUserRepository.save(user);
//        var jwtToken = jwtService.generateToken(user);
//        var refreshToken = jwtService.generateRefreshToken(user);
//
//        saveUserToken(savedUser, jwtToken);
//
//        return AuthenticationResponse.builder()
//                .accessToken(jwtToken)
//                .refreshToken(refreshToken)
//                .build();
//    }


    // obtiene el token del user asociado a CustomUserDetails
    public AuthenticationResponse login(LoginRequestDTO loginRequestDTO) throws ObjectNotFoundException {

        Optional<ApplicationUser> applicationUserOptional = applicationUserRepository.findByEmail(loginRequestDTO.getEmail());

        if (applicationUserOptional.isEmpty()){

            throw new ObjectNotFoundException("user not found");

        }
        ApplicationUser appUser = applicationUserOptional.get();

        System.out.println( "app user name " + appUser.getUsername());

        // se encarga de validar el user y password,
        // lanza una excepcion en casos incorrectos.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getEmail(), loginRequestDTO.getPassword()
        ));

        // obtiene el customUserDetails , en base a username
        // todo , debe contener username , role , scoperole , etc
        var user = customUserDetailsService.loadUserByUsername(appUser.getUsername());


        // todo , obtener role , operations asociadas al user !!
        String userRole = user.getRole().getName();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("role", userRole);

        extraClaims.put("operatiors", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        extraClaims.put("token type" , "");

        var jwtToken = jwtService.generateToken(extraClaims,user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();

    }


    private void saveUserToken(CustomUserDetails user, String jwtToken) throws ObjectNotFoundException {

        System.out.println("user id null? " + user.getApplicationUser().getId());

        Optional<ApplicationUser> persistedUser = applicationUserRepository.findById(user.getApplicationUser().getId());

        if (persistedUser.isEmpty()){
            throw new ObjectNotFoundException("no existe el user");
        }

        var token = Token.builder()
                .user(persistedUser.get())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(CustomUserDetails user) {

        Long userId = user.getApplicationUser().getId();

        var validUserTokens = tokenRepository.findAllValidTokenByUser(userId);
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }


    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ObjectNotFoundException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = customUserDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }


    // // registra un beneficiary ------------------------------------------------------------------
    public ApplicationUser createBeneficiary(ApplicationUser user) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", user.getClass(), user);
        initializeUserBeneficiaryCreation(user);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);
        applicationUserRepository.save(user);

        Optional<Tenant> tenant = tenantRepository.findByName("app mobile");
        Optional<Role> role = roleRepository.findByName("BENEFICIARY");

        ApplicationUserContext context = new ApplicationUserContext();
        context.setUser(user);

        tenant.ifPresent(context::setTenant);
        role.ifPresent(context::setRole);

        context.setPermissionStrategy(PermissionStrategy.DEFAULT_ROLE);

        applicationUserContextRepository.save(context);

        return user;
    }


    // // registra un practitioner ------------------------------------------------------------------
    public ApplicationUser createPractitioner(ApplicationUser user) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", user.getClass(), user);
        initializeUserPractitionerCreation(user);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);
        applicationUserRepository.save(user);

        Optional<Tenant> tenant = tenantRepository.findByName("app mobile");
        Optional<Role> role = roleRepository.findByName("PRACTITIONER");

        ApplicationUserContext context = new ApplicationUserContext();
        context.setUser(user);

        tenant.ifPresent(context::setTenant);
        role.ifPresent(context::setRole);

        context.setPermissionStrategy(PermissionStrategy.DEFAULT_ROLE);

        applicationUserContextRepository.save(context);

        return user;
    }

    //------------------------------------------------------------------

    //------------------------------------------------------------------
    private void initializeUserBeneficiaryCreation(ApplicationUser user) throws ObjectNotValidException, ObjectNotFoundException {
        applicationUserService.validate(user);
        applicationUserService.encodePassword(user);
        user.setGroup(Group.valueOf(applicationUserSupportService.getActiveProfileProp().toUpperCase())); // que group le asigna aca ??
        State unconfirmed = applicationUserSupportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId());
        user.setState(unconfirmed);
        user.associateChildObjects();
    }


    private void initializeUserPractitionerCreation(ApplicationUser user) throws ObjectNotValidException, ObjectNotFoundException {
        applicationUserService.validate(user);
        applicationUserService.encodePassword(user);
        user.setGroup(Group.valueOf(applicationUserSupportService.getActiveProfileProp().toUpperCase())); // que group le asigna aca ??
        State unconfirmed = applicationUserSupportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId());
        user.setState(unconfirmed);
        user.associateChildObjects();
    }
    //------------------------------------------------------------------

}

