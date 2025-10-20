package com.capacidad.validationapi.module.nomenclator.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.nomenclator.dto.NomenclatorGroupDTO;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorGroupProjection;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_NOMENCLATOR_GROUPS;

@RestController
@RequestMapping(value = ENDPOINT_NOMENCLATOR_GROUPS)
public class NomenclatorGroupController extends BaseControllerImpl<NomenclatorGroupDTO, Long> {

    private final NomenclatorGroupService nomenclatorGroupService;

    @Autowired
    public NomenclatorGroupController(NomenclatorGroupService nomenclatorGroupService) {
        super(nomenclatorGroupService);
        this.nomenclatorGroupService = nomenclatorGroupService;
    }

    @GetMapping(value = "{objectId}")
    @Override
    public ResponseEntity<Object> getOne(@PathVariable Long objectId) throws ObjectNotFoundException, ObjectNotValidException {
        return ResponseEntity.ok(nomenclatorGroupService.findProjectedById(objectId, NomenclatorGroupProjection.Extended.class));
    }

}
