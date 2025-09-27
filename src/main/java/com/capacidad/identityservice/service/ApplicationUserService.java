package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.RegisterRequestDTO;
import com.capacidad.identityservice.model.dto.*;
import com.capacidad.identityservice.model.projection.ApplicationUserProjection;
import com.capacidad.identityservice.service.base.BaseService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;

import java.util.UUID;

public interface ApplicationUserService extends BaseService<ApplicationUser, Long> {

    void checkUserState(ApplicationUser user);

    void resetPassword(ResetPasswordDTO resetPasswordDTO) throws ObjectNotFoundException, ObjectNotValidException;

    void updatePassword(NewPasswordDTO newPasswordDTO) throws ObjectNotFoundException, ObjectNotValidException;

    void updateTemporaryPassword(String username, NewPasswordDTO newPasswordDTO) throws ObjectNotFoundException, ObjectNotValidException;

    void clearUnconfirmedUser(ApplicationUser user);

    void update(ApplicationUser user);

    String verifyAccount(int otpCode, UUID sub);

    void confirmForgotPassword(RestorePasswordDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    ApplicationUser restorePassword(String email) throws ObjectNotFoundException;

    ApplicationUser create(ApplicationUser user) throws ObjectNotValidException;

    ApplicationUserProjection getAuthUser() throws ObjectNotFoundException;

    ApplicationUser signUp(ApplicationUser user) throws ObjectNotValidException;

    ApplicationUser findByUsernameOrEmail(String username) throws ObjectNotFoundException;

    boolean existsByResourceId(UUID resourceId);

    void delete(ApplicationUser object) throws ObjectNotFoundException;

    void clearUserTokens(String username);

    void update(ApplicationUserContext userContext, UpdateApplicationUserDTO dataToUpdate) throws ObjectNotFoundException;
}
