package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.validationapi.module.contract.dto.PractitionerContractDTO;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import com.capacidad.validationapi.module.contract.service.PractitionerContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PRACTITIONER_CONTRACT;

@RestController
@RequestMapping(value = ENDPOINT_PRACTITIONER_CONTRACT)
public class PractitionerContractController extends BaseContractController<PractitionerContract, PractitionerContractDTO> {

    @Autowired
    public PractitionerContractController(PractitionerContractService practitionerContractService) {
        super(practitionerContractService);
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

}
