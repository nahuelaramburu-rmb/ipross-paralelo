package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.exception.InvalidUserStateException;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.model.dto.NewPasswordDTO;
import com.capacidad.identityservice.model.dto.ResetPasswordDTO;
import com.capacidad.identityservice.model.dto.RestorePasswordDTO;
import com.capacidad.identityservice.repository.ApplicationUserRepository;
import com.capacidad.identityservice.service.ApplicationUserSupportService;
import com.capacidad.identityservice.service.RoleService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationUserServiceImplTest {

    @Mock
    private RoleService roleService;
    @Mock
    private ApplicationUserSupportService supportService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ApplicationUserRepository userRepository;
    @InjectMocks
    private ApplicationUserServiceImpl userService;

    @Test
    public void testCheckUserStateThrowsInvalidUserStateExceptionWhenUserIsUnconfirmed() {
        ApplicationUser user = new ApplicationUser();
        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());
        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);

        InvalidUserStateException thrown = (InvalidUserStateException) catchThrowable(() -> userService.checkUserState(user));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.invalidChallenge");
    }

    @Test
    public void testCheckUserStateThrowsInvalidUserStateExceptionWhenUserIsDisabled() {
        ApplicationUser user = new ApplicationUser();
        State unconfirmed = new State();
        unconfirmed.setId(StateReference.DISABLED.getId());
        user.setState(unconfirmed);

        InvalidUserStateException thrown = (InvalidUserStateException) catchThrowable(() -> userService.checkUserState(user));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.userDisabled");
    }

    @Test
    public void testCheckUserStateDoNotThrowExceptionWhenUserIsConfirmed() {
        ApplicationUser user = new ApplicationUser();
        State unconfirmed = new State();
        unconfirmed.setId(StateReference.CONFIRMED.getId());
        user.setState(unconfirmed);

        userService.checkUserState(user);
    }

    @Test
    public void testResetPasswordSuccessfullySetsUserStateNewPasswordAndChallengeWhenPasswordIsValid() throws ObjectNotValidException, ObjectNotFoundException {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setNewPassword("new_password");
        resetPasswordDTO.setUsername("user_test");
        resetPasswordDTO.setNewPassword("validNewPassword123");

        ApplicationUser user = new ApplicationUser();
        user.setUsername(resetPasswordDTO.getUsername());

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(supportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId())).thenReturn(unconfirmed);
        when(supportService.encodePassword(resetPasswordDTO.getNewPassword())).thenReturn("encodedValidNewPassword123");

        userService.resetPassword(resetPasswordDTO);

        assertThat(user.getPassword()).isEqualTo("encodedValidNewPassword123");
        assertThat(user.getState()).isEqualTo(unconfirmed);
        assertThat(user.getChallengeType()).isEqualTo(ChallengeType.FORCE_CHANGE_PASSWORD);
        verify(userRepository, times(1)).save(user);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testUpdatePasswordThrowsObjectNotFoundExceptionWhenAuthenticatedAuthorityIsNull() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("testAuthority"));
        Authentication anonymousAuthenticationToken = new AnonymousAuthenticationToken("key", "principal", authorities);

        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setPassword("currentPassword123");

        NewPasswordDTO newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setNewPassword("validNewPassword123");
        newPasswordDTO.setPassword("invalidPreviousPassword123");

        when(securityContext.getAuthentication()).thenReturn(anonymousAuthenticationToken);

        userService.updatePassword(newPasswordDTO);

        verify(userRepository, never()).save(user);
    }

    @Test(expected = BadCredentialsException.class)
    public void testUpdatePasswordThrowsBadCredentialsExceptionWhenPreviousPasswordDoesNotMatch() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = new JWTAuthenticationToken("user_test", "", Collections.emptyList(), Group.DEV, null);

        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setPassword("currentPassword123");

        NewPasswordDTO newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setNewPassword("validNewPassword123");
        newPasswordDTO.setPassword("invalidPreviousPassword123");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(supportService.passwordMatches(newPasswordDTO.getPassword(), user.getPassword())).thenReturn(false);

        userService.updatePassword(newPasswordDTO);

        verify(userRepository, never()).save(user);
    }

    @Test
    public void testUpdatePasswordThrowsObjectNotValidExceptionWhenPreviousPasswordIsEqualNewPassword() {
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = new JWTAuthenticationToken("user_test", "", Collections.emptyList(), Group.DEV, null);

        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setPassword("currentPassword123");

        NewPasswordDTO newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setNewPassword("currentPassword123");
        newPasswordDTO.setPassword("currentPassword123");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(supportService.passwordMatches(newPasswordDTO.getPassword(), user.getPassword())).thenReturn(true);
        when(supportService.passwordMatches(newPasswordDTO.getNewPassword(), user.getPassword())).thenReturn(true);

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.updatePassword(newPasswordDTO));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.samePassword");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("updatePassword");

        verify(userRepository, never()).save(user);
    }

    @Test
    public void testUpdatePasswordSuccessfullySetsNewPasswordWhenPreviousPasswordAndNewOneAreValid() throws ObjectNotValidException, ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = new JWTAuthenticationToken("user_test", "", Collections.emptyList(), Group.DEV, null);

        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setPassword("currentPassword123");

        NewPasswordDTO newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setNewPassword("newValidPassword123");
        newPasswordDTO.setPassword("currentPassword123");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(supportService.passwordMatches(newPasswordDTO.getPassword(), user.getPassword())).thenReturn(true);
        when(supportService.passwordMatches(newPasswordDTO.getNewPassword(), user.getPassword())).thenReturn(false);
        when(supportService.encodePassword(newPasswordDTO.getNewPassword())).thenReturn("encodedNewValidPassword123");

        userService.updatePassword(newPasswordDTO);

        assertThat(user.getPassword()).isEqualTo("encodedNewValidPassword123");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateTemporaryPasswordThrowsObjectNotValidWhenUserStateIsInvalid() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setPassword("currentPassword123");

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(confirmed);

        NewPasswordDTO newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setNewPassword("newValidPassword123");
        newPasswordDTO.setPassword("currentPassword123");

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.updateTemporaryPassword(user.getUsername(), newPasswordDTO));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.invalidUserState");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("updateTemporaryPassword");

        verify(userRepository, never()).save(user);
    }

    @Test
    public void testUpdateTemporaryPasswordThrowsObjectNotValidWhenUserChallengeIsInvalid() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setPassword("currentPassword123");

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.EMAIL_VERIFICATION_REQUIRED);

        NewPasswordDTO newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setNewPassword("newValidPassword123");
        newPasswordDTO.setPassword("currentPassword123");

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.updateTemporaryPassword(user.getUsername(), newPasswordDTO));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.invalidUserState");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("updateTemporaryPassword");

        verify(userRepository, never()).save(user);
    }

    @Test
    public void testUpdateTemporaryPasswordSuccessfullyUpdatesUserPasswordAndStateWhenUserDataIsValid() throws ObjectNotValidException, ObjectNotFoundException {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setPassword("currentPassword123");

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);

        NewPasswordDTO newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setNewPassword("newValidPassword123");
        newPasswordDTO.setPassword("currentPassword123");

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(supportService.passwordMatches(newPasswordDTO.getPassword(), user.getPassword())).thenReturn(true);
        when(supportService.passwordMatches(newPasswordDTO.getNewPassword(), user.getPassword())).thenReturn(false);
        when(supportService.encodePassword(newPasswordDTO.getNewPassword())).thenReturn("encodedNewValidPassword123");
        when(supportService.getEntityReference(State.class, StateReference.CONFIRMED.getId())).thenReturn(confirmed);

        userService.updateTemporaryPassword(user.getUsername(), newPasswordDTO);

        assertThat(user.getChallengeType()).isNull();
        assertThat(user.getState()).isEqualTo(confirmed);
        assertThat(user.getPassword()).isEqualTo("encodedNewValidPassword123");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testClearUnconfirmedUserDoNothingWhenUserDoesNotExist() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        userService.clearUnconfirmedUser(user);
    }

    @Test
    public void testClearUnconfirmedUserDoNothingWhenUserCreationIsLessThanVerificationWindow() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user_test");
        user.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(supportService.getVerificationRetryProp()).thenReturn(10L);

        userService.clearUnconfirmedUser(user);

        verify(userRepository, never()).delete(user);
    }

    @Test
    public void testClearUnconfirmedUserDoNothingWhenUserStateIsInvalid() {
        ApplicationUser user = new ApplicationUser();
        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());
        user.setUsername("user_test");
        user.setCreatedAt(LocalDateTime.now().minusMinutes(15));
        user.setState(confirmed);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(supportService.getVerificationRetryProp()).thenReturn(10L);

        userService.clearUnconfirmedUser(user);

        verify(userRepository, never()).delete(user);
    }

    @Test
    public void testClearUnconfirmedUserDoNothingWhenUserChallengeIsInvalid() {
        ApplicationUser user = new ApplicationUser();
        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());
        user.setUsername("user_test");
        user.setCreatedAt(LocalDateTime.now().minusMinutes(15));
        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(supportService.getVerificationRetryProp()).thenReturn(10L);

        userService.clearUnconfirmedUser(user);

        verify(userRepository, never()).delete(user);
    }

    @Test
    public void testClearUnconfirmedUserSuccessfullyDeletesAndFlushUserWhenUserDataIsValid() {
        ApplicationUser user = new ApplicationUser();
        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());
        user.setUsername("user_test");
        user.setCreatedAt(LocalDateTime.now().minusMinutes(15));
        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.EMAIL_VERIFICATION_REQUIRED);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(supportService.getVerificationRetryProp()).thenReturn(10L);

        userService.clearUnconfirmedUser(user);

        verify(userRepository, times(1)).delete(user);
        verify(userRepository, times(1)).flush();
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenPasswordLengthIsLessThan8Characters() {
        ApplicationUser user = new ApplicationUser();
        user.setPassword("invalid");

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.validate(user));

        assertPasswordError(thrown, "applicationUser.invalidPasswordLength");
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenPasswordDoNotContainNumbers() {
        ApplicationUser user = new ApplicationUser();
        user.setPassword("passwordWithoutNumbers");

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.validate(user));

        assertPasswordError(thrown, "applicationUser.invalidPasswordChars");
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenPasswordDoNotContainLetters() {
        ApplicationUser user = new ApplicationUser();
        user.setPassword("1122334455667788");

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.validate(user));

        assertPasswordError(thrown, "applicationUser.invalidPasswordChars");
    }

    private void assertPasswordError(ObjectNotValidException exception, String errorMessage) {
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getStackTrace()[0].getMethodName()).isEqualTo("validatePassword");
    }

    @Test
    public void testValidateDoNotThrowsExceptionWhenPasswordIsValid() throws ObjectNotValidException {
        ApplicationUser user = new ApplicationUser();
        user.setPassword("validPassword123456");

        userService.validate(user);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testConfirmForgotPasswordThrowsObjectNotFoundExceptionWhenEmailDoesNotExist() throws ObjectNotValidException, ObjectNotFoundException {
        RestorePasswordDTO restorePasswordDTO = new RestorePasswordDTO();
        restorePasswordDTO.setEmail("invalidemail@test.com");

        when(userRepository.findByEmail(restorePasswordDTO.getEmail())).thenReturn(Optional.empty());

        userService.confirmForgotPassword(restorePasswordDTO);
    }

    @Test
    public void testConfirmForgotPasswordThrowsObjectNotValidExceptionWhenUserStateIsInvalid() {
        ApplicationUser user = new ApplicationUser();

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        user.setState(unconfirmed);

        RestorePasswordDTO restorePasswordDTO = new RestorePasswordDTO();
        restorePasswordDTO.setEmail("validemail@test.com");

        when(userRepository.findByEmail(restorePasswordDTO.getEmail())).thenReturn(Optional.of(user));

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.confirmForgotPassword(restorePasswordDTO));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.notConfirmed");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("confirmForgotPassword");

        verify(userRepository, never()).save(user);
        verify(userRepository, never()).flush();
    }

    @Test
    public void testConfirmForgotPasswordThrowsObjectNotValidExceptionWhenRestoreOTPDoesNotExist() {
        ApplicationUser user = new ApplicationUser();
        user.setRestoreOtp(654321);

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(confirmed);

        RestorePasswordDTO restorePasswordDTO = new RestorePasswordDTO();
        restorePasswordDTO.setEmail("validemail@test.com");
        restorePasswordDTO.setRestoreOtp(123456);

        when(userRepository.findByEmail(restorePasswordDTO.getEmail())).thenReturn(Optional.of(user));

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.confirmForgotPassword(restorePasswordDTO));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.invalidRestoreOTP");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("confirmForgotPassword");

        verify(userRepository, never()).save(user);
        verify(userRepository, never()).flush();
    }

    @Test
    public void testConfirmForgotPasswordThrowsObjectNotValidExceptionWhenOtpIsExpired() {
        ApplicationUser user = new ApplicationUser();
        user.setRestoreOtp(654321);

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(confirmed);

        RestorePasswordDTO restorePasswordDTO = new RestorePasswordDTO();
        restorePasswordDTO.setEmail("validemail@test.com");
        restorePasswordDTO.setRestoreOtp(123456);

        when(userRepository.findByEmail(restorePasswordDTO.getEmail())).thenReturn(Optional.of(user));

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.confirmForgotPassword(restorePasswordDTO));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.invalidRestoreOTP");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("confirmForgotPassword");

        verify(userRepository, never()).save(user);
        verify(userRepository, never()).flush();
    }

    @Test
    public void testConfirmForgotPasswordThrowsObjectNotValidExceptionWhenOtpIsExpiredAndInvalid() {
        ApplicationUser user = new ApplicationUser();
        user.setRestoreOtp(654321);

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(confirmed);

        RestorePasswordDTO restorePasswordDTO = new RestorePasswordDTO();
        restorePasswordDTO.setEmail("validemail@test.com");
        restorePasswordDTO.setRestoreOtp(654321);

        when(userRepository.findByEmail(restorePasswordDTO.getEmail())).thenReturn(Optional.of(user));
        when(supportService.isOtpValid(654321, user.getEmail())).thenReturn(false);

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userService.confirmForgotPassword(restorePasswordDTO));

        assertThat(thrown.getMessage()).isEqualTo("applicationUser.invalidRestoreOTP");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("confirmForgotPassword");
        assertThat(user.getRestoreOtp()).isNull();

        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).flush();
    }

    @Test
    public void testConfirmForgotPasswordSuccessfullyUpdatesPasswordWhenOtpIsValid() throws ObjectNotValidException, ObjectNotFoundException {
        ApplicationUser user = new ApplicationUser();
        user.setRestoreOtp(654321);

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(confirmed);

        RestorePasswordDTO restorePasswordDTO = new RestorePasswordDTO();
        restorePasswordDTO.setEmail("validemail@test.com");
        restorePasswordDTO.setNewPassword("newPassword123");
        restorePasswordDTO.setRestoreOtp(654321);

        when(userRepository.findByEmail(restorePasswordDTO.getEmail())).thenReturn(Optional.of(user));
        when(supportService.isOtpValid(654321, user.getEmail())).thenReturn(true);
        when(supportService.encodePassword(restorePasswordDTO.getNewPassword())).thenReturn("encodedNewPassword123");

        userService.confirmForgotPassword(restorePasswordDTO);

        assertThat(user.getRestoreOtp()).isNull();
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword123");
        verify(userRepository, times(1)).save(user);
        verify(userRepository, never()).flush();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRestorePasswordThrowsObjectNotFoundExceptionWhenEmailIsInvalid() throws ObjectNotFoundException {
        String invalidEmail = "invalidemail@test.com";
        when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());
        userService.restorePassword(invalidEmail);
    }

    @Test
    public void testRestorePasswordSetsValidRestoreOTPWhenEmailIsValid() throws ObjectNotFoundException {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("validemail@test.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(supportService.generateOtpCode(user.getEmail())).thenReturn(123456);

        userService.restorePassword(user.getEmail());

        assertThat(user.getRestoreOtp()).isEqualTo(123456);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testVerifyAccountSendsInvalidTemplateMessageWhenSubDoesNotExist() {
        ApplicationUser user = new ApplicationUser();
        user.setSub(UUID.randomUUID());
        user.setVerificationOtp(123456);

        when(userRepository.findBySub(user.getSub())).thenReturn(Optional.empty());

        userService.verifyAccount(user.getVerificationOtp(), user.getSub());

        verify(userRepository, never()).save(user);
        verify(supportService, times(1)).prepareTemplate(Collections.emptyMap(), "verification-error");
    }

    @Test
    public void testVerifyAccountSendsAlreadyVerifiedTemplateMessageWhenUserConfirmed() {
        ApplicationUser user = new ApplicationUser();
        user.setSub(UUID.randomUUID());
        user.setVerificationOtp(123456);

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(confirmed);

        when(userRepository.findBySub(user.getSub())).thenReturn(Optional.of(user));

        userService.verifyAccount(user.getVerificationOtp(), user.getSub());

        verify(userRepository, never()).save(user);
        verify(supportService, times(1)).prepareTemplate(Collections.emptyMap(), "already-verified");
    }

    @Test
    public void testVerifyAccountSendsVerificationErrorTemplateMessageWhenUserDisabled() {
        ApplicationUser user = new ApplicationUser();
        user.setSub(UUID.randomUUID());
        user.setVerificationOtp(123456);

        State confirmed = new State();
        confirmed.setId(StateReference.DISABLED.getId());

        user.setState(confirmed);

        when(userRepository.findBySub(user.getSub())).thenReturn(Optional.of(user));

        userService.verifyAccount(user.getVerificationOtp(), user.getSub());

        verify(userRepository, never()).save(user);
        verify(supportService, times(1)).prepareTemplate(Collections.emptyMap(), "verification-error");
    }

    @Test
    public void testVerifyAccountSendsInvalidTemplateMessageWhenUserChallengeIsInvalid() {
        ApplicationUser user = new ApplicationUser();
        user.setSub(UUID.randomUUID());
        user.setVerificationOtp(123456);

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);

        when(userRepository.findBySub(user.getSub())).thenReturn(Optional.of(user));

        userService.verifyAccount(user.getVerificationOtp(), user.getSub());

        verify(userRepository, never()).save(user);
        verify(supportService, times(1)).prepareTemplate(Collections.emptyMap(), "verification-error");
    }

    @Test
    public void testVerifyAccountSendsInvalidTemplateMessageWhenVerificationOTPDoesNotMatch() {
        ApplicationUser user = new ApplicationUser();
        user.setSub(UUID.randomUUID());
        user.setVerificationOtp(123456);

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.EMAIL_VERIFICATION_REQUIRED);

        when(userRepository.findBySub(user.getSub())).thenReturn(Optional.of(user));

        userService.verifyAccount(654321, user.getSub());

        verify(userRepository, never()).save(user);
        verify(supportService, times(1)).prepareTemplate(Collections.emptyMap(), "verification-error");
    }

    @Test
    public void testVerifyAccountSendsInvalidTemplateMessageWhenVerificationOTPIsExpiredOrInvalid() {
        ApplicationUser user = new ApplicationUser();
        user.setSub(UUID.randomUUID());
        user.setVerificationOtp(123456);
        user.setEmail("test@test.com");

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.EMAIL_VERIFICATION_REQUIRED);

        when(userRepository.findBySub(user.getSub())).thenReturn(Optional.of(user));
        when(supportService.isOtpValid(123456, user.getEmail())).thenReturn(false);

        userService.verifyAccount(123456, user.getSub());

        verify(userRepository, never()).save(user);
        verify(supportService, times(1)).prepareTemplate(Collections.emptyMap(), "verification-error");
    }

    @Test
    public void testVerifyAccountSuccessfullyUpdatesUserStateWhenVerificationOTPIsValid() {
        ApplicationUser user = new ApplicationUser();
        user.setSub(UUID.randomUUID());
        user.setVerificationOtp(123456);
        user.setEmail("test@test.com");

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        user.setState(unconfirmed);
        user.setChallengeType(ChallengeType.EMAIL_VERIFICATION_REQUIRED);

        when(userRepository.findBySub(user.getSub())).thenReturn(Optional.of(user));
        when(supportService.isOtpValid(123456, user.getEmail())).thenReturn(true);
        when(supportService.getEntityReference(State.class, StateReference.CONFIRMED.getId())).thenReturn(confirmed);

        userService.verifyAccount(123456, user.getSub());

        assertThat(user.getEmailVerified()).isTrue();
        assertThat(user.getState()).isEqualTo(confirmed);
        assertThat(user.getChallengeType()).isNull();

        verify(userRepository, times(1)).save(user);
        verify(supportService, times(1)).prepareTemplate(Collections.emptyMap(), "verification-success");
    }

    @Test
    public void testCreateReturnsUserWithActiveProfileGroupWhenAuthenticatedAuthorityIsNull() throws ObjectNotValidException {
        ApplicationUser user = new ApplicationUser();
        user.setPassword("password123");

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        when(supportService.getActiveProfileProp()).thenReturn("test");
        when(supportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId())).thenReturn(unconfirmed);
        when(supportService.encodePassword(user.getPassword())).thenReturn("encodedpassword123");
        when(userRepository.save(user)).thenReturn(user);

        ApplicationUser result = userService.create(user);

        assertThat(result.getVerificationOtp()).isNull();
        assertThat(result.getState()).isEqualTo(unconfirmed);
        assertThat(result.getGroup()).isEqualTo(Group.TEST);
        assertThat(result.getChallengeType()).isEqualTo(ChallengeType.FORCE_CHANGE_PASSWORD);
        assertThat(result.getPassword()).isEqualTo("encodedpassword123");

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testCreateReturnsUserWithAuthenticatedAuthorityGroupWhenAuthenticatedAuthorityIsValid() throws ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUser user = new ApplicationUser();
        user.setPassword("password123");

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        when(supportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId())).thenReturn(unconfirmed);
        when(supportService.encodePassword(user.getPassword())).thenReturn("encodedpassword123");
        when(userRepository.save(user)).thenReturn(user);
        when(supportService.getActiveProfileProp()).thenReturn("dev");

        ApplicationUser result = userService.create(user);

        assertThat(result.getVerificationOtp()).isNull();
        assertThat(result.getState()).isEqualTo(unconfirmed);
        assertThat(result.getGroup()).isEqualTo(Group.DEV);
        assertThat(result.getChallengeType()).isEqualTo(ChallengeType.FORCE_CHANGE_PASSWORD);
        assertThat(result.getPassword()).isEqualTo("encodedpassword123");

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testSignUpReturnsUserWithAuthenticatedAuthorityGroupAndVerificationOTPWhenAuthenticatedAuthorityIsValid() throws ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUser user = new ApplicationUser();
        user.setPassword("password123");
        user.setEmail("test@test.com");

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        when(supportService.getEntityReference(State.class, StateReference.UNCONFIRMED.getId())).thenReturn(unconfirmed);
        when(supportService.encodePassword(user.getPassword())).thenReturn("encodedpassword123");
        when(supportService.generateOtpCode(user.getEmail())).thenReturn(654321);
        when(supportService.getActiveProfileProp()).thenReturn("dev");
        when(userRepository.save(user)).thenReturn(user);

        ApplicationUser result = userService.signUp(user);

        assertThat(result.getVerificationOtp()).isEqualTo(654321);
        assertThat(result.getState()).isEqualTo(unconfirmed);
        assertThat(result.getGroup()).isEqualTo(Group.DEV);
        assertThat(result.getChallengeType()).isEqualTo(ChallengeType.EMAIL_VERIFICATION_REQUIRED);
        assertThat(result.getPassword()).isEqualTo("encodedpassword123");

        verify(userRepository, times(1)).save(user);
    }

}