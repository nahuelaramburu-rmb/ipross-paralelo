package com.capacidad.validationapi.module.procedure.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.dto.UnknownAuthorizationProcedureDTO;
import com.capacidad.validationapi.module.procedure.hateoas.UnknownAuthorizationProcedureResource;
import com.capacidad.validationapi.module.procedure.model.UnknownAuthorizationProcedure;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.service.UnknownAuthorizationProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_UNKNOWN_AUTHORIZATION_PROCEDURES;
import static com.capacidad.validationapi.misc.constant.PathVariables.ID_PATH;

@RestController
@RequestMapping(value = ENDPOINT_UNKNOWN_AUTHORIZATION_PROCEDURES)
public class UnknownAuthorizationProcedureController extends BaseProcedureControllerImpl<UnknownAuthorizationProcedureDTO, ProcedureResolutionDTO, UnknownAuthorizationProcedure> {

    private final UnknownAuthorizationProcedureService unknownAuthorizationProcedureService;

    @Autowired
    public UnknownAuthorizationProcedureController(UnknownAuthorizationProcedureService unknownAuthorizationProcedureService) {
        super(unknownAuthorizationProcedureService);
        this.unknownAuthorizationProcedureService = unknownAuthorizationProcedureService;
    }

    @PreAuthorize("@authorizationChecker.hasAccessToAuthorization(#input.medicalAuthorization.id)")
    @PostMapping
    @Override
    public ResponseEntity<Object> addOne(@RequestParam(value = "file") List<MultipartFile> file,
                                         @Valid @RequestParam(value = "body") UnknownAuthorizationProcedureDTO input) throws ObjectNotValidException, ObjectNotFoundException {
        ProcedureProjection result = unknownAuthorizationProcedureService.create(input, file);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path(ID_PATH)
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(params = "authorizationId")
    public ResponseEntity<UnknownAuthorizationProcedureResource> findByAuthorizationId(@RequestParam Long authorizationId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(new UnknownAuthorizationProcedureResource(unknownAuthorizationProcedureService.findProcedureByAuthorizationId(authorizationId)));
    }


}
