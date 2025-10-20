package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.validationapi.module.contract.dto.OrganizationContractDTO;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.contract.service.OrganizationContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_ORGANIZATION_CONTRACT;

@RestController
@RequestMapping(value = ENDPOINT_ORGANIZATION_CONTRACT)
public class OrganizationContractController extends BaseContractController<OrganizationContract, OrganizationContractDTO> {

    @Autowired
    public OrganizationContractController(OrganizationContractService organizationContractService) {
        super(organizationContractService);
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
