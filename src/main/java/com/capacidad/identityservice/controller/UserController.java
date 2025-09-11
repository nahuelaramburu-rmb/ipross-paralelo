package com.capacidad.identityservice.controller;

import com.capacidad.identityservice.misc.constant.ControllerEndpoints;
import com.capacidad.identityservice.model.dto.*;
import com.capacidad.identityservice.model.hal.PageModelWrapper;
import com.capacidad.identityservice.model.projection.ApplicationUserContextProjection;
import com.capacidad.identityservice.model.projection.ApplicationUserProjection;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.ApplicationUserService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.*;


@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_USERS)
public class UserController {

    private final ApplicationUserContextService userContextService;
    private final ApplicationUserService userService;

    @Autowired
    public UserController(ApplicationUserService userService,
                          ApplicationUserContextService userContextService) {
        this.userContextService = userContextService;
        this.userService = userService;
    }

    /**
     * Public Access
     **/
    @GetMapping(value = ENDPOINT_FORGOT, params = {"email"})
    public ResponseEntity<Object> restorePassword(@RequestParam String email) throws ObjectNotFoundException, ObjectNotValidException {
        userContextService.forgotPassword(email);
        return ResponseEntity.noContent().build();
    }

    /**
     * Public Access
     **/
    @PutMapping(value = ENDPOINT_FORGOT)
    public ResponseEntity<Object> confirmForgotPassword(@Valid @RequestBody RestorePasswordDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        userService.confirmForgotPassword(input);
        return ResponseEntity.noContent().build();
    }

    /**
     * Public Access
     **/
    @GetMapping("/verification/{otp}/{sub}")
    public ResponseEntity<Object> verifyAccount(@PathVariable Integer otp, @PathVariable UUID sub) {
        String html = userService.verifyAccount(otp, sub);
        return ResponseEntity.ok(html);
    }

    /**
     * Public Access
     **/
    @PutMapping(value = ENDPOINT_PASSWORD, params = {"username"})
    public ResponseEntity<Object> updateTemporaryPassword(@RequestParam String username, @Valid @RequestBody NewPasswordDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        userService.updateTemporaryPassword(username, input);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(ENDPOINT_PASSWORD)
    public ResponseEntity<Object> updatePassword(@Valid @RequestBody NewPasswordDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        userService.updatePassword(input);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(ENDPOINT_PASSWORD_RESET)
    public ResponseEntity<Object> resetUserPassword(@Valid @RequestBody ResetPasswordDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        userService.resetPassword(input);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ENDPOINT_ME)
    public ResponseEntity<ApplicationUserProjection> getProfile() throws ObjectNotFoundException {
        return ResponseEntity.ok(userService.getAuthUser());
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody ApplicationUserContextDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        userContextService.create(userContextService.mapDtoToInput(input));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<PageModelWrapper<ApplicationUserContextProjection.WithoutPermissionGroups>> getUserData
            (@RequestParam(required = false) String role,
             @RequestParam(name = "resource_id", required = false) UUID resourceId,
             @RequestParam(name = "search", required = false) String search,
             @RequestParam int page,
             @RequestParam int size) throws ObjectNotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationUserContextProjection.WithoutPermissionGroups> results = userContextService.findUsers(resourceId, role, search, pageable);
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        if (StringUtils.isNotBlank(role))
            queryParams.add("role", role);
        if (resourceId != null)
            queryParams.add("resource_id", resourceId.toString());
        if (StringUtils.isNotBlank(search))
            queryParams.add("search", search);
        PageModelWrapper<ApplicationUserContextProjection.WithoutPermissionGroups> pageModelWrapper = new PageModelWrapper<>("users", results, pageable, queryParams);
        return ResponseEntity.ok(pageModelWrapper);
    }

    @PreAuthorize("hasAuthority('update:users')")
    @GetMapping(value = "{sub}")
    public ResponseEntity<ApplicationUserContextProjection.WithPermissionGroups> getUserData(@PathVariable UUID sub) throws ObjectNotFoundException {
        return ResponseEntity.ok(userContextService.findUser(sub));
    }

    @PutMapping(params = {"sub"})
    public ResponseEntity<ApplicationUserContextProjection> resetUserPassword(@RequestParam UUID sub, @Valid @RequestBody UpdateApplicationUserContextDTO input) throws ObjectNotFoundException {
        ApplicationUserContextProjection result = userContextService.update(sub, input);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteUserContext(@RequestParam String username) throws ObjectNotFoundException, ObjectNotValidException {
        userContextService.delete(username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(params = {"resource_id"})
    public ResponseEntity<Object> deleteAllUserContextsByResourceId(@RequestParam(name = "resource_id") UUID resourceId) throws ObjectNotFoundException, ObjectNotValidException {
        userContextService.deleteAll(resourceId);
        return ResponseEntity.noContent().build();
    }

}
