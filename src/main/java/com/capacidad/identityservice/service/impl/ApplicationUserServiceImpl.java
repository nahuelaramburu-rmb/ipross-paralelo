package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.exception.InvalidUserStateException;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.model.dto.*;
import com.capacidad.identityservice.model.projection.ApplicationUserProjection;
import com.capacidad.identityservice.repository.*;
import com.capacidad.identityservice.service.ApplicationUserService;
import com.capacidad.identityservice.service.ApplicationUserSupportService;
import com.capacidad.identityservice.service.base.BaseServiceImpl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;


/*
 *
 * este service se usa en el contexto de cuando un user logeado quiere actualizar su password,resetear ,confirmar user,
 * es decir , operaciones de un user ya registrado en el sistema,
 *
 * */


@Log4j2
@Service
public class ApplicationUserServiceImpl extends BaseServiceImpl<ApplicationUser, Long> implements ApplicationUserService {

    private static final String INVALID_USER_STATE_ERROR_CODE = "applicationUser.invalidUserState";
    private final ApplicationUserRepository userRepository;
    private final ApplicationUserSupportService supportService;



    @Autowired
    public ApplicationUserServiceImpl(ApplicationUserRepository repository,
                                      ApplicationUserSupportService supportService
                                      ) {
        super(repository);
        this.userRepository = repository;
        this.supportService = supportService;
    }


    @Override
    public void checkUserState(ApplicationUser user) {
        if (user.getState().getId().equals(StateReference.UNCONFIRMED.getId()))
            throw new InvalidUserStateException("applicationUser.invalidChallenge", user.getChallengeType().toString());
        if (user.getState().getId().equals(StateReference.DISABLED.getId()))
            throw new InvalidUserStateException("applicationUser.userDisabled");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUser user = findByUsernameOrEmail(resetPasswordDTO.getUsername());
        State unconfirmed = supportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId());
        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);
        user.setPassword(resetPasswordDTO.getNewPassword());
        validate(user);
        encodePassword(user);
        clearUserTokens(user.getUsername());
        userRepository.save(user);
    }

    @Override
    public void updatePassword(NewPasswordDTO newPasswordDTO) throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUser user = findByUsernameOrEmail(Utils.getAuthenticatedAuthorityPrincipal());
        updatePassword(user, newPasswordDTO);
    }

    @Override
    public void updateTemporaryPassword(String username, NewPasswordDTO newPasswordDTO) throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUser user = findByUsernameOrEmail(username);
        if (!user.getState().getId().equals(StateReference.UNCONFIRMED.getId()) || user.getChallengeType() != ChallengeType.FORCE_CHANGE_PASSWORD)
            throw new ObjectNotValidException(INVALID_USER_STATE_ERROR_CODE);
        State confirmed = supportService.getEntityReference(State.class, StateReference.CONFIRMED.getId());
        user.setState(confirmed);
        user.setChallengeType(null);
        updatePassword(user, newPasswordDTO);
    }

    private void updatePassword(ApplicationUser user, NewPasswordDTO newPasswordDTO) throws ObjectNotValidException {
        if (!supportService.passwordMatches(newPasswordDTO.getPassword(), user.getPassword()))
            throw new BadCredentialsException("applicationUser.invalidCurrentPassword");
        if (supportService.passwordMatches(newPasswordDTO.getNewPassword(), user.getPassword()))
            throw new ObjectNotValidException("applicationUser.samePassword");
        user.setPassword(newPasswordDTO.getNewPassword());
        validate(user);
        encodePassword(user);
        userRepository.save(user);
    }

    @Override
    public void clearUnconfirmedUser(ApplicationUser user) {
        Optional<ApplicationUser> optionalUser = userRepository.findByUsername(user.getUsername());
        if (optionalUser.isPresent() && Duration.between(LocalDateTime.now(),
                optionalUser.get().getCreatedAt().plusMinutes(supportService.getVerificationRetryProp())).isNegative()) {
            ApplicationUser dbUser = optionalUser.get();
            if (dbUser.getState().getId().equals(StateReference.UNCONFIRMED.getId()) &&
                    dbUser.getChallengeType() == ChallengeType.EMAIL_VERIFICATION_REQUIRED) {
                userRepository.delete(dbUser);
                userRepository.flush();
            }
        }
    }

    @Override
    public void update(ApplicationUser user) {
        userRepository.save(user);
    }

    @Override
    public void confirmForgotPassword(RestorePasswordDTO input) throws ObjectNotFoundException, ObjectNotValidException {

        ApplicationUser user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("applicationUser.notFoundEmail", input.getEmail()));

        if (!user.getState().getId().equals(StateReference.CONFIRMED.getId()))
            throw new ObjectNotValidException("applicationUser.notConfirmed");

        if (user.getRestoreOtp() == null || !user.getRestoreOtp().equals(input.getRestoreOtp()))
            throw new ObjectNotValidException("applicationUser.invalidRestoreOTP");

        boolean isNotExpiredAndValid = supportService.isOtpValid(input.getRestoreOtp(), user.getEmail());

        user.setRestoreOtp(null);

        if (!isNotExpiredAndValid) {
            userRepository.save(user);
            userRepository.flush();
            throw new ObjectNotValidException("applicationUser.invalidRestoreOTP");
        }

        user.setPassword(input.getNewPassword());
        validate(user);
        encodePassword(user);
        userRepository.save(user);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApplicationUser restorePassword(String email) throws ObjectNotFoundException {

        ApplicationUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("applicationUser.notFoundEmail", email));

        // fuerza a hibernate a cargar el profile,para cuando hace un save del user, no haya errores
        Profile profile = user.getProfile();
        profile.getName();

        int restoreOtp = supportService.generateOtpCode(user.getEmail());

        user.setRestoreOtp(restoreOtp);

        userRepository.save(user);

        return user;
    }


    @Override
    public String verifyAccount(int otpCode, UUID sub) {
        try {

            // probar la verificacion con dni del user
            ApplicationUser userDni = userRepository.findBySub(sub)
                    .orElseThrow(() -> new ObjectNotFoundException("applicationUser.notFoundSub", sub.toString()));


            ApplicationUser user = userRepository.findBySub(sub)
                    .orElseThrow(() -> new ObjectNotFoundException("applicationUser.notFoundSub", sub.toString()));

            if (user.getState().getId().equals(StateReference.CONFIRMED.getId()))
                return buildVerificationTemplateResponse("already-verified");

            if (!user.getState().getId().equals(StateReference.UNCONFIRMED.getId()) || user.getChallengeType() != ChallengeType.EMAIL_VERIFICATION_REQUIRED)
                throw new ObjectNotValidException(INVALID_USER_STATE_ERROR_CODE);

            if (!user.getVerificationOtp().equals(otpCode) || !supportService.isOtpValid(otpCode, user.getEmail()))
                throw new ObjectNotValidException("applicationUser.invalidVerificationOTP");

            user.setEmailVerified(true);
            State confirmed = supportService.getEntityReference(State.class, StateReference.CONFIRMED.getId());
            user.setState(confirmed);
            user.setChallengeType(null);
            userRepository.save(user);

            return buildVerificationTemplateResponse("verification-success");

        } catch (ObjectNotValidException | ObjectNotFoundException e) {
            log.error("({}) - exception: {}", e.getMessage(), e.getClass());
            return buildVerificationTemplateResponse("verification-error");
        }
    }



    public String verifyAccountByIdNumber(int otpCode, Long idNumber) {
        try {

            // probar la verificacion con dni del user
            ApplicationUser user = userRepository.findByProfile_IdNumber(idNumber)
                    .orElseThrow(() -> new ObjectNotFoundException("applicationUser.notFoundSub", idNumber.toString()));

            // si el user ya esta verificado
            if (user.getState().getId().equals(StateReference.CONFIRMED.getId()))
                return buildVerificationTemplateResponse("already-verified");

            // se valida si el estado de confirmacion del user no es UNCONFIRMED  Ó
            // se verifica el tipo de validacion del user , contra la validacion por email
            if (!user.getState().getId().equals(StateReference.UNCONFIRMED.getId()) || user.getChallengeType() != ChallengeType.EMAIL_VERIFICATION_REQUIRED)
                throw new ObjectNotValidException(INVALID_USER_STATE_ERROR_CODE);


            // se valida si el otp que genero el user al registrarse , es el mismo al ingresado
            if (!user.getVerificationOtp().equals(otpCode) || !supportService.isOtpValid(otpCode, user.getEmail()))
                throw new ObjectNotValidException("applicationUser.invalidVerificationOTP");


            user.setEmailVerified(true);
            State confirmed = supportService.getEntityReference(State.class, StateReference.CONFIRMED.getId());
            user.setState(confirmed);
            user.setChallengeType(null); // aqui ya no se requiere que el user modifique su password,
            userRepository.save(user);

            return buildVerificationTemplateResponse("verification-success");

        } catch (ObjectNotValidException | ObjectNotFoundException e) {
            log.error("({}) - exception: {}", e.getMessage(), e.getClass());
            return buildVerificationTemplateResponse("verification-error");
        }
    }



    private String buildVerificationTemplateResponse(String templateName) {
        return supportService.prepareTemplate(Collections.emptyMap(), templateName);
    }


    @Override
    public ApplicationUser create(ApplicationUser user) throws ObjectNotValidException {
        log.info("create - args: {}({})", user.getClass(), user);
        initializeUserCreation(user);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);
        return userRepository.save(user);
    }


    // users/me
    @Override
    public ApplicationUserProjection getAuthUser() throws ObjectNotFoundException {

        String authUserEmail = Utils.getAuthenticatedAuthorityPrincipal();

        return userRepository.findProjectedByEmail(authUserEmail)
                .orElseThrow(() -> new ObjectNotFoundException("applicationUser.notFoundUsername", authUserEmail));
    }


    // registra un cliente
    @Override
    public ApplicationUser signUp(ApplicationUser user) throws ObjectNotValidException {
        log.info("signUp - args: {}({})", user.getClass(), user);
        initializeUserCreation(user);
        user.setChallengeType(ChallengeType.EMAIL_VERIFICATION_REQUIRED);
        Integer otpCode = supportService.generateOtpCode(user.getEmail());
        user.setVerificationOtp(otpCode);
        return userRepository.save(user);
    }

    private void initializeUserCreation(ApplicationUser user) throws ObjectNotValidException {
        validate(user);
        encodePassword(user);
        user.setGroup(Group.valueOf(supportService.getActiveProfileProp().toUpperCase()));
        State unconfirmed = supportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId());
        user.setState(unconfirmed);
        user.associateChildObjects();
    }


    @Override
    public void validate(ApplicationUser user) throws ObjectNotValidException {
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());

        // validar dni , cod beneficiario
    }


    private void validateEmail(String email) throws ObjectNotValidException {

        Optional<ApplicationUser> user = userRepository.findByEmail(email);

        if (user.isPresent()) {

            throw new ObjectNotValidException("applicationUser.emailAlreadyExist");
        }
    }


    // el largo de password debe ser mayor a 8 , debe contener al menos 1 numero y una letra
    private void validatePassword(String password) throws ObjectNotValidException {
        if (password.length() < 8)
            throw new ObjectNotValidException("applicationUser.invalidPasswordLength");

        var alphaPattern = Pattern.compile("\\p{Alpha}");
        var digitPattern = Pattern.compile("\\p{Digit}");
        var alphaMatcher = alphaPattern.matcher(password);
        var digitMatcher = digitPattern.matcher(password);

        if (!alphaMatcher.find() || !digitMatcher.find())
            throw new ObjectNotValidException("applicationUser.invalidPasswordChars");
    }


    @Override
    public void encodePassword(ApplicationUser user) {
        String unencodedPassword = user.getPassword();
        user.setPassword(supportService.encodePassword(unencodedPassword));
        user.setTemporaryPassword(unencodedPassword);
    }


    @Override
    public ApplicationUser findByUsernameOrEmail(String username) throws ObjectNotFoundException {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ObjectNotFoundException("applicationUser.notFoundUsernameOrEmail", username));
    }


    @Override
    public boolean existsByResourceId(UUID resourceId) {
        return userRepository.existsByResourceId(resourceId);
    }

    @Override
    public void delete(ApplicationUser user) {
        userRepository.delete(user);
    }

    @Override
    public void clearUserTokens(String username) {
        Set<String> refreshTokenId = userRepository.getUserRefreshToken(username);
        refreshTokenId.forEach(userRepository::clearUserRefreshToken);
        userRepository.clearUserAccessToken(username);
        supportService.clearTokensFromStore(username);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ApplicationUserContext userContext, UpdateApplicationUserDTO dataToUpdate) throws ObjectNotFoundException {
        ApplicationUser contextUser = userContext.getUser();
        contextUser.setEmail(dataToUpdate.getEmail());
        contextUser.getProfile().setName(dataToUpdate.getProfile().getName());
        contextUser.getProfile().setLastName(dataToUpdate.getProfile().getLastName());
        contextUser.getProfile().setIdNumber(dataToUpdate.getProfile().getIdNumber());
        contextUser.getProfile().setIdType(dataToUpdate.getProfile().getIdType());
        ApplicationUser updated = userRepository.save(contextUser);
        userContext.setUser(updated);
    }


}
