package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.contract.dto.ContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.projection.ContractProjection;
import com.capacidad.validationapi.module.contract.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_CONTRACTS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_CONTRACTS)
public class ContractController extends BaseContractController<Contract, ContractDTO> {

    private final ContractService contractService;

    @Autowired
    public ContractController(ContractService contractService) {
        super(contractService);
        this.contractService = contractService;
    }

    @GetMapping(params = {"page", "size"})
    @Override
    public ResponseEntity<Object> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContractProjection> currentPage = contractService.findAllContracts(pageable, search);
        PageModelWrapper<EntityModel<ContractProjection>> contractPageModelWrapper = contractService.buildPageResource(currentPage, pageable, search);
        return ResponseEntity.ok(contractPageModelWrapper);
    }

    @GetMapping(params = "name")
    public ResponseEntity<CollectionModelWrapper<EntityModel<ContractProjection>>> findContracts(@RequestParam String name) {
        Set<ContractProjection> results = contractService.findContractsContaining(name);
        ResourceAssembler<ContractProjection, Long> assembler = new ResourceAssembler<>(ContractController.class);
        CollectionModelWrapper<EntityModel<ContractProjection>> resources = new CollectionModelWrapper<>(RESOURCE_CONTRACTS, assembler.toResources(results));
        resources.add(linkTo(methodOn(this.getClass()).findContracts(name)).withSelfRel());
        return ResponseEntity.ok(resources);
    }

}
