package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.dto.UpdateApplicationUserContextDTO;
import com.capacidad.identityservice.model.projection.ApplicationUserContextProjection;
import com.capacidad.identityservice.service.base.BaseService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.Set;
import java.util.UUID;

public interface ApplicationUserContextService extends BaseService<ApplicationUserContext, Long> {

    ApplicationUserContext create(ApplicationUserContext object) throws ObjectNotValidException, ObjectNotFoundException;

    void delete(String username) throws ObjectNotFoundException, ObjectNotValidException;

    void deleteAll(UUID resourceId) throws ObjectNotFoundException, ObjectNotValidException;

    void forgotPassword(String email) throws ObjectNotFoundException, ObjectNotValidException;

    void checkOperationalState(ApplicationUser user, ApplicationUserContext context, ClientDetails clientDetails);

    Set<ApplicationUserContext> findAllContextsByUsernameOrEmail(String input) throws ObjectNotFoundException;

    Page<ApplicationUserContextProjection.WithoutPermissionGroups> findUsers(UUID resourceId, String roleName, String search, Pageable pageable) throws ObjectNotFoundException;

    ApplicationUserContextProjection.WithPermissionGroups findUser(UUID sub) throws ObjectNotFoundException;

    ApplicationUserContextProjection update(UUID sub, UpdateApplicationUserContextDTO input) throws ObjectNotFoundException;

}
