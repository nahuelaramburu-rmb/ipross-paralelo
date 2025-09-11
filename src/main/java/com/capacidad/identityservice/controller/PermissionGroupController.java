package com.capacidad.identityservice.controller;

import com.capacidad.identityservice.model.projection.PermissionGroupProjection;
import com.capacidad.identityservice.model.projection.PermissionSuggestionProjection;
import com.capacidad.identityservice.service.PermissionGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.ENDPOINT_PERMISSION_GROUPS;

@RestController
@RequestMapping(value = ENDPOINT_PERMISSION_GROUPS)
public class PermissionGroupController {

    private final PermissionGroupService permissionGroupService;

    public PermissionGroupController(PermissionGroupService permissionGroupService) {
        this.permissionGroupService = permissionGroupService;
    }

    @GetMapping
    public ResponseEntity<List<PermissionGroupProjection>> getPermissionGroups() {
        return ResponseEntity.ok(permissionGroupService.getAllPermissionGroups());
    }

    @GetMapping(params = {"role"})
    public ResponseEntity<List<PermissionGroupProjection>> findPermissionGroupsByRole(@RequestParam String role) {
        return ResponseEntity.ok(permissionGroupService.findAllPermissionGroupsByRole(role));
    }

    @GetMapping(value = "/suggestions", params = {"role"})
    public ResponseEntity<List<PermissionSuggestionProjection>> findPermissionSuggestionsByRole(@RequestParam String role) {
        return ResponseEntity.ok(permissionGroupService.findAllPermissionSuggestionsByRole(role));
    }

}
